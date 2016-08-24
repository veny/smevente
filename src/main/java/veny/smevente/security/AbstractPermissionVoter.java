package veny.smevente.security;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.User;

/**
 * Represents base class of voters for Smevente specific permissions.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
public abstract class AbstractPermissionVoter implements AccessDecisionVoter<Object> {

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
    protected Object getCallerId(final Authentication authentication) {
        @SuppressWarnings("unchecked")
        final Pair<User, List<Object>> userDetail = (Pair<User, List<Object>>) authentication.getDetails();
        if (null == userDetail) {
            throw new AuthenticationCredentialsNotFoundException("user detail of the caller cannot be null");
        }
        final User user = userDetail.getA();
        if (null == user) {
            throw new AuthenticationServiceException("user in detail cannot be null");
        }
        if (null == user.getId()) {
            throw new AuthenticationServiceException("user ID in user detail cannot be null");
        }
        return user.getId();
    }

    /**
     * Gets unit IDs where the current logged in user is ADMIN.
     * @param authentication the caller invoking the method
     * @return list of unit IDs
     */
    protected List<Object> getAdminUnits(final Authentication authentication) {
        @SuppressWarnings("unchecked")
        final Pair<User, List<Object>> userDetail = (Pair<User, List<Object>>) authentication.getDetails();
        if (null == userDetail) {
            throw new NullPointerException("user detail of the caller cannot be null");
        }
        final List<Object> adminUnits = userDetail.getB();
        if (null == adminUnits) {
            throw new NullPointerException("unit list in user detail cannot be null");
        }
        return adminUnits;
    }

    /**
     * Logs the voter checks.
     * @param permission resulting permission
     * @param callerId caller ID
     * @param object object to check
     */
    protected void debugLog(final int permission, final Object callerId, final Object object) {
        if (LOG.isDebugEnabled()) {
            final StringBuilder msg = new StringBuilder("permission checked, callerId=")
                .append(callerId)
                .append(", voterName=")
                .append(getTargetPermission())
                .append(", object=")
                .append(object)
                .append(", permission=" + (ACCESS_GRANTED == permission ? "GRANTED" : "DENIED"));
            LOG.debug(msg.toString());
        }
    }

}
