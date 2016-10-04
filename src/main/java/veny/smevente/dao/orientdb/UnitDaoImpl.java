package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.UnitDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.Unit;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;


/**
 * OrientDB DAO implementation for <code>Unit</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.6.2010
 */
public class UnitDaoImpl extends AbstractDaoOrientdb<Unit> implements UnitDao {

    /** {@inheritDoc} */
    @Override
    public List<Unit> getUnitsByUser(final Object userId) {
        if (null == userId) {
            throw new NullPointerException("user ID cannot be null");
        }

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Unit>>() {
            @Override
            public List<Unit> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM (TRAVERSE unit FROM (")
                        .append("SELECT FROM Membership WHERE user = ")
                        .append(userId.toString()) //!!! TODO[veny,A] SQL injection!
                        .append(")) WHERE @class = :clazz");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("x", userId); // TODO [veny,A] there is problem with binded userId
                params.put("clazz", getPersistentClass().getSimpleName());

                final List<Unit> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                for (final Unit entity : rslt) {
                    db.detachAll(entity, false);
                }
                return rslt;
            }
        });
    }

}
