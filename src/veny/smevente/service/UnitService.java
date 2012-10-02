package veny.smevente.service;

import java.util.List;

import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Event;
import veny.smevente.model.Patient;
import veny.smevente.model.Procedure;
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

    // ---------------------------------------------------------- Patient Stuff

    /**
     * Creates a new client in DB.
     *
     * @param client client to be created
     * @param unitId unit where the client belongs to
     * @return created client
     */
    Patient createPatient(Patient client, Object unitId);

    /**
     * Gets patient by ID.
     *
     * @param id patient ID
     * @return found patient
     */
    Patient getPatientById(Object id);

    /**
     * Gets patients by unit.
     *
     * @param unitId unit ID
     * @return list of patients in given unit
     */
    List<Patient> getPatientsByUnit(Object unitId);

    /**
     * Updates the given patient.
     *
     * @param patient patient to update
     */
    void updatePatient(Patient patient);

    /**
     * Finds patients according to given name and/or phone number and/or birth number.
     *
     * @param unitId ID to search in
     * @param name name to search
     * @param phoneNumber phone number to search
     * @param birthNumber birth number to search
     * @return list of found patients
     */
    List<Patient> findPatients(Object unitId, String name, String phoneNumber, String birthNumber);

    /**
     * Deletes patient.
     * @param id patient ID
     */
    void deletePatient(Object id);

    // -------------------------------------------------------- Procedure Stuff

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

    /**
     * Gets list of procedures for given unit.
     *
     * @param unitId ID of Unit
     * @param type type of procedure (event type) or <i>null</i> for all
     * @return list of found procedures
     */
    List<Procedure> getProceduresByUnit(Object unitId, Event.Type type);

//    /**
//     * Deletes category.
//     * @param id category ID
//     */
//    void deleteMedicalHelpCategory(Long id);

}
