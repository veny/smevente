package veny.smevente.dao.orientdb;

import java.util.List;

import veny.smevente.dao.MembershipDao;
import veny.smevente.model.Membership;


/**
 * OrientDB DAO implementation for <code>Membership</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.6.2010
 */
public class MembershipDaoImpl extends AbstractDaoOrientdb<Membership> implements MembershipDao {


    /** {@inheritDoc} */
    @Override
    public Membership findByUserAndUnit(final Object userId, final Object unitId) {
        if (null == userId) {
            throw new NullPointerException("user ID cannot be null");
        }
        if (null == unitId) {
            throw new NullPointerException("unit ID cannot be null");
        }

        final List<Membership> membs = this.findBy("user", userId, "unit", unitId, null);
        if (membs.size() > 1) {
            throw new IllegalStateException("expected max 1 membership, but found " + membs.size());
        }
        return membs.isEmpty() ? null : membs.get(0);
    }

}
