package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.MembershipDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Membership;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;


/**
 * OrientDB DAO implementation for <code>Unit</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.6.2010
 */
public class MembershipDaoImpl extends AbstractDaoOrientdb<Membership> implements MembershipDao {


    /** {@inheritDoc} */
    public Membership findByUserAndUnit(final Object userId, final Object unitId) {
        if (null == userId) { throw new NullPointerException("user ID cannot be null"); }
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }

        return getDatabaseWrapper().execute(new ODatabaseCallback<Membership>() {
            @Override
            public Membership doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE user = :userId AND units contains :unitId");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("userId", userId);
                params.put("unitId", unitId);

                final List<AbstractEntity> membs = executeWithSoftDelete(db, sql.toString(), params);
                if (membs.size() > 1) {
                    throw new IllegalStateException("expected max 1 membership, but found " + membs.size());
                }

                return membs.isEmpty() ? null : (Membership) db.detach(membs.get(0));
            }
        });
    }

}
