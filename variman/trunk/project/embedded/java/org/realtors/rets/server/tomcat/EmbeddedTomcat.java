/*
 * Rex RETS Server
 *
 * Author: Dave Dribin
 * Copyright (c) 2004, The National Association of REALTORS
 * Distributed under a BSD-style license.  See LICENSE.TXT for details.
 */

package org.realtors.rets.server.tomcat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Wrapper;
import org.apache.catalina.logger.FileLogger;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class EmbeddedTomcat
{

    public static synchronized EmbeddedTomcat getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new EmbeddedTomcat();
        }
        return mInstance;
    }

    private static EmbeddedTomcat mInstance;

    private EmbeddedTomcat()
    {
        mInitFailed = true;
    }

    public void setRexHome(String path)
    {
        mRexHome = path;
        mCatalinaHome = mRexHome + File.separator + "server";
    }

    public String getRexHome()
    {
        return mCatalinaHome;
    }

    public void startTomcat()
        throws Exception
    {
        readRetsConfig();
        // Set the Tomcat home directory
        System.setProperty("catalina.home", mCatalinaHome);
        System.setProperty("catalina.base", mCatalinaHome);

        // Create an embedded server
        mEmbedded = new Embedded();
        // Print all log statments to standard error
        mEmbedded.setDebug(0);
        FileLogger logger = new FileLogger();
        logger.setDirectory(mRexHome + File.separator + "server" +
                            File.separator + "logs");
        mEmbedded.setLogger(logger);

        // Create an engine
        Engine engine = mEmbedded.createEngine();
        engine.setDefaultHost("localhost");

        // Create a default virtual host
        mHost = mEmbedded.createHost("localhost", mCatalinaHome + "/webapps");
        engine.addChild(mHost);
        engine.setRealm(new MemoryRealm());

        // Install the assembed container hierarchy
        mEmbedded.addEngine(engine);

        // Assembled and install a default HTTP connector
        Connector connector = mEmbedded.createConnector(null, getPort(), false);
        mEmbedded.addConnector(connector);

        // Start the embedded server
        mEmbedded.start();

        Runtime.getRuntime().addShutdownHook(new Shutdown());
    }

    private void readRetsConfig()
        throws ParserConfigurationException, SAXException, IOException
    {
        // Use standard DOM rather than RetsConfig to parse the XML file.
        // Moving the Rex jar file into the Tomcat classpath messes up the
        // classloader, and then it cannot be used inside the webapp. I really
        // don't know what games Tomcat plays with classloaders, but it causes
        // all sorts of problems. In any case, we only need the port parameter,
        // so it's easy enough just to use DOM and grab that one XML tag.
        String configFile =
            mRexHome + "/webapp/WEB-INF/classes/rets-config.xml";
        DocumentBuilder builder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new File(configFile));
        Element root = document.getDocumentElement();
        Node portTag = root.getElementsByTagName("port").item(0);
        mPort = Integer.parseInt(portTag.getFirstChild().getNodeValue());
    }

    private int getPort()
    {
        return mPort;
    }

    public void stopTomcat() throws Exception
    {
        mEmbedded.stop();
        synchronized (this)
        {
            notify();
        }
    }

    public void waitUntilStopped()
        throws InterruptedException
    {
        synchronized (this)
        {
            wait();
        }
    }

    public void registerWebapp(String contextPath, URL webapp)
        throws Exception
    {
        if (contextPath.equals("/"))
        {
            contextPath = "";
        }

        mContext = mEmbedded.createContext(contextPath, webapp.getFile());
        mContext.addInstanceListener(InitCompleteListener.class.getName());
        Enumeration propertyNames = System.getProperties().propertyNames();
        while (propertyNames.hasMoreElements())
        {
            String name = (String) propertyNames.nextElement();
            if (name.startsWith("context."))
            {
                String value = System.getProperty(name);
                String contextName = name.substring("context.".length());
                mContext.addParameter(contextName, value);
            }
        }
        mHost.addChild(mContext);
    }

    public static class InitCompleteListener implements InstanceListener
    {
        public void instanceEvent(InstanceEvent event)
        {
            Wrapper wrapper = event.getWrapper();
            String servletName = event.getWrapper().getName();
            // Ignore this event, and unregister for future events not coming
            // from the init servlet
            if (!servletName.equals("init-servlet"))
            {
                wrapper.removeInstanceListener(this);
                return;
            }

            // Wait for initialization of servlet to complete
            if (!event.getType().equals(InstanceEvent.AFTER_INIT_EVENT))
            {
                return;
            }

            // Init only succeeded if the init servlet completed without
            // throwing an exception.
            EmbeddedTomcat tomcat = EmbeddedTomcat.getInstance();
            if (event.getException() == null)
            {
                tomcat.setInitFailed(false);
            }
            // We got what we needed, so remove this listener
            wrapper.removeInstanceListener(this);
        }
    }

    public void setInitFailed(boolean initFailed)
    {
        mInitFailed = initFailed;
    }

    public boolean initFailed()
    {
        return mInitFailed;
    }

    public static void main(String args[])
    {
        try
        {
            String rexHome = System.getProperty("rex.home");
            if (rexHome == null)
            {
                rexHome = System.getProperty("user.dir");
                System.setProperty("rex.home", rexHome);
            }
            EmbeddedTomcat tomcat = EmbeddedTomcat.getInstance();
            tomcat.setRexHome(rexHome);

            tomcat.startTomcat();

            URL url = new URL("file:" + rexHome + "/webapp");
            tomcat.registerWebapp("/", url);
            if (tomcat.initFailed())
            {
                System.exit(1);
            }
            tomcat.waitUntilStopped();
            System.out.println("Stopped");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class Shutdown extends Thread
    {
        public void run()
        {
            try
            {
                EmbeddedTomcat.this.stopTomcat();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private String mRexHome;
    private String mCatalinaHome;
    private Embedded mEmbedded;
    private Host mHost;
    private int mPort;
    private boolean mInitFailed;
    private Context mContext;
}
