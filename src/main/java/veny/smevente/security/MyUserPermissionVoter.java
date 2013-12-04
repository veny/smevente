package veny.smevente.security;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * Voter to check whether the given user is the current logged in one.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 19.5.2011
 */
public class MyUserPermissionVoter extends AbstractPermissionVoter {

    /** {@inheritDoc} */
    @Override
    public int vote(
            final Authentication authentication, final Object object,
            final Collection<ConfigAttribute> attributes) {

        final Object callerId = getCallerId(authentication);
        final Object userId = object;
        if (null == userId) { throw new NullPointerException("user ID cannot be null"); }

        final int rslt = (callerId.equals(userId) ? ACCESS_GRANTED : ACCESS_DENIED);
        debugLog(rslt, callerId, object);
        return rslt;
    }

    /** @return symbolic name of this permission */
    @Override
    public String getTargetPermission() {
        return "V_MY_USER";
    }

}
