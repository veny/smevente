package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.PatientDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.Patient;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>Patient</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public class PatientDaoImpl extends AbstractDaoOrientdb<Patient> implements PatientDao {

    /** {@inheritDoc} */
    public List<Patient> findLikeBy(final Object unitId, final String paramName, final Object value) {
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        if (Strings.isNullOrEmpty(paramName)) { throw new IllegalArgumentException("parameter name cannot be blank"); }

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Patient>>() {
            @Override
            public List<Patient> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE unit = :unitId AND (").append(paramName)
                        .append(".indexOf('").append(value).append("') > -1)"); // TODO[veny,A] SQL Injection? Java API

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("unitId", unitId);

                final List<Patient> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        });
    }

    /** {@inheritDoc} */
    public Patient findByBirthNumber(final Object unitId, final String birthNumber) {
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        if (Strings.isNullOrEmpty(birthNumber)) {
            throw new IllegalArgumentException("birth number name cannot be blank");
        }

        return getDatabaseWrapper().execute(new ODatabaseCallback<Patient>() {
            @Override
            public Patient doWithDatabase(final OObjectDatabaseTx db) {
                final List<Patient> bn = findBy("unit", unitId, "birthNumber", birthNumber.trim(), null);

                if (bn.isEmpty()) { return null; }
                if (bn.size() > 1) {
                    throw new IllegalStateException(
                            "not unique birth number in unit, unitId=" + unitId
                            + ", found=" + bn);
                }

                return bn.get(0);
            }
        });
    }

    // ---------------------------------------------------------- Special Stuff

}
