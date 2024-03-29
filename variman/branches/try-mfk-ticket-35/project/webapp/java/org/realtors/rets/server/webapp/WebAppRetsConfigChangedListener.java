/*
 * Variman RETS Server
 *
 * Author: Dave Dribin
 * Copyright (c) 2009, The National Association of REALTORS
 * Distributed under a BSD-style license.  See LICENSE.TXT for details.
 */
package org.realtors.rets.server.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.realtors.rets.server.RetsServerException;
import org.realtors.rets.server.config.RetsConfigChangedListener;

/**
 * A simple RetsConfigChangedListener that reloads the RETS configuration in
 * the WebApp.
 * 
 * @author timbo
 */
public class WebAppRetsConfigChangedListener implements RetsConfigChangedListener {
    private Log LOGGER = LogFactory.getLog(WebAppRetsConfigChangedListener.class);

    /*- (non-Javadoc)
     * @see org.realtors.rets.server.config.RetsConfigChangedListener#retsConfigChanged()
     */
    public void retsConfigChanged() {
        try {
            WebApp.loadConfiguration();
        } catch (RetsServerException e) {
            LOGGER.warn("Unable to reset RETS configuration in the application.");
        }
    }

}
