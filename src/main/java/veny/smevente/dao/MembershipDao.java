package veny.smevente.dao;

import veny.smevente.model.Membership;

/**
 * Interface for persistence operation with <code>Unit</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 2.9.2012
 */
public interface MembershipDao extends GenericDao<Membership> {

    /**
     * Finds memberships binding given user and unit.
     *
     * @param userId user ID
     * @param unitId unit ID
     * @return found membership or <i>null</i> if not found
     */
    Membership findByUserAndUnit(Object userId, Object unitId);

}
