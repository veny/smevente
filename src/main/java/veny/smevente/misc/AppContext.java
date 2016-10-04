package veny.smevente.misc;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.User;
import veny.smevente.shared.AppVersion;

/**
 * A helper class.
 * It is aimed to<ul>
 * <li> process some application operations when the application starts
 * <li> provide set of convenient methods usable in the scope of server side application
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 2.7.2014
 */
public class AppContext {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AppContext.class.getName());

    /**
     * Application entry point.
     */
    @PostConstruct
    public void start() {
        LOG.fine("initialization...");
        // server works in UTC, all date-times will be converted according to user's time zone
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
        LOG.info("AppContext initialized ok, version=" + AppVersion.VERSION + ", timezone="
                + TimeZone.getDefault() + ", locale=" + Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Gets the logged in user stored in security context.
     * @return current logged in user
     */
    public User getLoggedInUser() {
        @SuppressWarnings("unchecked")
        final Pair<User, List<Object>> userDetails = (Pair<User, List<Object>>)
                SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (null == userDetails) {
            throw new AuthenticationCredentialsNotFoundException("user detail in security context cannot be null");
        }
        final User user = userDetails.getA();
        if (null == user) {
            throw new AuthenticationCredentialsNotFoundException("user in security context cannot be null");
        }
        if (null == user.getId()) {
            throw new AuthenticationCredentialsNotFoundException("user ID in security context cannot be null");
        }

        return user;
    }

    /**
     * Gets softly the logged in user stored in security context.
     * It means no exception if no corresponding data in security context.
     * @return current logged in user if possible, otherwise <i>null</i>
     */
    @SuppressWarnings("unchecked")
    public User getLoggedInUserSoftly() {
        if (null != SecurityContextHolder.getContext().getAuthentication()) {

            final Object userDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
            if (null != userDetails && (userDetails instanceof Pair)
                    && (null != ((Pair<User, List<Object>>) userDetails).getA())) {
                return ((Pair<User, List<Object>>) userDetails).getA();
            }
        }
        return null;
    }

    /**
     * Gets softly ID of the logged in user stored in security context.
     * It means no exception if no corresponding data in security context.
     * @return current logged in user ID if possible, otherwise <i>null</i>
     */
    public String getLoggedInUserIdSoftly() {
        final User u = getLoggedInUserSoftly();
        return null == u || null == u.getId() ? null : u.getId().toString();
    }

    /**
     * Gets the logged in user stored in session.
     * @return current logged in user
     */
    public TimeZone getLoggedInUserTimezone() {
        final User user = getLoggedInUser();
        if (null == user.getTimezone() || 0 == user.getTimezone().trim().length()) {
            throw new IllegalStateException("user without timezone");
        }
        return TimeZone.getTimeZone(user.getTimezone());
    }

    /**
     * Asserts if the logged in user stored in session is an root.
     */
    public void assertRoot() {
        final User user = getLoggedInUser();
        if (!user.isRoot()) {
            LOG.severe("unauthorized data change (NOT root), username=" + user.getUsername());
            throw new IllegalStateException("non-privileged access (NOT root)");
        }
    }


    // ----------------------------------------------------------- Time Methods

    /**
     * Convert date from UTC to user's time zone.
     * @param date date in UTC
     * @return date recalculated from UTC to given time zone
     */
    public Date fromUtcToUserView(final Date date) {
        if (null == date) {
            return null;
        }
        final TimeZone to = getLoggedInUserTimezone();

        final int tzOffset = to.getOffset(date.getTime());
        return new Date(date.getTime() + tzOffset);
    }

    /**
     * Convert date from user's time zone to UTC.
     * @param date date in user's perspective
     * @return date recalculated from given time zone to UTC
     */
    public Date fromUserViewToUtc(final Date date) {
        if (null == date) {
            return null;
        }
        final TimeZone to = getLoggedInUserTimezone();

        final int tzOffset = to.getOffset(date.getTime());
        return new Date(date.getTime() - tzOffset);
    }

}
