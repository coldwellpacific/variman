/*
 */
package org.realtors.rets.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.enum.Enum;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Splits output into multiple file appenders using an MDC value as a part of
 * the filename. The same result could be a achieved with filters, but this
 * is much more effecient and scalable.
 */
public class MdcSplitFileAppender extends AppenderSkeleton
{
    public MdcSplitFileAppender()
    {
        super();
        mDelegatedAppenders = new HashMap();
        mDelegate = FILE_APPENDER;
    }

    protected void append(LoggingEvent event)
    {
        String mdcValue = (String) event.getMDC(mMdcName);
        Appender delegate = getDelegate(mdcValue);
        delegate.doAppend(event);
    }

    private Appender getDelegate(String mdcValue)
    {
        Appender delegate =
            (Appender) mDelegatedAppenders.get(mdcValue);
        if (delegate == null)
        {
            delegate = createAppender(mdcValue);
            mDelegatedAppenders.put(mdcValue, delegate);
        }
        return delegate;
    }

    private Appender createAppender(String mdcValue)
    {
        String formattedFile = StringUtils.replace(mFile, "%X", mdcValue);
        FileAppender appender = instantiateDelegate();
        PropertySetter setter = new PropertySetter(appender);
        appender.setName(this.name + "." + mdcValue);
        appender.setLayout(this.layout);
        appender.setThreshold(this.threshold);
        setProperty(setter, "Encoding", mEncoding);
        setProperty(setter, "ImmediateFlush", mImmediateFlush);
        setProperty(setter, "Append", mAppend);
        setProperty(setter, "File", formattedFile);
        setProperty(setter, "BufferedIO", mBufferedIO);
        setProperty(setter, "BufferSize", mBufferSize);

        if (appender instanceof DailyRollingFileAppender)
        {
            setProperty(setter, "DatePattern", mDatePattern);
        }

        if (appender instanceof RollingFileAppender)
        {
            setProperty(setter, "MaxBackupIndex", mMaxBackupIndex);
            setProperty(setter, "MaxFileSize", mMaxFileSize);
        }

        appender.setErrorHandler(errorHandler);
        appender.activateOptions();
        return appender;
    }

    /**
     * Set a property, only if the value is not null. Since our properties are
     * null by default, this only sets delegate propeties, if they have been
     * set on our appender. This allows delegate default values to be set
     * accordingly.
     *
     * @param setter Bean setter to use
     * @param name Property name
     * @param value Property value
     */
    private void setProperty(PropertySetter setter, String name, String value)
    {
        if (value != null)
        {
            setter.setProperty(name, value);
        }
    }

    private FileAppender instantiateDelegate()
    {
        if (mDelegate.equals(DAILY_ROLLING_FILE_APPENDER))
        {
            return new DailyRollingFileAppender();
        }
        else if (mDelegate.equals(ROLLING_FILE_APPENDER))
        {
            return new RollingFileAppender();
        }
        else // if (mDelegate.equals(FILE_APPENDER))
        {
            return new FileAppender();
        }
    }

    public void close()
    {
        Collection appenders = mDelegatedAppenders.values();
        for (Iterator i = appenders.iterator(); i.hasNext();)
        {
            Appender appender = (Appender) i.next();
            LogLog.debug("Closing delegate: " + appender.getName());
            appender.close();
        }
    }

    public boolean requiresLayout()
    {
        return true;
    }

    public String getDelegate()
    {
        return mDelegate.toString();
    }

    public void setDelegate(String delegate)
    {
        mDelegate = Delegate.getEnum(delegate);
        LogLog.debug("Setting delegate: " + mDelegate);
    }

    public String getAppend()
    {
        return mAppend;
    }

    public void setAppend(String append)
    {
        mAppend = append;
    }

    public String getBufferedIO()
    {
        return mBufferedIO;
    }

    public void setBufferedIO(String bufferedIO)
    {
        mBufferedIO = bufferedIO;
    }

    public String getBufferSize()
    {
        return mBufferSize;
    }

    public void setBufferSize(String bufferSize)
    {
        mBufferSize = bufferSize;
    }

    public String getDatePattern()
    {
        return mDatePattern;
    }

    public void setDatePattern(String datePattern)
    {
        mDatePattern = datePattern;
    }

    public String getEncoding()
    {
        return mEncoding;
    }

    public void setEncoding(String encoding)
    {
        mEncoding = encoding;
    }

    public String getFile()
    {
        return mFile;
    }

    public void setFile(String file)
    {
        mFile = file;
    }

    public String getImmediateFlush()
    {
        return mImmediateFlush;
    }

    public void setImmediateFlush(String immediateFlush)
    {
        mImmediateFlush = immediateFlush;
    }

    public String getMaxBackupIndex()
    {
        return mMaxBackupIndex;
    }

    public void setMaxBackupIndex(String maxBackupIndex)
    {
        mMaxBackupIndex = maxBackupIndex;
    }

    public String getMaxFileSize()
    {
        return mMaxFileSize;
    }

    public void setMaxFileSize(String maxFileSize)
    {
        mMaxFileSize = maxFileSize;
    }

    public String getMdcName()
    {
        return mMdcName;
    }

    public void setMdcName(String mdcName)
    {
        mMdcName = mdcName;
    }

    static class Delegate extends Enum
    {
        private Delegate(String name)
        {
            super(name);
        }

        public static Delegate getEnum(String name)
        {
            Delegate delegate = (Delegate) getEnum(Delegate.class, name);
            if (delegate == null)
            {
                return FILE_APPENDER;
            }
            return delegate;
        }
    }

    public static final Delegate FILE_APPENDER = new Delegate("FileAppender");
    public static final Delegate ROLLING_FILE_APPENDER =
        new Delegate("RollingFileAppender");
    public static final Delegate DAILY_ROLLING_FILE_APPENDER =
        new Delegate("DailyRollingFileAppender");

    private Map mDelegatedAppenders;
    private Delegate mDelegate;
    private String mEncoding;
    private String mImmediateFlush;
    private String mAppend;
    private String mFile;
    private String mBufferedIO;
    private String mBufferSize;
    private String mDatePattern;
    private String mMdcName;
    private String mMaxBackupIndex;
    private String mMaxFileSize;
}
