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
     * Stores (creates or updates) an unit.<p/>
     * The criterion to decide if create or update is entity's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param unit unit to be stored
     * @return stored unit with generated ID if created
     */
    Unit storeUnit(Unit unit);

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
    List<Unit> getAllUnits();

    // --------------------------------------------------------- Customer Stuff

    /**
     * Stores (creates or updates) a customer.<p/>
     * The criterion to decide if create or update is entity's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param client client to be created
     * @return created client
     */
    Customer storeCustomer(Customer client);

    /**
     * Gets customer by ID.
     *
     * @param id customer ID
     * @return found customer
     */
    Customer getCustomerById(Object id);

    /**
     * Gets customers by unit.
     *
     * @param unitId unit ID
     * @return list of customers in given unit
     */
    List<Customer> getCustomersByUnit(Object unitId);

    /**
     * Finds customers according to given name and/or phone number and/or birth number.
     *
     * @param unitId ID to search in
     * @param name name to search
     * @param phoneNumber phone number to search
     * @param birthNumber birth number to search
     * @return list of found customers
     */
    List<Customer> findCustomers(Object unitId, String name, String phoneNumber, String birthNumber);

    /**
     * Deletes customer.
     * @param id customer ID
     */
    void deleteCustomer(Object id);

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
