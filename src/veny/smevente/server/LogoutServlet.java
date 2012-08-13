package veny.smevente.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import veny.smevente.security.AuthenticationSuccessHandlerImpl;

/**
 * Servlet implementing the log out mechanism.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
@Deprecated
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(LogoutServlet.class.getName());

    /** {@inheritDoc} */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException {

        LOG.info("user logged out");
        req.getSession().setAttribute(AuthenticationSuccessHandlerImpl.USER_SESSION_KEY, null);
        req.getSession().invalidate();
        resp.sendRedirect("/rest/ping/");
    }

}
