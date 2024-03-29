/*
 * Rex RETS Server
 *
 * Author: Dave Dribin
 * Copyright (c) 2004, The National Association of REALTORS
 * Distributed under a BSD-style license.  See LICENSE.TXT for details.
 */

/*
 */

package org.realtors.rets.server.webapp.cct;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;

import org.realtors.rets.server.RetsReplyException;
import org.realtors.rets.server.RetsVersion;
import org.realtors.rets.server.webapp.RetsServletRequest;
import org.realtors.rets.server.webapp.RetsServletResponse;
import org.realtors.rets.server.webapp.SessionFilter;
import org.realtors.rets.server.webapp.ServletUtils;

public class LoginHandler extends BaseServletHandler
{
    public static final String NAME = "/login";

    public LoginHandler()
    {
        super();
        reset();
    }

    public String getName()
    {
        return NAME;
    }

    public void reset()
    {
        super.reset();
        mSessionId = "";
        mCapabilityUrlLevel = CapabilityUrlLevel.NORMAL;
        mRelativeUrls = false;
        mAlternateActionUrl = false;
        mAlternateLoginUrl = false;
    }

    public void setSessionId(String sessionId)
    {
        mSessionId = sessionId;
    }

    public void setRelativeUrls(boolean relativeUrls)
    {
        mRelativeUrls = relativeUrls;
    }

    public void setAlternateActionUrl(boolean alternateActionUrl)
    {
        mAlternateActionUrl = alternateActionUrl;
    }

    public void setAlternateLoginUrl(boolean alternateLoginUrl)
    {
        mAlternateLoginUrl = alternateLoginUrl;
    }

    protected void serviceRets(RetsServletRequest request,
                               RetsServletResponse response)
        throws RetsReplyException, IOException
    {
        SessionFilter.validateSession(request.getSession());

        mContextPath = ServletUtils.getContextPath(request);
        Cookie cookie = new Cookie("RETS-Session-ID", mSessionId);
        cookie.setPath("/");
        response.addCookie(cookie);
        mOut = response.getXmlWriter();
        println(mOut, "<RETS ReplyCode=\"0\" " +
                      "ReplyText=\"Operation Successful\">");
        if (request.getRetsVersion() != RetsVersion.RETS_1_0)
        {
            println(mOut, "<RETS-RESPONSE>");
        }
        println(mOut, "MemberName = Joe Schmoe");
        println(mOut, "User = A123,5678,1,A123");
        println(mOut, "Broker = B123");
        println(mOut, "MetadataVersion = 1.00.000");
        println(mOut, "MinMetadataVersion = 1.00.000");
        printNormalUrl(mOut, "Action", getActionUrl());
        printMaximalUrl(mOut, "ChangePassword", "changePassword");
        printMaximalUrl(mOut, "GetObject", "getObject");
        printAbsoluteCapabilityUrl(mOut, "Login", getLoginUrl());
        printMaximalUrl(mOut, "LoginComplete", "loginComplete");
        printNormalUrl(mOut, "Logout", "cct/logout");
        printMinimalUrl(mOut, "Search", "cct/search");
        printMinimalUrl(mOut, "GetMetadata", "cct/getMetadata");
        printMaximalUrl(mOut, "Update", "update");
        if (request.getRetsVersion() != RetsVersion.RETS_1_0)
        {
            println(mOut, "</RETS-RESPONSE>");
        }
        println(mOut, "</RETS>");
    }

    private String getActionUrl()
    {
        return mAlternateActionUrl ? "cct/actionAlt" : "cct/action";
    }

    private String getLoginUrl()
    {
        return mAlternateLoginUrl ? "cct/loginAlt" : "cct/login";
    }

    private void printCapabilityUrl(PrintWriter out, String capability,
                                    String url)
    {
        if (mRelativeUrls)
        {
            printRelativeCapabilityUrl(out, capability, url);
        }
        else
        {
            printAbsoluteCapabilityUrl(out, capability, url);
        }
    }

    private void printAbsoluteCapabilityUrl(PrintWriter out, String capability,
                                            String url)
    {
        println(out, capability + " = " + mContextPath + "/rets/" + url);
    }

    private void printRelativeCapabilityUrl(PrintWriter out, String capability,
                                            String url)
    {
        println(out, capability + " = " + "/rets/" + url);
    }

    private void printMinimalUrl(PrintWriter out, String capability, String url)
    {
        printCapabilityUrl(out, capability, url);
    }

    private void printNormalUrl(PrintWriter out, String capability, String url)
    {
        if (mCapabilityUrlLevel != CapabilityUrlLevel.MINIMAL)
        {
            printCapabilityUrl(out, capability, url);
        }
    }

    private void printMaximalUrl(PrintWriter out, String capability, String url)
    {
        if (mCapabilityUrlLevel == CapabilityUrlLevel.MAXIMMAL)
        {
            printCapabilityUrl(out, capability, url);
        }
    }

    /**
     * Prints a line always using CRLF rather than using the system property
     * line.separator.
     *
     * @param out
     * @param data
     */
    private void println(PrintWriter out, String data)
    {
        out.print(data);
        out.print("\r\n");
    }

    public void setCapabilityUrlLevel(CapabilityUrlLevel capabilityUrlLevel)
    {
        mCapabilityUrlLevel = capabilityUrlLevel;
    }

    private String mSessionId;
    private CapabilityUrlLevel mCapabilityUrlLevel;
    private boolean mRelativeUrls;
    private boolean mAlternateActionUrl;
    private boolean mAlternateLoginUrl;
    private StringBuffer mContextPath;
    private PrintWriter mOut;
}
