package veny.smevente.service.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.dao.PatientDao;
import veny.smevente.dao.ProcedureDao;
import veny.smevente.dao.UnitDao;
import veny.smevente.model.Event;
import veny.smevente.model.Patient;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.server.validation.ValidationContainer;
import veny.smevente.service.UnitService;

import com.google.gwt.thirdparty.guava.common.base.Strings;

import eu.maydu.gwt.validation.client.server.ServerValidation;

/**
 * Implementation of service collecting methods associated to Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public class UnitServiceImpl implements UnitService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(UnitServiceImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UnitDao unitDao;
    /** Dependency. */
    @Autowired
    private PatientDao patientDao;
    /** Dependency. */
    @Autowired
    private ProcedureDao procedureDao;
    /** Dependency. */
    @Autowired
    private ValidationContainer validationContainer;


    // ------------------------------------------------------------- Unit Stuff

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Unit createUnit(final Unit unit) {
        validateUnit(unit, true);

        final String name = unit.getName();
        final List<Unit> check = unitDao.findBy("name", name, null);
        if (!check.isEmpty()) {
            throw new IllegalStateException("unit with given name already exists, name=" + name);
        }

//        unitGae.setMetadata(TextUtils.mapToString(unit.getMetadata()));
        final Unit rslt = unitDao.persist(unit);
        LOG.info("created new unit, name=" + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Unit getUnit(final Object id) {
        final Unit unit = unitDao.getById(id);
        LOG.info("found unit by id=" + id);
        return unit;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Unit> getAllUnits() {
        final List<Unit> found = unitDao.getAll();
        LOG.info("found all units, size=" + found.size());
        return found;
    }

    // ---------------------------------------------------------- Patient Stuff

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Patient createPatient(final Patient patient) {
        validatePatient(patient);

        // unique birth number
        if (!Strings.isNullOrEmpty(patient.getBirthNumber())) {
            final Patient bn = patientDao.findByBirthNumber(patient.getUnit().getId(), patient.getBirthNumber());
            if (null == bn) {
                // expected state <- birth number not found
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("duplicate birth number check OK, bn=" + patient.getBirthNumber());
                }
            } else {
                ServerValidation.exception("duplicateValue", "birthNumber", (Object[]) null);
            }
        }

        Patient rslt = patientDao.persist(patient);
        LOG.info("created new patient, " + rslt);
        return rslt;
    }


    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Patient getPatientById(final Object id) {
        final Patient rslt = patientDao.getById(id);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Patient> getPatientsByUnit(final Object unitId) {
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        final List<Patient> rslt = patientDao.findBy("unit", unitId, null);
        LOG.info("found " + rslt.size() + " patients(s) by unit");
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void updatePatient(final Patient patient) {
        if (null == patient.getId()) { throw new NullPointerException("ID cannot be null"); }
        validatePatient(patient);

        // unique birth number
        if (!Strings.isNullOrEmpty(patient.getBirthNumber())) {
            final Patient bn = patientDao.findByBirthNumber(patient.getUnit().getId(), patient.getBirthNumber());
            if (null == bn || bn.getId().equals(patient.getId())) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("duplicite birth number check OK, bn=" + patient.getBirthNumber());
                }
            } else {
                ServerValidation.exception("duplicateValue", "birthNumber", (Object[]) null);
            }
        }

        patientDao.persist(patient);
        LOG.info("patient updated, id=" + patient.getId()
                + ", fisrtname=" + patient.getFirstname() + ", surname=" + patient.getSurname());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Patient> findPatients(
            final Object unitId, final String name, final String phoneNumber, final String birthNumber) {

        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        LOG.info("findPatients, unitId=" + unitId + ", name=" + name
                + ", phone=" + phoneNumber + ", birthNumber=" + birthNumber);

        if (null == name && null == phoneNumber && null == birthNumber) {
            return getPatientsByUnit(unitId);
        }

        List<Patient> collectedRslt = null;

        // name
        if (null != name) {
            collectedRslt = patientDao.findLikeBy(unitId, "firstname", name);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("patient(s) found by first name, size=" + collectedRslt.size());
            }
            List<Patient> found = patientDao.findLikeBy(unitId, "surname", name);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("patient(s) found by surname, size=" + found.size());
            }
            collectedRslt = (List<Patient>) CollectionUtils.union(collectedRslt, found);
        }

        // phone number
        if (null != phoneNumber) {
            final List<Patient> found = patientDao.findLikeBy(unitId, "phoneNumber", phoneNumber);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("patient(s) found by phone number, size=" + found.size());
            }
            if (null == collectedRslt) {
                collectedRslt = found;
            } else {
                collectedRslt = (List<Patient>) CollectionUtils.intersection(collectedRslt, found);
            }
        }

        // birth number
        if (null != birthNumber) {
            final List<Patient> found = patientDao.findLikeBy(unitId, "birthNumber", birthNumber);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("patient(s) found by birth number, size=" + found.size());
            }
            if (null == collectedRslt) {
                collectedRslt = found;
            } else {
                collectedRslt = (List<Patient>) CollectionUtils.intersection(collectedRslt, found);
            }
        }

        LOG.info("patient(s) found, size=" + collectedRslt.size());
        return collectedRslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void deletePatient(final Object id) {
        patientDao.remove(id);
        LOG.info("patient deleted, id=" + id);
    }

    // -------------------------------------------------------- Procedure Stuff

