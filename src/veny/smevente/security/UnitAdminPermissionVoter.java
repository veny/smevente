package veny.smevente.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * Voter to check whether the logged in user is an ADMIN in the given unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
public class UnitAdminPermissionVoter extends AbstractPermissionVoter {

    /** {@inheritDoc} */
    @Override
    public int vote(
            final Authentication authentication, final Object object,
            final Collection<ConfigAttribute> attributes) {

        int rslt = ACCESS_DENIED;

        final Long callerId = getCallerId(authentication);
        final List<Long> adminUnits = getAdminUnits(authentication);

        // get ID of the unit
        final Long unitId = (Long) object;
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }

        for (Long adminUnitId : adminUnits) {
            if (unitId.equals(adminUnitId)) {
                rslt = ACCESS_GRANTED;
                break;
            }
        }

        debugLog(rslt, callerId, object);
        return rslt;
    }

//    /** Dependency. */
//    @Autowired
//    private MembershipDaoGae membershipDao;

//    /** {@inheritDoc} */
//    @Override
//    public int vote(
//            final Authentication authentication, final Object object,
//            final Collection<ConfigAttribute> attributes) {
//
//        int rslt = ACCESS_DENIED;
//
//        // get ID of the logged in user
//        final Long userId = (Long) authentication.getDetails();
//        if (null == userId) { throw new NullPointerException("user ID in authentication details cannot be null"); }
//
//        // get ID of the unit
//        final Long unitId = (Long) object;
//        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
//
//        final List<Membership> memberships = membershipDao.findBy("userId", userId, "unitId", unitId, null);
//        if (memberships.size() > 1) {
//            throw new IllegalStateException("found more than 1 memberships, userId=" + userId + ", unitId=" + unitId);
//        }
//
//        if (memberships.isEmpty()) {
//            LOG.warning("no membership in unit, userId=" + userId + ", unitId=" + unitId);
//        } else {
//            rslt = (Type.ADMIN == memberships.get(0).getType() ? ACCESS_GRANTED : ACCESS_DENIED);
//        }
//
//        if (LOG.isLoggable(Level.FINER)) {
//            LOG.finer("permission checked, userId=" + userId + ", unitId=" + unitId
//                    + ", permission=" + (ACCESS_GRANTED == rslt ? "GRANTED" : "DENIED"));
//        }
//        return rslt;
//    }

    /** @return symbolic name of this permission */
    @Override
    public String getTargetPermission() {
        return "V_UNIT_ADMIN";
    }

}
