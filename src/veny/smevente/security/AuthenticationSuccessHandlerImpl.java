package veny.smevente.security;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.UserDto;
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

    /** Session key used to store the User instance. */
    public static final String USER_SESSION_KEY = AuthenticationSuccessHandlerImpl.class + "_110518_090015";

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
            final Pair<Long, List<Long>> userDetail = (Pair<Long, List<Long>>) auth.getDetails();
            if (null == userDetail) {
                throw new AuthenticationServiceException("user detail of the caller cannot be null");
            }
            final Long userId = userDetail.getA();
            if (null == userId) { throw new AuthenticationServiceException("user ID in user detail cannot be null"); }

            final UserDto user;
            if (UserDto.ROOT_ID.equals(userId)) {
                user = UserDto.buildRoot();
            } else {
                user = userService.getUser(userId);
            }

            // store the opened client into the session
            final HttpSession session = request.getSession();
            session.setAttribute(USER_SESSION_KEY, user);

            LOG.info("wave user created & stored in session, user=" + auth.getName()
                + ", sessionId=" + request.getSession().getId());

//            // fire event about authentication
//            appContext.publishEvent(new AuthenticationEvent(waveUser, true));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getOutputStream().println("LOGED IN");
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "failed to process onAuthenticationSuccess", t);
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
