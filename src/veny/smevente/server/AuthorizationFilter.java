package veny.smevente.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Filter to check if the current session does have a logged in user.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 24.5.2010
 */
@Deprecated
public class AuthorizationFilter implements Filter {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AuthorizationFilter.class.getName());

    /** {@inheritDoc} */
    @Override
    public void destroy() {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void init(final FilterConfig config) throws ServletException {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        // no authorization for Cron Tasks
        if (httpRequest.getRequestURI().startsWith("/rest/cron/task/")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("detected cron task, uri=" + httpRequest.getRequestURI());
            }
            chain.doFilter(request, response);
            return;
        }

        final HttpSession session = httpRequest.getSession();
        if (null == session || session.isNew()) {
            LOG.info("session not found or is new, returned 401, uri=" + httpRequest.getRequestURI());
            ((HttpServletResponse) response).setStatus(401);
            return;
        }

//        final Object currentUser = session.getAttribute(LoginServlet.USER_SESSION_KEY);
//        if (null == currentUser) {
//            LOG.info("user not found in session, returned 401, uri=" + httpRequest.getRequestURI());
//            ((HttpServletResponse) response).setStatus(401);
//        } else {
//            if (LOG.isLoggable(Level.FINER)) {
//                LOG.finer("found logged in user on session, uri="
//                        + httpRequest.getRequestURI() + ", username=" + ((UserDto) currentUser).getUsername()
//                        + ", session=" + session.getId());
//            }
//            chain.doFilter(request, response);
//        }
    }

}
