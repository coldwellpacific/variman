/*
 * Rex RETS Server
 *
 * Author: Dave Dribin
 * Copyright (c) 2004, The National Association of REALTORS
 * Distributed under a BSD-style license.  See LICENSE.TXT for details.
 */

/*
 */
package org.realtors.rets.server.webapp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Configuration;

import org.realtors.rets.server.IOUtils;
import org.realtors.rets.server.JdomUtils;
import org.realtors.rets.server.PasswordMethod;
import org.realtors.rets.server.RetsServerException;
import org.realtors.rets.server.config.RetsConfig;
import org.realtors.rets.server.metadata.MSystem;
import org.realtors.rets.server.metadata.MetadataLoader;
import org.realtors.rets.server.metadata.MetadataManager;
import org.realtors.rets.server.webapp.auth.NonceReaper;
import org.realtors.rets.server.webapp.auth.NonceTable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @web.servlet name="init-servlet"
 *   load-on-startup="1"
 */
public class InitServlet extends RetsServlet
{
    public void init() throws ServletException
    {
        initLog4J();
        try
        {
            LOG.debug("Running init servlet");
            WebApp.setServletContext(getServletContext());
            PasswordMethod.setDefaultMethod(PasswordMethod.DIGEST_A1,
                                            PasswordMethod.RETS_REALM);
            initRetsConfiguration();
            initHibernate();
            initMetadata();
            initNonceTable();
            LOG.info("Init servlet completed successfully");
        }
        catch (ServletException e)
        {
            LOG.fatal("Caught", e);
            Throwable rootCause = e.getRootCause();
            if (rootCause != null)
            {
                LOG.fatal("Caused by", rootCause);
            }
            throw e;
        }
        catch (RuntimeException e)
        {
            LOG.fatal("Caught", e);
            throw e;
        }
        catch (Error e)
        {
            LOG.fatal("Caught", e);
            throw e;
        }
    }

    private String getContextInitParameter(String name, String defaultValue)
    {
        String value = getServletContext().getInitParameter(name);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    private String resolveFromConextRoot(String file)
    {
        return IOUtils.resolve(getServletContext().getRealPath("/"), file);
    }

    /**
     * Initialize log4j. First, the application's context is checked for the
     * file name, and then the servlet context is checked.
     */
    private void initLog4J()
    {
        String log4jInitFile =
            getContextInitParameter("log4j-init-file",
                                    "WEB-INF/classes/log4j.lcf");
        log4jInitFile = resolveFromConextRoot(log4jInitFile);
        WebApp.setLog4jFile(log4jInitFile);
        WebApp.loadLog4j();
    }

    private void initRetsConfiguration() throws ServletException
    {
        try
        {
            String configFile =
                getContextInitParameter("rets-config-file",
                                        "WEB-INF/rex/rets-config.xml");
            configFile = resolveFromConextRoot(configFile);
            mRetsConfig = RetsConfig.initFromXml(new FileReader(configFile));
            LOG.debug(mRetsConfig);

            ServletContext context = getServletContext();
            String getObjectRoot =
                mRetsConfig.getGetObjectRoot(context.getRealPath("/"));
            WebApp.setGetObjectRoot(getObjectRoot);
            LOG.debug("GetObject root: " + getObjectRoot);

            String getObjectPattern =
                mRetsConfig.getGetObjectPattern("pictures/%k-%i.jpg");
            WebApp.setGetObjectPattern(getObjectPattern);
            LOG.debug("GetObject pattern: " + getObjectPattern);
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
        catch (RetsServerException e)
        {
            throw new ServletException(e);
        }
    }

    private void initHibernate() throws ServletException
    {
        try
        {
            LOG.debug("Initializing hibernate");
            Configuration cfg = new Configuration();
            cfg.addJar("rex-hbm-xml.jar");
            cfg.setProperties(mRetsConfig.createHibernateProperties());
            WebApp.setSessions(cfg.buildSessionFactory());
        }
        catch (HibernateException e)
        {
            throw new ServletException("Could not initialize hibernate", e);
        }
    }

    private void initMetadata() throws ServletException
    {
        LOG.debug("Initializing metadata");
        MSystem system = findSystem();
        LOG.debug("Creating metadata manager");
        MetadataManager manager = new MetadataManager();
        manager.addRecursive(system);
        LOG.debug("Adding metadata to servlet context");
        WebApp.setMetadataManager(manager);
    }

    private MSystem findSystem() throws ServletException
    {
        try
        {
            String metadataDir = mRetsConfig.getMetadataDir();
            metadataDir = resolveFromConextRoot(metadataDir);
            LOG.info("Reading metadata from: " + metadataDir);
            MetadataLoader loader = new MetadataLoader();
            List files = IOUtils.listFilesRecursive(
                new File(metadataDir), new MetadataFileFilter());
            List documents = parseAllFiles(files);
            Document merged =
                JdomUtils.mergeDocuments(documents, new Element("RETS"));
            return loader.parseMetadata(merged);
        }
        catch (JDOMException e)
        {
            throw new ServletException(e);
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }

    /**
     * Parses all files, returning a list of JDOM Document objects.
     *
     * @param files list of File objects
     * @return a list of Document objects
     * @throws JDOMException if a JDOM error occurs
     * @throws IOException if an I/O error occurs
     */
    private static List /* Document */ parseAllFiles(List /* File */ files)
        throws JDOMException, IOException
    {
        List documents = new ArrayList();
        SAXBuilder builder = new SAXBuilder();
        for (int i = 0; i < files.size(); i++)
        {
            File file = (File) files.get(i);
            documents.add(builder.build(file));
        }
        return documents;
    }

    /**
     * Filters out directories and files that are not metadata, in particular
     * files used by the 1.0 version of the RETS server. Metadata files must
     * have a ".xml" extension. Certain directories, like Notices, Roles, and
     * Template do not contain any metadata, so they are skipped completely.
     */
    private class MetadataFileFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            if (file.isDirectory())
            {
                return false;
            }

            // These directories do not contain metadata
            String parent = file.getParent();
            if (StringUtils.contains(parent, "Notices") ||
                StringUtils.contains(parent, "Roles") ||
                StringUtils.contains(parent, "Template"))
            {
                return false;
            }

            if (file.getName().endsWith(".xml"))
            {
                return true;
            }
            // Everything else is not considered metadata
            return false;
        }
    }

    private void initNonceTable()
    {
        NonceTable nonceTable = new NonceTable();
        int initialTimeout = mRetsConfig.getNonceInitialTimeout();
        if (initialTimeout != -1)
        {
            nonceTable.setInitialTimeout(
                initialTimeout * DateUtils.MILLIS_IN_MINUTE);
            LOG.debug("Set initial nonce timeout to " + initialTimeout +
                      " minutes");
        }

        int successTimeout = mRetsConfig.getNonceSuccessTimeout();
        if (successTimeout != -1)
        {
            nonceTable.setSuccessTimeout(
                successTimeout * DateUtils.MILLIS_IN_MINUTE);
            LOG.debug("Set success nonce timeout to " + successTimeout +
                      " minutes");
        }
        WebApp.setNonceTable(nonceTable);
        WebApp.setNonceReaper(new NonceReaper(nonceTable));
    }

    public void destroy()
    {
        WebApp.getReaper().stop();
    }

    private static final Logger LOG =
        Logger.getLogger(InitServlet.class);
    private RetsConfig mRetsConfig;
}