//    /** {@inheritDoc} */
//    @Transactional
//    @Override
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    public MedicalHelpCategoryDto createMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
//        // load the unit (validation at the same time that the unit exist)
//        final Unit unit = getById(mhc.getUnit().getId());
//
//        validateMedicalHelpCategory(mhc);
//
//        // unique name and type
//        boolean uniqueOk = false;
//        try {
//            final MedicalHelpCategory found =
//                mhcDao.findByNameAndType(mhc.getUnit().getId(), mhc.getName(), mhc.getType());
//            uniqueOk = found == null;
//        } catch (IllegalStateException e) {
//            // expected state <- combination of name and type not found
//            uniqueOk = true;
//        }
//        if (!uniqueOk) {
//            ServerValidation.exception("duplicateValue", "name", (Object[]) null);
//        }
//        final MedicalHelpCategory mhcGae = MedicalHelpCategory.mapFromDto(mhc);
//        mhcGae.setUnitId(mhc.getUnit().getId());
//        mhcDao.persist(mhcGae);
//
//        final MedicalHelpCategoryDto rslt = mhcGae.mapToDto();
//        rslt.setUnit(unit);
//        LOG.info("created new category, " + rslt);
//        return rslt;
//    }
//
//    /** {@inheritDoc} */
//    @Transactional
//    @Override
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    public void updateMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
//        if (null == mhc.getId()) { throw new NullPointerException("ID cannot be null"); }
//        validateMedicalHelpCategory(mhc);
//
//        // unique name and type
//        if (null != mhc.getName() && mhc.getName().trim().length() > 0) {
//            boolean nameOk = false;
//            try {
//                final MedicalHelpCategory found = mhcDao.findByNameAndType(
//                        mhc.getUnit().getId(), mhc.getName(), mhc.getType());
//                nameOk = (found == null || found.getId().equals(mhc.getId()));
//            } catch (IllegalStateException e) {
//                // expected state <- combination of name and type not found
//                nameOk = true;
//            }
//            if (nameOk) {
//                if (LOG.isLoggable(Level.FINER)) {
//                    LOG.finer("duplicite name check OK, name=" + mhc.getName());
//                }
//            } else {
//                ServerValidation.exception("duplicateValue", "name", (Object[]) null);
//            }
//        }
//
//        final MedicalHelpCategory mhcGae = mhcDao.getById(mhc.getId());
//        MedicalHelpCategory.mapFromDto(mhc, mhcGae);
//        mhcDao.persist(mhcGae);
//        LOG.info("category updated, id=" + mhc.getId()
//                + ", name=" + mhc.getName());
//    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Procedure> getProceduresByUnit(final Object unitId, final Event.Type type) {
        final List<Procedure> rslt = procedureDao.findByType(unitId, type, "name");
        LOG.info("found proceduress, unitId=" + unitId + ", size=" + rslt.size());
        return rslt;
    }

