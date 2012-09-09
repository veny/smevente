package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.MedicalHelpCategoryDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.MedicalHelpCategory;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>MedicalHelpCategory</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public class MedicalHelpCategoryDaoImpl extends AbstractDaoOrientdb<MedicalHelpCategory>
        implements MedicalHelpCategoryDao {

    /** {@inheritDoc} */
    public MedicalHelpCategory findByNameAndType(final Object unitId, final String name, final Short categoryType) {
        return getDatabaseWrapper().execute(new ODatabaseCallback<MedicalHelpCategory>() {
            @Override
            public MedicalHelpCategory doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE unit = :unitId AND name = :name AND type = :type");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("unitId", unitId);
                params.put("name", name);
                params.put("type", categoryType);

                final List<MedicalHelpCategory> mhcs = executeWithSoftDelete(db, sql.toString(), params, true);
                if (mhcs.size() > 1) {
                    throw new IllegalStateException("expected max 1 MHC, but found " + mhcs.size());
                }

                return mhcs.isEmpty() ? null : (MedicalHelpCategory) mhcs.get(0);
            }
        });
    }

    /** {@inheritDoc} */
    public List<MedicalHelpCategory> findByType(final Object unitId, final Short categoryType, final String orderBy) {
        return findBy("unit", unitId, "type", categoryType, orderBy);
    }

}
