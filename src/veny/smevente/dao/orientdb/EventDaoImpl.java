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

                return executeWithSoftDelete(db, sql.toString(), params, true);
            }
        });
    }

    /** {@inheritDoc} */
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

//    /** {@inheritDoc} */
//    public List<Event> findSms2BulkSend(final Date olderThan) {
//        return JpaGaeUtils.execute(new JpaCallback<List<Event>>() {
//            @Override
//            public List<Event> doWithEntityManager(final EntityManager em) {
//                final Query query = em.createQuery("SELECT s FROM " + getPersistentClass().getName()
//                    + " s WHERE s.medicalHelpStartTime < :olderThan AND s.sent IS NULL");
//                query.setParameter("olderThan", olderThan);
//                final List<Event> found = (List<Event>) query.getResultList();
//
//                // filter according to 'sendAttemptCount' cannot be done with SQL because of:
//                // "Only one inequality filter per query is supported.
//                // Encountered both sendAttemptCount and medicalHelpStartTime"
//                final List<Event> rslt = new ArrayList<Event>();
//                for (Event sms : found) {
//                    if ((sms.getSendAttemptCount() < Event.MAX_SEND_ATTEMPTS)
//                        && (0 == (sms.getStatus() & Event.STATUS_DELETED))) { rslt.add(sms); }
//                }
//
//                return rslt;
//            }
//        });
//    }

}
