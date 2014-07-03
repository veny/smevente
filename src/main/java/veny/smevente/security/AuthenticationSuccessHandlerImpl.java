package veny.smevente.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.User;
import veny.smevente.service.UserService;

/**
 * Strategy used to handle a successful user authentication.
 * <p>
 * Used to handle a successful user authentication to return HTTP 200
 * instead of the default 302.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 17.5.2011
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AuthenticationSuccessHandlerImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UserService userService;

    /** {@inheritDoc} */
    @Override
    public void onAuthenticationSuccess(
        final HttpServletRequest request, final HttpServletResponse response, final Authentication auth)
        throws IOException, ServletException {

        LOG.info("user authenticated, user=" + auth.getName() + ", " + getBrowserInfo(request));

        try {
            @SuppressWarnings("unchecked")
            final Pair<User, List<Object>> userDetail = (Pair<User, List<Object>>) auth.getDetails();
            if (null == userDetail) {
                throw new AuthenticationServiceException("user detail of the caller cannot be null");
            }
            final User user = userDetail.getA();
            if (null == user) { throw new AuthenticationServiceException("user in detail cannot be null"); }
            if (null == user.getId()) {
                throw new AuthenticationServiceException("user ID in user detail cannot be null");
            }

            // set session durability
            final HttpSession session = request.getSession();
            session.setMaxInactiveInterval(240 * 60);

//            // fire event about authentication
//            appContext.publishEvent(new AuthenticationEvent(user, true));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().println("LOGED IN");
        } catch (Throwable t) {
            LOG.error("failed to process onAuthenticationSuccess", t);
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getOutputStream().println(t.getMessage());
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Gets some info about the client.
     * @param req the HTTP request
     * @return info about the client
     */
    public static String getBrowserInfo(final HttpServletRequest req) {
        final StringBuilder rslt = new StringBuilder("session=")
                .append(req.getSession().getId())
                .append(", agent=")
                .append(req.getHeader("User-Agent"))
                .append(", clientAddr=")
                .append(req.getRemoteHost());
        return rslt.toString();
    }

}