//    /** {@inheritDoc} */
//    @Transactional(readOnly = true)
//    @Override
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    public MedicalHelpCategoryDto getMedicalHelpCategoryById(final Long mhcId) {
//        final MedicalHelpCategory mhcGae = mhcDao.getById(mhcId);
//        final Unit unitGae = unitDao.getById(mhcGae.getUnitId());
//        final MedicalHelpCategoryDto rslt = mhcGae.mapToDto();
//        rslt.setUnit(unitGae.mapToDto());
//        return rslt;
//    }
//
//    /** {@inheritDoc} */
//    @Transactional
//    @Override
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    public void deleteMedicalHelpCategory(final Long id) {
//        mhcDao.remove(id);
//        LOG.info("category deleted, id=" + id);
//    }
//
    // -------------------------------------------------------- Assistant Stuff

    /**
     * Validation of a unit before persistence.
     * @param unit unit to be validated
     * @param forCreate whether the object has to be created as new entry in DB
     */
    private void validateUnit(final Unit unit, final boolean forCreate) {
        if (null == unit) { throw new NullPointerException("unit cannot be null"); }
        if (Strings.isNullOrEmpty(unit.getName())) { throw new IllegalArgumentException("unit name cannot be blank"); }
        if (forCreate) {
            if (null != unit.getId()) {
                throw new IllegalArgumentException("expected object with empty ID");
            }
        }
    }

    /**
     * Validation of a patient before persistence.
     * @param patient patient to be validated
     */
    private void validatePatient(final Patient patient) {
        if (null == patient) { throw new NullPointerException("patient cannot be null"); }
        if (null == patient.getUnit()) { throw new NullPointerException("unit cannot be null"); }
        if (null == patient.getUnit().getId()) { throw new NullPointerException("unit ID cannot be null"); }
        if (null == patient.getFirstname()) { throw new NullPointerException("first name cannot be null"); }
        if (null == patient.getSurname()) { throw new NullPointerException("surname cannot be null"); }

        // birth number
        if (null == patient.getBirthNumber() || 0 == patient.getBirthNumber().trim().length()) {
            LOG.warning("birth number is empty, fisrtname=" + patient.getFirstname()
                    + ", surname=" + patient.getSurname());
        } else {
            validationContainer.validate("birthNumber", "birthNumber", patient.getBirthNumber());
        }
        // phone number
        if (null == patient.getPhoneNumber() || 0 == patient.getPhoneNumber().trim().length()) {
            LOG.warning("phone number is empty, fisrtname=" + patient.getFirstname()
                    + ", surname=" + patient.getSurname());
        } else {
            validationContainer.validate("phoneNumber", "phoneNumber", patient.getPhoneNumber());
        }
    }

//    /**
//     * Validation of a category before persistence.
//     * @param mhc category to be validated
//     */
//    private void validateMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
//        if (null == mhc.getUnit()) { throw new NullPointerException("unit cannot be null"); }
//        if (null == mhc.getUnit().getId()) { throw new NullPointerException("unit ID cannot be null"); }
//        if (mhc.getUnit().getId() <= 0) { throw new IllegalArgumentException("unit ID must be more than 0"); }
//        if (null == mhc.getName()) { throw new NullPointerException("name cannot be null"); }
//        if (null == mhc.getSmsText()) { throw new NullPointerException("SMS text cannot be null"); }
//        if (null == mhc.getType() || mhc.getType() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            if (null == mhc.getColor()) { throw new NullPointerException("color cannot be null"); }
//            if (6 != mhc.getColor().length()) { throw new IllegalArgumentException("bad color format"); }
//            if (mhc.getTime() <= 0) { throw new IllegalArgumentException("bad time"); }
//        }
//    }

}
