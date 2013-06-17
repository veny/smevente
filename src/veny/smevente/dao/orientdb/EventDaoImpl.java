package veny.smevente.dao.orientdb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.EventDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.Event;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>Event</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.7.2010
 */
public class EventDaoImpl extends AbstractDaoOrientdb<Event> implements EventDao {

    /** {@inheritDoc} */
    @Override
    public List<Event> findByAuthorAndPeriod(
            final Object authorId, final Date from, final Date to, final boolean includeDeleted) {

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Event>>() {
            @Override
            public List<Event> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE author = :author AND startTime > :from")
                        .append(" AND startTime < :to");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("author", authorId);
                params.put("from", from);
                params.put("to", to);

                final List<Event> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> findByPatient(final Object patientId) {

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Event>>() {
            @Override
            public List<Event> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE patient = :patient ORDER BY startTime DESC");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("patient", patientId);

                return executeWithSoftDelete(db, sql.toString(), params, true);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> findEvents2BulkSend(final Date olderThan) {

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Event>>() {
            @Override
            public List<Event> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getName())
                        .append(" WHERE startTime < :olderThan AND sent IS NULL");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("olderThan", olderThan);

                final List<Event> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        });
    }

}
