package veny.smevente.security;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import veny.smevente.client.utils.Pair;

/**
 * Represents base class of voters for Smevente specific permissions.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
public abstract class AbstractPermissionVoter implements AccessDecisionVoter {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AbstractPermissionVoter.class.getName());


    /** {@inheritDoc} */
    @Override
    public boolean supports(final ConfigAttribute attribute) {
        return ((null != attribute.getAttribute()) && (attribute.getAttribute().startsWith(getTargetPermission())));
    }

    /** {@inheritDoc} */
    @Override
    public boolean supports(final Class< ? > clazz) {
        return true;
    }

    /**
     * Symbolic name of target permission.
     * @return symbolic name of target permission
     */
    public abstract String getTargetPermission();

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("AbstractPermissionVoter [permission=")
            .append(getTargetPermission())
            .append("]")
            .toString();
    }

    /**
     * Gets current logged in user ID.
     * @param authentication the caller invoking the method
     * @return logged in user ID
     */
    protected Long getCallerId(final Authentication authentication) {
        @SuppressWarnings("unchecked")
        final Pair<Long, List<Long>> userDetail = (Pair<Long, List<Long>>) authentication.getDetails();
        if (null == userDetail) { throw new NullPointerException("user detail of the caller cannot be null"); }
        final Long userId = userDetail.getA();
        if (null == userId) { throw new NullPointerException("user ID in user detail cannot be null"); }
        return userId;
    }

    /**
     * Gets unit IDs where the current logged in user is ADMIN.
     * @param authentication the caller invoking the method
     * @return list of unit IDs
     */
    protected List<Long> getAdminUnits(final Authentication authentication) {
        @SuppressWarnings("unchecked")
        final Pair<Long, List<Long>> userDetail = (Pair<Long, List<Long>>) authentication.getDetails();
        if (null == userDetail) { throw new NullPointerException("user detail of the caller cannot be null"); }
        final List<Long> adminUnits = userDetail.getB();
        if (null == adminUnits) { throw new NullPointerException("unit list in user detail cannot be null"); }
        return adminUnits;
    }

    /**
     * Logs the voter checks.
     * @param permission resulting permission
     * @param callerId caller ID
     * @param object object to check
     */
    protected void debugLog(final int permission, final Long callerId, final Object object) {
        if (LOG.isLoggable(Level.FINER)) {
            final StringBuilder msg = new StringBuilder("permission checked, callerId=")
                .append(callerId)
                .append(", voterName=")
                .append(getTargetPermission())
                .append(", object=")
                .append(object)
                .append(", permission=" + (ACCESS_GRANTED == permission ? "GRANTED" : "DENIED"));
            LOG.finer(msg.toString());
        }
    }

}
