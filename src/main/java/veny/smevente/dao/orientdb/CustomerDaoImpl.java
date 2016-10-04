package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.CustomerDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.Customer;

import com.google.common.base.Strings;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>Patient</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public class CustomerDaoImpl extends AbstractDaoOrientdb<Customer> implements CustomerDao {

    /** {@inheritDoc} */
    @Override
    public List<Customer> findLikeBy(final Object unitId, final String paramName, final Object value) {
        if (null == unitId) {
            throw new NullPointerException("unit ID cannot be null");
        }
        if (Strings.isNullOrEmpty(paramName)) {
            throw new IllegalArgumentException("parameter name cannot be blank");
        }

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Customer>>() {
            @Override
            public List<Customer> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE unit = :unitId AND ").append(paramName)
                        .append(" LIKE :value");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("unitId", unitId);
                params.put("value", new StringBuilder("%").append(value).append("%").toString());

                final List<Customer> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                //detachWithFirstLevelAssociations(rslt, db);
                for (final Customer entity : rslt) {
                    db.detachAll(entity, false);
                }
                return rslt;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Customer findByBirthNumber(final Object unitId, final String birthNumber) {
        if (null == unitId) {
            throw new NullPointerException("unit ID cannot be null");
        }
        if (Strings.isNullOrEmpty(birthNumber)) {
            throw new IllegalArgumentException("birth number name cannot be blank");
        }

        return getDatabaseWrapper().execute(new ODatabaseCallback<Customer>() {
            @Override
            public Customer doWithDatabase(final OObjectDatabaseTx db) {
                final List<Customer> bn = findBy("unit", unitId, "birthNumber", birthNumber.trim(), null);

                if (bn.isEmpty()) {
                    return null;
                }
                if (bn.size() > 1) {
                    throw new IllegalStateException(
                            "not unique birth number in unit, unitId=" + unitId
                            + ", found=" + bn);
                }
                db.detachAll(bn.get(0), false);
                return bn.get(0);
            }
        });
    }

    // ---------------------------------------------------------- Special Stuff

}
