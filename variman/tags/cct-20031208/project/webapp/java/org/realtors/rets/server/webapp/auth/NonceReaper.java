/*
 */
package org.realtors.rets.server.webapp.auth;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

/**
 * Expires nonces on a nonce table once a minute.
 */
public class NonceReaper
{
    /**
     * Create a new nonce reaper for a nonce table.
     *
     * @param nonceTable Nonce table to expire
     */
    public NonceReaper(NonceTable nonceTable)
    {
        mNonceTable = nonceTable;
        mTimer = new Timer();
        mTimer.schedule(new ReaperTask(),
                        DateUtils.MILLIS_IN_MINUTE,     // Initial delay
                        DateUtils.MILLIS_IN_MINUTE);    // Subsqeuent delay
        LOG.debug("Started nonce reaper");
    }

    /**
     * Stops expiring nonces.
     */ 
    public void stop()
    {
        LOG.debug("Stopping nonce reaper");
        mTimer.cancel();
    }

    private class ReaperTask extends TimerTask
    {
        public void run()
        {
            mNonceTable.expireNonces();
        }
    }

    private static final Logger LOG =
        Logger.getLogger(NonceReaper.class);
    private NonceTable mNonceTable;
    private Timer mTimer;
}
