package veny.smevente.service.gae;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.dao.jpa.gae.MedicalHelpCategoryDaoGae;
import veny.smevente.dao.jpa.gae.PatientDaoGae;
import veny.smevente.dao.jpa.gae.UnitDaoGae;
import veny.smevente.model.MedicalHelpCategoryDto;
import veny.smevente.model.PatientDto;
import veny.smevente.model.UnitDto;
import veny.smevente.model.gae.MedicalHelpCategory;
import veny.smevente.model.gae.Patient;
import veny.smevente.model.gae.Unit;
import veny.smevente.server.validation.ValidationContainer;
import veny.smevente.service.TextUtils;
import veny.smevente.service.UnitService;
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
    private UnitDaoGae unitDao;
    /** Dependency. */
    @Autowired
    private PatientDaoGae patientDao;
    /** Dependency. */
    @Autowired
    private MedicalHelpCategoryDaoGae mhcDao;
    /** Dependency. */
    @Autowired
    private ValidationContainer validationContainer;


    // ------------------------------------------------------------- Unit Stuff

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public UnitDto createUnit(final UnitDto unit) {
        if (null == unit) { throw new NullPointerException("unit cannot be null"); }
        if (null == unit.getName()) { throw new NullPointerException("unit name cannot be null"); }
        if (null == unit.getMetadata()) { throw new NullPointerException("unit metadata cannot be null"); }
        if (unit.getMetadata().isEmpty()) { throw new IllegalArgumentException("unit metadata cannot be empty"); }

        final String name = unit.getName();
        final List<Unit> check = unitDao.findBy("name", name, null);
        if (!check.isEmpty()) {
            throw new IllegalStateException("unit with given name already exists, name=" + name);
        }

        final Unit unitGae = new Unit();
        unitGae.setName(name);
        unitGae.setMetadata(TextUtils.mapToString(unit.getMetadata()));
        unitGae.setLimitedSmss(unit.getLimitedSmss());
        unitDao.persist(unitGae);

        final UnitDto rslt = unitGae.mapToDto();
        LOG.info("created new unit, name=" + name);

        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public UnitDto getById(final Long id) {
        final Unit unitGae = unitDao.getById(id);
        return unitGae.mapToDto();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<UnitDto> getAllUnits() {
        final List<Unit> found = unitDao.getAll();
        LOG.info("found all units, size=" + found.size());

        final List<UnitDto> rslt = new ArrayList<UnitDto>();
        for (Unit userGae : found) {
            rslt.add(userGae.mapToDto());
        }
        return rslt;
    }

    // ---------------------------------------------------------- Patient Stuff

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public PatientDto createPatient(final PatientDto patient) {
        validatePatient(patient);
        final UnitDto unit = getById(patient.getUnit().getId());
        final Patient patientGae = Patient.mapFromDto(patient);
        patientGae.setUnitId(unit.getId());

        // unique birth number
        if (null != patient.getBirthNumber() && patient.getBirthNumber().trim().length() > 0) {
            try {
                patientDao.findByBirthNumber(unit.getId(), patient.getBirthNumber().trim());
                ServerValidation.exception("duplicateValue", "birthNumber", (Object[]) null);
            } catch (IllegalStateException e) {
                // expected state <- birth number not found
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("duplicate birth number check OK, bn=" + patient.getBirthNumber());
                }
            }
        }

        patientDao.persist(patientGae);
        final PatientDto rslt = patientGae.mapToDto();
        rslt.setUnit(unit);
        LOG.info("created new patient, " + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void updatePatient(final PatientDto patient) {
        if (null == patient.getId()) { throw new NullPointerException("ID cannot be null"); }
        validatePatient(patient);

        // unique birth number
        if (null != patient.getBirthNumber() && patient.getBirthNumber().trim().length() > 0) {
            boolean birthNumberOk = false;
            try {
                final Patient found =
                    patientDao.findByBirthNumber(patient.getUnit().getId(), patient.getBirthNumber().trim());
                birthNumberOk = (found.getId().equals(patient.getId()));
            } catch (IllegalStateException e) {
                // expected state <- birth number not found
                birthNumberOk = true;
            }
            if (birthNumberOk) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("duplicite birth number check OK, bn=" + patient.getBirthNumber());
                }
            } else {
                ServerValidation.exception("duplicateValue", "birthNumber", (Object[]) null);
            }
        }

        final Patient patientGae = patientDao.getById(patient.getId());
        Patient.mapFromDto(patient, patientGae);
        patientDao.persist(patientGae);
        LOG.info("patient updated, id=" + patient.getId()
                + ", fisrtname=" + patient.getFirstname() + ", surname=" + patient.getSurname());
    }

//    /** {@inheritDoc} */
//    @Transactional(readOnly = true)
//    @Override
//    public List<PatientDto> getAllPatients() {
//        final List<PatientGae> found = patientDao.getAll();
//        LOG.info("found " + found.size() + " patients(s)");
//
//        final List<PatientDto> rslt = new ArrayList<PatientDto>();
//        for (PatientGae p : found) {
//            rslt.add(beanMapper.map(p, PatientDto.class));
//        }
//        return rslt;
//    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<PatientDto> getPatientsByUnit(final Long unitId) {
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        final UnitDto unit = getById(unitId);
        final List<Patient> found = patientDao.findBy("unitId", unitId, null);
        LOG.info("found " + found.size() + " patients(s) by unit");

        final List<PatientDto> rslt = new ArrayList<PatientDto>();
        for (Patient p : found) {
            final PatientDto patient = p.mapToDto();
            patient.setUnit(unit);
            rslt.add(patient);
        }
        LOG.info("returned patients, size=" + rslt.size());
        return rslt;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<PatientDto> findPatients(
            final Long unitId, final String name, final String phoneNumber, final String birthNumber) {

        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        final UnitDto unit = getById(unitId);
        LOG.info("findPatients, unitId=" + unitId + ", name=" + name
                + ", phone=" + phoneNumber + ", birthNumber=" + birthNumber);

        if (null == name && null == phoneNumber && null == birthNumber) {
            return getPatientsByUnit(unitId);
        }

        List<Patient> collectedRslt = null;

        // name
        if (null != name) {
            collectedRslt = patientDao.findByFirstname(unitId, name);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("patient(s) found by first name, size=" + collectedRslt.size());
            }
            List<Patient> found = patientDao.findBySurname(unitId, name);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("patient(s) found by surname, size=" + found.size());
            }
            collectedRslt = (List<Patient>) CollectionUtils.union(collectedRslt, found);
        }

        // phone number
        if (null != phoneNumber) {
            final List<Patient> found = patientDao.findByPhoneNumber(unitId, phoneNumber);
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
            final List<Patient> found = patientDao.findByBirthNumberPrefix(unitId, birthNumber);
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
        final List<PatientDto> rslt = new ArrayList<PatientDto>();
        for (Patient p : collectedRslt) {
            final PatientDto patient = p.mapToDto();
            patient.setUnit(unit);
            rslt.add(patient);
        }
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public PatientDto getPatientById(final Long patientId) {
        final Patient patientGae = patientDao.getById(patientId);
        final Unit unitGae = unitDao.getById(patientGae.getUnitId());
        final PatientDto rslt = patientGae.mapToDto();
        rslt.setUnit(unitGae.mapToDto());
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void deletePatient(final Long id) {
        patientDao.remove(id);
        LOG.info("patient deleted, id=" + id);
    }

    // ---------------------------------------------- MedicalHelpCategory Stuff

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public MedicalHelpCategoryDto createMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
        // load the unit (validation at the same time that the unit exist)
        final UnitDto unit = getById(mhc.getUnit().getId());

        validateMedicalHelpCategory(mhc);

        // unique name and type
        boolean uniqueOk = false;
        try {
            final MedicalHelpCategory found =
                mhcDao.findByNameAndType(mhc.getUnit().getId(), mhc.getName(), mhc.getType());
            uniqueOk = found == null;
        } catch (IllegalStateException e) {
            // expected state <- combination of name and type not found
            uniqueOk = true;
        }
        if (!uniqueOk) {
            ServerValidation.exception("duplicateValue", "name", (Object[]) null);
        }
        final MedicalHelpCategory mhcGae = MedicalHelpCategory.mapFromDto(mhc);
        mhcGae.setUnitId(mhc.getUnit().getId());
        mhcDao.persist(mhcGae);

        final MedicalHelpCategoryDto rslt = mhcGae.mapToDto();
        rslt.setUnit(unit);
        LOG.info("created new category, " + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void updateMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
        if (null == mhc.getId()) { throw new NullPointerException("ID cannot be null"); }
        validateMedicalHelpCategory(mhc);

        // unique name and type
        if (null != mhc.getName() && mhc.getName().trim().length() > 0) {
            boolean nameOk = false;
            try {
                final MedicalHelpCategory found = mhcDao.findByNameAndType(
                        mhc.getUnit().getId(), mhc.getName(), mhc.getType());
                nameOk = (found == null || found.getId().equals(mhc.getId()));
            } catch (IllegalStateException e) {
                // expected state <- combination of name and type not found
                nameOk = true;
            }
            if (nameOk) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("duplicite name check OK, name=" + mhc.getName());
                }
            } else {
                ServerValidation.exception("duplicateValue", "name", (Object[]) null);
            }
        }

        final MedicalHelpCategory mhcGae = mhcDao.getById(mhc.getId());
        MedicalHelpCategory.mapFromDto(mhc, mhcGae);
        mhcDao.persist(mhcGae);
        LOG.info("category updated, id=" + mhc.getId()
                + ", name=" + mhc.getName());
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<MedicalHelpCategoryDto> getMedicalHelpCategoriesByUnit(final long unitId, final Short categoryType) {
        // load the unit (validation at the same time that the unit exist)
        final UnitDto unit = getById(unitId);

        final List<MedicalHelpCategory> found = mhcDao.findByType(unitId, categoryType, "name");
        LOG.info("found MHCs, unitId=" + unitId + ", size=" + found.size());

        final List<MedicalHelpCategoryDto> rslt = new ArrayList<MedicalHelpCategoryDto>();
        for (MedicalHelpCategory mhcGae : found) {
            final MedicalHelpCategoryDto mhcDto = mhcGae.mapToDto();
            mhcDto.setUnit(unit);
            rslt.add(mhcDto);
        }
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public MedicalHelpCategoryDto getMedicalHelpCategoryById(final Long mhcId) {
        final MedicalHelpCategory mhcGae = mhcDao.getById(mhcId);
        final Unit unitGae = unitDao.getById(mhcGae.getUnitId());
        final MedicalHelpCategoryDto rslt = mhcGae.mapToDto();
        rslt.setUnit(unitGae.mapToDto());
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void deleteMedicalHelpCategory(final Long id) {
        mhcDao.remove(id);
        LOG.info("category deleted, id=" + id);
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Validation of a patient before persistence.
     * @param patient patient to be validated
     */
    private void validatePatient(final PatientDto patient) {
        if (null == patient.getUnit()) { throw new NullPointerException("unit cannot be null"); }
        if (null == patient.getUnit().getId()) { throw new NullPointerException("unit ID cannot be null"); }
        if (patient.getUnit().getId() <= 0) { throw new IllegalArgumentException("unit ID must be more than 0"); }
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

    /**
     * Validation of a category before persistence.
     * @param mhc category to be validated
     */
    private void validateMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
        if (null == mhc.getUnit()) { throw new NullPointerException("unit cannot be null"); }
        if (null == mhc.getUnit().getId()) { throw new NullPointerException("unit ID cannot be null"); }
        if (mhc.getUnit().getId() <= 0) { throw new IllegalArgumentException("unit ID must be more than 0"); }
        if (null == mhc.getName()) { throw new NullPointerException("name cannot be null"); }
        if (null == mhc.getSmsText()) { throw new NullPointerException("SMS text cannot be null"); }
        if (null == mhc.getType() || mhc.getType() == MedicalHelpCategoryDto.TYPE_STANDARD) {
            if (null == mhc.getColor()) { throw new NullPointerException("color cannot be null"); }
            if (6 != mhc.getColor().length()) { throw new IllegalArgumentException("bad color format"); }
            if (mhc.getTime() <= 0) { throw new IllegalArgumentException("bad time"); }
        }
    }

}
