package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.ProcedureDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>Procedure</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public class ProcedureDaoImpl extends AbstractDaoOrientdb<Procedure>
        implements ProcedureDao {

    /** {@inheritDoc} */
    @Override
    public Procedure findByNameAndType(final Object unitId, final String name, final Event.Type type) {
        return getDatabaseWrapper().execute(new ODatabaseCallback<Procedure>() {
            @Override
            public Procedure doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE unit = :unitId AND name = :name AND type = :type");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("unitId", unitId);
                params.put("name", name);
                params.put("type", type.toString());

                final List<Procedure> mhcs = executeWithSoftDelete(db, sql.toString(), params, true);
                if (mhcs.size() > 1) {
                    throw new IllegalStateException("expected max 1 MHC, but found " + mhcs.size());
                }

                return mhcs.isEmpty() ? null : (Procedure) mhcs.get(0);
            }
        });
    }

}
