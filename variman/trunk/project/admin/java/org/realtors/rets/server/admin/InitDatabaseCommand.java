/*
 * Rex RETS Server
 *
 * Author: Dave Dribin
 * Copyright (c) 2004, The National Association of REALTORS
 * Distributed under a BSD-style license.  See LICENSE.TXT for details.
 */

package org.realtors.rets.server.admin;

import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import org.realtors.rets.server.PasswordMethod;
import org.realtors.rets.server.config.RetsConfig;

import org.apache.log4j.Logger;

import org.wxwindows.wxBoxSizer;
import org.wxwindows.wxDialog;
import org.wxwindows.wxJWorker;
import org.wxwindows.wxStaticText;
import org.wxwindows.wxWindow;
import org.wxwindows.wxWindowDisabler;

public class InitDatabaseCommand
{
    public void execute()
    {
        final wxWindowDisabler disabler = new wxWindowDisabler();
        final AdminFrame frame = Admin.getAdminFrame();
        final InitDatabaseDialog dialog = new InitDatabaseDialog(frame);
        dialog.Show();
        frame.SetStatusText("Initializing database...");
        wxJWorker worker = new wxJWorker()
        {
            public Object construct()
            {
                String message = "";
                try
                {
                    LOG.info("Initializing Hibernate configuration");
                    RetsConfig retsConfig = Admin.getRetsConfig();
                    Configuration config = new Configuration()
                        .addJar("rex-hbm-xml.jar")
                        .setProperties(retsConfig.createHibernateProperties());
                    SessionFactory sessionFactory =
                        config.buildSessionFactory();
                    PasswordMethod.setDefaultMethod(PasswordMethod.DIGEST_A1,
                                                    PasswordMethod.RETS_REALM);
                    LOG.info("Hibernate initialized");
                    Admin.setHibernateConfiguration(config);
                    Admin.setSessionFactory(sessionFactory);
                }
                catch (Throwable e)
                {
                    LOG.error("Caught", e);
                    message = e.getMessage();
                }
                return message;
            }

            public void finished()
            {
                dialog.Destroy();
                disabler.delete();
                frame.SetStatusText("Database initialized successfully");
            }
        };
        worker.start();
    }

    private class InitDatabaseDialog extends wxDialog
    {
        public InitDatabaseDialog(wxWindow parent)
        {
            super(parent, -1, "Status");
            wxBoxSizer box = new wxBoxSizer(wxVERTICAL);
            wxStaticText label =
                new wxStaticText(this, -1, "Initializing database...");
            box.Add(label, 0, wxALIGN_CENTER | wxALL, 35);
            SetSizer(box);
            box.Fit(this);
            CenterOnParent();
        }
    }

    private static final Logger LOG =
        Logger.getLogger(InitDatabaseCommand.class);
}