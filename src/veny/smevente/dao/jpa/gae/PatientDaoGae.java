package veny.smevente.dao.jpa.gae;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import veny.smevente.model.gae.Patient;
import veny.smevente.server.JpaGaeUtils;
import veny.smevente.server.JpaGaeUtils.JpaCallback;
import veny.smevente.service.TextUtils;

/**
 * JPA DAO implementation for <code>Patient</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public class PatientDaoGae extends AbstractDaoGae<Patient> {

    /**
     * Finds patients according to given first name.
     * @param unitId ID to search in
     * @param firstname first name to search
     * @return list of found patients
     * {@link http://gae-java-persistence.blogspot.com/2009/11/case-insensitive-queries.html}
     */
    public List<Patient> findByFirstname(final Long unitId, final String firstname) {

        return JpaGaeUtils.execute(new JpaCallback<List<Patient>>() {
            @Override
            public List<Patient> doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.unitId=:unitId AND e.upperFirstname LIKE :firstname");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                query.setParameter("firstname", TextUtils.convert2ascii(firstname.toUpperCase()) + "%");
                setSoftDeleteFilter(query);

                @SuppressWarnings("unchecked")
                final List<Patient> rslt = (List<Patient>) query.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();

                return rslt;
            }
        });
    }

    /**
     * Finds patients according to given surname.
     * @param unitId ID to search in
     * @param surname surname to search
     * @return list of found patients
     */
    public List<Patient> findBySurname(final Long unitId, final String surname) {

        return JpaGaeUtils.execute(new JpaCallback<List<Patient>>() {
            @Override
            public List<Patient> doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.unitId=:unitId AND e.upperSurname LIKE :surname");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                query.setParameter("surname", TextUtils.convert2ascii(surname.toUpperCase()) + "%");
                setSoftDeleteFilter(query);
                @SuppressWarnings("unchecked")
                final List<Patient> rslt = (List<Patient>) query.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();

                return rslt;
            }
        });
    }

    /**
     * Finds patients according to given phone number.
     * @param unitId ID to search in
     * @param phoneNumber phone number to search
     * @return list of found patients
     */
    public List<Patient> findByPhoneNumber(final Long unitId, final String phoneNumber) {

        return JpaGaeUtils.execute(new JpaCallback<List<Patient>>() {
            @Override
            public List<Patient> doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.unitId=:unitId AND e.phoneNumber LIKE :phoneNumber");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                query.setParameter("phoneNumber", phoneNumber + "%");
                setSoftDeleteFilter(query);
                @SuppressWarnings("unchecked")
                final List<Patient> rslt = (List<Patient>) query.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();

                return rslt;
            }
        });
    }

    /**
     * Finds patients according to given birth number.
     * @param unitId ID to search in
     * @param birthNumber birth number to search
     * @return list of found patients
     */
    public Patient findByBirthNumber(final Long unitId, final String birthNumber) {
        return JpaGaeUtils.execute(new JpaCallback<Patient>() {
            @Override
            public Patient doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.unitId=:unitId AND e.birthNumber LIKE :birthNumber");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                query.setParameter("birthNumber", birthNumber + "%");
                setSoftDeleteFilter(query);
                final Patient rslt = (Patient) query.getSingleResult();
                return rslt;
            }
        });
    }

    /**
     * Finds patients according to given birth number prefix.
     * @param unitId ID to search in
     * @param birthNumber birth number prefix to search
     * @return list of found patients
     */
    public List<Patient> findByBirthNumberPrefix(final Long unitId, final String birthNumber) {
        return JpaGaeUtils.execute(new JpaCallback<List<Patient>>() {
            @Override
            public List<Patient> doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.unitId=:unitId AND e.birthNumber LIKE :birthNumber");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("unitId", unitId);
                query.setParameter("birthNumber", birthNumber + "%");
                setSoftDeleteFilter(query);
                @SuppressWarnings("unchecked")
                final List<Patient> rslt = (List<Patient>) query.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();

                return rslt;
            }
        });
    }

//    /**
//     * Marks given patient as deleted (STATUS_DELETED).
//     * @param patientId patient ID
//     */
//    public void remove(final Long patientId) {
//        JpaGaeUtils.execute(new JpaCallback<Object>() {
//            @Override
//            public Object doWithEntityManager(final EntityManager em) throws PersistenceException {
//                final Patient patient = em.find(getPersistentClass(), patientId);
//                patient.setStatus(patient.getStatus() | PatientDto.STATUS_DELETED);
//                em.persist(patient);
//                return null;
//            }
//        }, true);
//    }

    // ---------------------------------------------------------- Special Stuff

    /**
     * Finds all patients wit the version number lesser than given one.
     *
     * @param version version that should be updated
     * @return list of found patients
     */
    public List<Patient> findLesserVersion(final int version) {
        return JpaGaeUtils.execute(new JpaCallback<List<Patient>>() {
            @Override
            public List<Patient> doWithEntityManager(final EntityManager em) {
                final Query query = em.createQuery("SELECT p FROM " + getPersistentClass().getName()
                    + " p WHERE p.version<:version");
                query.setParameter("version", version);
                @SuppressWarnings("unchecked")
                final List<Patient> rslt = (List<Patient>) query.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();

                return rslt;
            }
        });
    }

}
