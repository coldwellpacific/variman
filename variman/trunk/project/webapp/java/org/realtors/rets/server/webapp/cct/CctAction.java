package org.realtors.rets.server.webapp.cct;

import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.realtors.rets.server.User;
import org.realtors.rets.server.webapp.auth.AuthenticationFilter;
import org.realtors.rets.server.cct.UserInfo;

/**
 * Created by IntelliJ IDEA.
 * User: dbt
 * Date: Aug 26, 2003
 * Time: 2:05:42 PM
 * To change this template use Options | File Templates.
 */
public abstract class CctAction extends Action
{
    void loadCctState(HttpSession session)
    {
        User user = (User) session.getAttribute(USER_KEY);
        UserInfo info = (UserInfo) session.getAttribute(USERINFO_KEY);
        CertificationTestSuite suite =
            (CertificationTestSuite) session.getAttribute(TESTSUITE_KEY);
        if (user != null &&
            !user.getUsername().equals(getUser(session).getUsername()))
        {
            user = null;
            info = null;
            suite = null;
        }
        if (user == null)
        {
            user = getUser(session);
            session.setAttribute(USER_KEY, user);
        }
        if (info == null)
        {
            info = getUserInfo(session);
            session.setAttribute(USERINFO_KEY, info);
        }
        if (suite == null)
        {
            suite = new CertificationTestSuite(user.getUsername());
            session.setAttribute(TESTSUITE_KEY, suite);
        }
    }

    protected User getUser(HttpSession session)
    {
        return (User) session.getAttribute(
            AuthenticationFilter.AUTHORIZED_USER_KEY);
    }

    protected UserInfo getUserInfo(HttpSession session)
    {
        User user = getUser(session);
        if (user.getUsername() == null)
        {
            LOG.warn(user);
            return null;
        }
        return UTILS.getUserInfo(user.getUsername());
    }

    protected CertificationTestSuite getSuite(HttpSession session)
    {
        return (CertificationTestSuite) session.getAttribute(TESTSUITE_KEY);
    }


    public static final String USER_KEY = "cctUser";
    public static final String USERINFO_KEY = "cctUserInfo";
    public static final String TESTSUITE_KEY = "cctSuite";

    private static final UserUtils UTILS = new UserUtils();
    private static final Log LOG = LogFactory.getLog(CctAction.class);
}