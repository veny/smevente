package veny.smevente.service;

import java.util.List;

import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Event;
import veny.smevente.model.Customer;
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
     * Stores (creates or updates) a patient.<p/>
     * The criterion to decide if create or update is entity's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param client client to be created
     * @return created client
     */
    Customer storePatient(Customer client);

    /**
     * Gets patient by ID.
     *
     * @param id patient ID
     * @return found patient
     */
    Customer getPatientById(Object id);

    /**
     * Gets patients by unit.
     *
     * @param unitId unit ID
     * @return list of patients in given unit
     */
    List<Customer> getPatientsByUnit(Object unitId);

    /**
     * Finds patients according to given name and/or phone number and/or birth number.
     *
     * @param unitId ID to search in
     * @param name name to search
     * @param phoneNumber phone number to search
     * @param birthNumber birth number to search
     * @return list of found patients
     */
    List<Customer> findPatients(Object unitId, String name, String phoneNumber, String birthNumber);

    /**
     * Deletes patient.
     * @param id patient ID
     */
    void deletePatient(Object id);

    // -------------------------------------------------------- Procedure Stuff

    /**
     * Stores (creates or updates) a procedure.<p/>
     * The criterion to decide if create or update is entity's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param procedure procedure to be created
     * @return created instance
     */
    Procedure storeProcedure(Procedure procedure);

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

    /**
     * Deletes given procedure by ID.
     *
     * @param id procedure ID
     */
    void deleteProcedure(Object id);

}
