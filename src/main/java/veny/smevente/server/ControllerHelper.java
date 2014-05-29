package veny.smevente.server;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import veny.smevente.model.User;
import veny.smevente.security.AuthenticationSuccessHandlerImpl;

/**
 * Helper with convenience method to be used by a controller.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 19.6.2013
 */
public final class ControllerHelper {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ControllerHelper.class.getName());

    /** Suppresses default constructor, ensuring non-instantiability. */
    private ControllerHelper() { }

    /**
     * Gets the logged in user stored in session.
     * @param request HTTP request
     * @return current logged in user
     */
    public static User getLoggedInUser(final HttpServletRequest request) {
        final User user = (User)
                request.getSession(false).getAttribute(AuthenticationSuccessHandlerImpl.USER_SESSION_KEY);
        if (null == user) {
            throw new IllegalStateException("user not found in session, no authentication?");
        }
        return user;
    }

    /**
     * Gets the logged in user stored in session.
     * @param request HTTP request
     * @return current logged in user
     */
    public static TimeZone getLoggedInUserTimezone(final HttpServletRequest request) {
        final User user = getLoggedInUser(request);
        if (null == user.getTimezone() || 0 == user.getTimezone().trim().length()) {
            throw new IllegalStateException("user without timezone");
        }
        return TimeZone.getTimeZone(user.getTimezone());
    }

    /**
     * Asserts if the logged in user stored in session is an root.
     * @param request HTTP request
     */
    public static void assertRoot(final HttpServletRequest request) {
        final User user = getLoggedInUser(request);
        if (!user.isRoot()) {
            LOG.error("unauthorized data change (NOT root), username=" + user.getUsername());
            throw new IllegalStateException("non-privileged access (NOT root)");
        }
    }


}
