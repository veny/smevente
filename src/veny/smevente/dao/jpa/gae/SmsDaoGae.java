package veny.smevente.dao.jpa.gae;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import veny.smevente.model.SmsDto;
import veny.smevente.model.gae.Sms;
import veny.smevente.server.JpaGaeUtils;
import veny.smevente.server.JpaGaeUtils.JpaCallback;

/**
 * JPA DAO implementation for <code>Sms</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.7.2010
 */
public class SmsDaoGae extends AbstractDaoGae<Sms> {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(SmsDaoGae.class.getName());

    /**
     * Persists new SMS with <code>flush</code> to set the PK attribute.
     * @param sms SMS to persist
     */
    public void create(final Sms sms) {
        EntityManager em = JpaGaeUtils.get().createEntityManager();
        try {
            EntityTransaction trx = em.getTransaction();
            trx.begin();
            em.persist(sms);
            // the application does not set the primary key attribute
            // until after the entity has been flushed to the database
            em.flush();
            trx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "failed to persist and flush SMS", e);
            throw new IllegalStateException("failed to persist and flush SMS", e);
        } finally {
            em.close();
        }
    }

    /**
     * Gets list of SMSs for given author and time period (not deleted).
     * @param authorId author ID
     * @param from from time
     * @param to to time
     * @param includeDeleted whether the deleted SMSs should be included
     * @return list of SMSs
     */
    @SuppressWarnings("unchecked")
    public List<Sms> findByAuthorAndPeriod(
            final Long authorId, final Date from, final Date to, final boolean includeDeleted) {

        return JpaGaeUtils.execute(new JpaCallback<List<Sms>>() {
            @Override public List<Sms> doWithEntityManager(final EntityManager em) {
                final Query query = em.createQuery("SELECT s FROM " + getPersistentClass().getName() + " s WHERE "
                    + "s.userId=:authorId AND s.medicalHelpStartTime > :from AND s.medicalHelpStartTime < :to");
                query.setParameter("authorId", authorId).setParameter("from", from).setParameter("to", to);
                final List<Sms> found = (List<Sms>) query.getResultList();

                if (includeDeleted) { // load entities to eliminate 'Object Manager has been closed' exception
                    found.size();
                    return found;
                }

                // filter the deleted SMSs
                final List<Sms> rslt = new ArrayList<Sms>();
                for (Sms s : found) {
                    if (0 == (s.getStatus() & SmsDto.STATUS_DELETED)) { rslt.add(s); }
                }
                return rslt;
            }
        });
    }

    /**
     * Gets list of SMSs for given patient (not deleted and special).
     * @param patientId patient ID
     * @return list of SMSs sorted by start time descending
     */
    public List<Sms> findByPatient(final Long patientId) {

        return JpaGaeUtils.execute(new JpaCallback<List<Sms>>() {
            @Override public List<Sms> doWithEntityManager(final EntityManager em) {
                Query query = em.createQuery("SELECT s FROM " + getPersistentClass().getName()
                    + " s WHERE s.patientId=:patientId  ORDER BY medicalHelpStartTime DESC");
                query.setParameter("patientId", patientId);
                @SuppressWarnings("unchecked")
                final List<Sms> found = (List<Sms>) query.getResultList();

                // filter the deleted and special SMSs
                final List<Sms> rslt = new ArrayList<Sms>();
                for (Sms s : found) {
                    if ((0 == (s.getStatus() & SmsDto.STATUS_DELETED))
                        && (0 == (s.getStatus() & SmsDto.STATUS_SPECIAL))) {
                        rslt.add(s);
                    }
                }
                return rslt;
            }
        });
    }

    /**
     * Gets list of SMSs that are older than given timestamp.
     * @param olderThan time that all older SMSs should be found
     * @return list of SMSs
     */
    @SuppressWarnings("unchecked")
    public List<Sms> findSms2BulkSend(final Date olderThan) {
        return JpaGaeUtils.execute(new JpaCallback<List<Sms>>() {
            @Override
            public List<Sms> doWithEntityManager(final EntityManager em) {
                final Query query = em.createQuery("SELECT s FROM " + getPersistentClass().getName()
                    + " s WHERE s.medicalHelpStartTime < :olderThan AND s.sent IS NULL");
                query.setParameter("olderThan", olderThan);
                final List<Sms> found = (List<Sms>) query.getResultList();

                // filter according to 'sendAttemptCount' cannot be done with SQL because of:
                // "Only one inequality filter per query is supported.
                // Encountered both sendAttemptCount and medicalHelpStartTime"
                final List<Sms> rslt = new ArrayList<Sms>();
                for (Sms sms : found) {
                    if ((sms.getSendAttemptCount() < SmsDto.MAX_SEND_ATTEMPTS)
                        && (0 == (sms.getStatus() & SmsDto.STATUS_DELETED))) { rslt.add(sms); }
                }

                return rslt;
            }
        });
    }

    /**
     * Removes given SMS from storage if it was not sent
     * or marks as deleted (soft delete) if already sent.
     * @param id SMS ID
     * @return <i>true</i> by hard delete
     */
    public boolean removeHardOrSoft(final Long id) {
        return JpaGaeUtils.execute(new JpaCallback<Boolean>() {
            @Override
            public Boolean doWithEntityManager(final EntityManager em) throws PersistenceException {
                final Sms sms = em.find(getPersistentClass(), id);
                if (null == sms.getSent()) {
                    em.remove(sms);
                    return Boolean.TRUE;
                } else {
                    sms.setStatus(sms.getStatus() | SmsDto.STATUS_DELETED);
                    em.persist(sms);
                    return Boolean.FALSE;
                }
            }
        }, true);
    }

}
