package veny.smevente.dao.orientdb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.EventDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.Event;
import veny.smevente.model.Unit;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>Event</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.7.2010
 */
public class EventDaoImpl extends AbstractDaoOrientdb<Event> implements EventDao {

    // fix for https://groups.google.com/forum/?fromgroups#!topic/orient-database/vxG7W5kgbqQ
    /** Formater for DateTime. */
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
                params.put("from", dateFormat.format(from));
                params.put("to", dateFormat.format(to));

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
                        .append(" WHERE patient = :patient AND type IS NOT :type ORDER BY startTime DESC");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("patient", patientId);
                params.put("type", Event.Type.IMMEDIATE_MESSAGE.toString());

                final List<Event> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> findEvents2BulkSend(final Unit unit, final Date olderThan) {

        return getDatabaseWrapper().execute(new ODatabaseCallback<List<Event>>() {
            @Override
            public List<Event> doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE customer.unit = :unit")
                        .append(" AND startTime < :olderThan")
                        .append(" AND sent IS NULL")
                        .append(" AND (sendAttemptCount IS NULL OR sendAttemptCount <= :sac) ORDER BY startTime ASC");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("unit", unit.getId());
                params.put("olderThan", dateFormat.format(olderThan));
                params.put("sac", 3);

                final List<Event> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        });
    }

}
