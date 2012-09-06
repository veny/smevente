package veny.smevente.service;

import java.util.List;

import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Unit;

/**
 * Service API collecting methods associated to Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public interface UnitService {

    // ------------------------------------------------------------- Unit Stuff

    /**
     * Creates a new unit.
     *
     * @param unit unit to be created
     * @return created unit with generated ID
     */
    Unit createUnit(Unit unit);

    /**
     * Gets unit by given ID.
     *
     * @param id ID to search
     * @return found unit (only the unit, not the aggregated entities)
     * @throws ObjectNotFoundException if the ID doesn't exist
     */
    Unit getUnit(Object id) throws ObjectNotFoundException;

    /**
     * Loads all units.
     *
     * @return list of all units
     */
    @Deprecated // only for unit testing purposes
    List<Unit> getAllUnits();

//    // ---------------------------------------------------------- Patient Stuff
//
//    /**
//     * Creates a new patient in DB.
//     * The patient has to have an associated unit where he belongs to.
//     * The unit can be fulfilled only with an unit ID.
//     *
//     * @param patient patient
//     * @return created patient
//     */
//    PatientDto createPatient(PatientDto patient);
//
//    /**
//     * Gets patient by ID.
//     *
//     * @param patientId patient ID
//     * @return found patient
//     */
//    PatientDto getPatientById(Long patientId);
//
//    /**
//     * Updates the given patient.
//     *
//     * @param patient patient
//     */
//    void updatePatient(PatientDto patient);
//
////    /**
////     * Loads all patients.
////     *
////     * @return list of all patients
////     * @deprecated 100722
////     */
////    List<PatientDto> getAllPatients();
//
//    /**
//     * Gets patients by unit.
//     * @param unitId unit ID
//     * @return list of patients in given unit
//     */
//    List<PatientDto> getPatientsByUnit(Long unitId);
//
//    /**
//     * Finds patients according to given name and/or phone number and/or birth number.
//     * @param unitId ID to search in
//     * @param name name to search
//     * @param phoneNumber phone number to search
//     * @param birthNumber birth number to search
//     * @return list of found patients
//     */
//    List<PatientDto> findPatients(Long unitId, String name, String phoneNumber, String birthNumber);
//
//    /**
//     * Deletes patient.
//     * @param id patient ID
//     */
//    void deletePatient(Long id);
//
//    // ---------------------------------------------- MedicalHelpCategory Stuff
//
//    /**
//     * Stores a given category into DB.
//     * @param mhc category to be created
//     * @return created instance
//     */
//    MedicalHelpCategoryDto createMedicalHelpCategory(MedicalHelpCategoryDto mhc);
//
//    /**
//     * Gets category by ID.
//     *
//     * @param mhcId category ID
//     * @return found category
//     */
//    MedicalHelpCategoryDto getMedicalHelpCategoryById(Long mhcId);
//
//    /**
//     * Updates the given category.
//     *
//     * @param mhc category
//     */
//    void updateMedicalHelpCategory(MedicalHelpCategoryDto mhc);
//
//    /**
//     * Gets list of Medical Help Categories for given unit.
//     * @param unitId ID of Unit
//     * @param categoryType Type of category
//     * @return list of all Medical Help Categories
//     */
//    List<MedicalHelpCategoryDto> getMedicalHelpCategoriesByUnit(long unitId, Short categoryType);
//
//    /**
//     * Deletes category.
//     * @param id category ID
//     */
//    void deleteMedicalHelpCategory(Long id);

}
