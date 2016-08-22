package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import veny.smevente.AbstractBaseTest;
import veny.smevente.dao.DeletedObjectException;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Event;
import veny.smevente.model.Customer;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;

import com.orientechnologies.orient.core.id.ORecordId;

import eu.maydu.gwt.validation.client.ValidationException;

/**
 * Test of <code>UnitService</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
public class UnitServiceTest extends AbstractBaseTest {

    // ------------------------------------------------------------- Unit Stuff

    /** UnitService.createUnit. */
    @Test
    public void testCreateUnit() {
        final Unit firstUnit = createDefaultUnit();
        final List<Unit> found = unitService.getAllUnits();
        assertEquals(1, found.size());
        assertDefaultUnit(found.get(0));

        final Unit secondUnit = createUnit("A", "desc", Unit.TextVariant.PATIENT, 10L, null);
        assertNotNull(secondUnit.getId());
        assertEquals("A", secondUnit.getName());
        assertEquals(new Long(10), secondUnit.getMsgLimit());

        assertFalse(firstUnit.getId().equals(secondUnit.getId()));
        assertEquals(2, unitService.getAllUnits().size());

        // Validation
        Unit toCreate = new Unit();
        try { // existing unit name
            toCreate.setName("A");
            unitService.storeUnit(toCreate);
            assertEquals("expected IllegalStateException", true, false);
        } catch (IllegalStateException e) { assertEquals(true, true); }

        // SOFT DELETE
        // impossible to delete unit now
    }

    /** UnitService.getUnit. */
    @Test
    public void testGetById() {
        final Unit firstCreated = createDefaultUnit();
        final Unit firstFound = unitService.getUnit(firstCreated.getId());
        assertEquals(firstCreated.getId(), firstFound.getId());
        assertDefaultUnit(firstFound);

        final Unit secondCreated = createUnit("A", "desc", Unit.TextVariant.PATIENT, 0L, "{}");
        final Unit secondFound = unitService.getUnit(secondCreated.getId());
        assertEquals(secondCreated.getId(), secondFound.getId());
        assertEquals("A", secondFound.getName());

        try { // invalid ID class
            unitService.getUnit("xx");
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
        try { // invalid ID
            unitService.getUnit(new ORecordId("#1001:123456789"));
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
    }

    // --------------------------------------------------------- Customer Stuff

    /** UnitService.storeCustomer (create). */
    @Test // unit/customer/
    public void testStoreCreatePatient() {
        final Unit unit = createDefaultUnit();

        // first patient in the first unit
        final Customer toCreate = new Customer();
        toCreate.setFirstname(FIRSTNAME);
        toCreate.setSurname(SURNAME);
        toCreate.setPhoneNumber(PHONE_NUMBER);
        toCreate.setBirthNumber(BIRTH_NUMBER);
        toCreate.setDegree("degree");
        toCreate.setStreet("street");
        toCreate.setCity("city");
        toCreate.setZipCode("zip code");
        toCreate.setEmployer("employer");
        toCreate.setCareers("careers");
        toCreate.setUnitId(unit.getId());

        final Customer firstCreated = unitService.storeCustomer(toCreate);
        assertDefaultPatient(firstCreated, true);
        assertEquals(unit.getId(), firstCreated.getUnit().getId());
        assertEquals("degree", firstCreated.getDegree());
        assertEquals("street", firstCreated.getStreet());
        assertEquals("city", firstCreated.getCity());
        assertEquals("zip code", firstCreated.getZipCode());
        assertEquals("employer", firstCreated.getEmployer());
        assertEquals("careers", firstCreated.getCareers());
        assertEquals(1, unitService.getCustomersByUnit(unit.getId()).size());

        // second customer in the first unit
        final Customer secondCreated = createCustomer("a", "b", null, null, unit);
        assertNotNull(secondCreated);
        assertNotNull(secondCreated.getId());
        assertNotNull(secondCreated.getUnit());
        assertNotNull(secondCreated.getUnit().getId());
        assertEquals(unit.getId(), secondCreated.getUnit().getId());
        assertEquals("a", secondCreated.getFirstname());
        assertEquals("b", secondCreated.getSurname());
        assertNull(secondCreated.getPhoneNumber());
        assertNull(secondCreated.getBirthNumber());
        assertEquals(2, unitService.getCustomersByUnit(unit.getId()).size());

        final Customer badPatient = new Customer();
        badPatient.setFirstname("aa");
        badPatient.setSurname("bb");
        badPatient.setBirthNumber(BIRTH_NUMBER);
        badPatient.setUnitId(unit.getId());
        try { // existing birth number
            unitService.storeCustomer(badPatient);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }

        // second unit (I can create user with Birth Number in other unit)
        final Unit secondUnit = createUnit("A", "desc", Unit.TextVariant.PATIENT, 10L, null);
        badPatient.setUnitId(secondUnit.getId());
        final Customer thirdCreated = unitService.storeCustomer(badPatient);
        assertEquals(secondUnit.getId(), thirdCreated.getUnit().getId());
        assertEquals(BIRTH_NUMBER, thirdCreated.getBirthNumber());
        assertEquals(2, unitService.getCustomersByUnit(unit.getId()).size());
        assertEquals(1, unitService.getCustomersByUnit(secondUnit.getId()).size());

        // validation - birth number
        final Customer validation = new Customer();
        validation.setFirstname("a");
        validation.setSurname("a");
        validation.setBirthNumber("12345678");
        validation.setUnitId(unit.getId());
        try { // short birth number
            unitService.storeCustomer(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        validation.setBirthNumber("12345678901");
        try { // long birth number
            unitService.storeCustomer(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        // OK
        validation.setBirthNumber("1234567890");
        unitService.storeCustomer(validation);

        // validation - phone number
        validation.setBirthNumber(null);
        validation.setPhoneNumber("12345678");
        try { // short phone number
            unitService.storeCustomer(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        validation.setPhoneNumber("12345678901x");
        try { // phone number not a number
            unitService.storeCustomer(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        // OK
        validation.setPhoneNumber("123456789");
        unitService.storeCustomer(validation);
    }

    /** UnitService.storeCustomer (update). */
    @Test // unit/patient/
    public void testStoreUpdateCustomer() {
        final Customer created = createDefaultCustomer();
        assertNull(created.getCity());
        assertNull(created.getDegree());

        created.setCity("city");
        unitService.storeCustomer(created);
        Customer found = unitService.getCustomerById(created.getId());
        assertDefaultPatient(found, true);
        assertEquals("city", found.getCity());
        assertNull(created.getDegree());

        created.setDegree("degree");
        unitService.storeCustomer(created);
        found = unitService.getCustomerById(created.getId());
        assertDefaultPatient(found, true);
        assertEquals("city", found.getCity());
        assertEquals("degree", created.getDegree());

        // phone + birth number
        created.setPhoneNumber("987987987");
        created.setBirthNumber("789789789");
        unitService.storeCustomer(created);
        found = unitService.getCustomerById(created.getId());
        assertEquals("987987987", created.getPhoneNumber());
        assertEquals("789789789", found.getBirthNumber());
    }

    /** UnitService.getCustomersByUnit. */
    @Test // unit/{id}/info/
    public void testGetCustomersByUnit() {
        final Customer created = createDefaultCustomer();
        assertNotNull(created.getUnit().getId());
        List<Customer> found = unitService.getCustomersByUnit(created.getUnit().getId());
        assertNotNull(found);
        assertEquals(1, found.size());
        assertDefaultPatient(found.get(0), true);

        createCustomer("a", "a", null, null, created.getUnit());
        found = unitService.getCustomersByUnit(created.getUnit().getId());
        assertEquals(2, found.size());
        assertDefaultUnit(found.get(0).getUnit());
        assertDefaultUnit(found.get(1).getUnit());

        createCustomer("b", "b", null, null, created.getUnit());
        found = unitService.getCustomersByUnit(created.getUnit().getId());
        assertEquals(3, found.size());
        assertDefaultUnit(found.get(0).getUnit());
        assertDefaultUnit(found.get(1).getUnit());
        assertDefaultUnit(found.get(2).getUnit());

        final Unit secondUnit = createUnit("X", "desc", Unit.TextVariant.PATIENT, 0L, null);
        final Customer c = createCustomer("c", "c", null, null, secondUnit);
        found = unitService.getCustomersByUnit(secondUnit.getId());
        assertEquals(1, found.size());
        assertEquals(secondUnit.getId(), found.get(0).getUnit().getId());
        assertEquals(3, unitService.getCustomersByUnit(created.getUnit().getId()).size());

        // SOFT DELETE
        // first unit
        unitService.deleteCustomer(created.getId());
        assertEquals(2, unitService.getCustomersByUnit(created.getUnit().getId()).size());
        // second unit
        unitService.deleteCustomer(c.getId());
        assertEquals(0, unitService.getCustomersByUnit(c.getUnit().getId()).size());
    }

    /** UnitService.findCustomers. */
    @Test // unit/{id}/patient/
    public void testFindCustomers() {
        final Unit unit = createDefaultUnit();
        assertTrue(unitService.findCustomers(unit.getId(), null, null, null).isEmpty());
        final Customer adam = createCustomer("Adam", "Bláha", "000000000", "7001011111", unit);
        final Customer vaclav = createCustomer("Václav", "Sýkora", "011111111", "7001022222", unit);
        createCustomer("John", "Žluťoučký", "012222222", "7003033333", unit);
        createCustomer("Robert", "Kůň", "012333333", "7004044444", unit);
        createCustomer("Norbert", "Kuře", "012344444", "7005055555", unit);
        createCustomer("Gábina", "Buližníková", "012345555", "7006066666", unit);
        createCustomer("Žulet", "Světlíková", "012345666", "7007077777", unit);
        createCustomer("Šón", "Ďáblík", "012345677", "7008088889", unit);

        // by name
        assertEquals(1, unitService.findCustomers(unit.getId(), "ADAM", null, null).size()); // Blaha
        assertEquals(1, unitService.findCustomers(unit.getId(), "blaha", null, null).size()); // Blaha
        assertEquals(3, unitService.findCustomers(unit.getId(), "S", null, null).size()); // Sykora,Svetlikova,Dablik
        assertEquals(2, unitService.findCustomers(unit.getId(), "ova", null, null).size()); // Buliznikova,Svetlikova
        assertEquals(3, unitService.findCustomers(unit.getId(), "li", null, null).size()); // Buliznikova,Dablik
        assertEquals(0, unitService.findCustomers(unit.getId(), "x", null, null).size());

        // by phone number
        assertEquals(8, unitService.findCustomers(unit.getId(), null, "0", null).size());
        assertEquals(4, unitService.findCustomers(unit.getId(), null, "01234", null).size());
        assertEquals(1, unitService.findCustomers(unit.getId(), null, "012345677", null).size());
        assertEquals(0, unitService.findCustomers(unit.getId(), null, "9", null).size());
        assertEquals(2, unitService.findCustomers(unit.getId(), null, "56", null).size());
        assertEquals(1, unitService.findCustomers(unit.getId(), null, "77", null).size());

        // by birth number
        assertEquals(8, unitService.findCustomers(unit.getId(), null, null, "70").size());
        assertEquals(2, unitService.findCustomers(unit.getId(), null, null, "7001").size());
        assertEquals(1, unitService.findCustomers(unit.getId(), null, null, "7003").size());
        assertEquals(1, unitService.findCustomers(unit.getId(), null, null, "88").size());
        assertEquals(1, unitService.findCustomers(unit.getId(), null, null, "89").size());
        assertEquals(0, unitService.findCustomers(unit.getId(), null, null, "123").size());

        // combination
        assertEquals(8, unitService.findCustomers(unit.getId(), null, null, null).size());
        assertEquals(3, unitService.findCustomers(unit.getId(), "s", null, "70").size()); // Sykora,Svetlikova,Dablik
        assertEquals(2, unitService.findCustomers(unit.getId(), "s", "0123456", "70").size()); // Svetlikova,Dablik
        assertEquals(1, unitService.findCustomers(unit.getId(), "S", "01234567", null).size()); // Dablik
        assertEquals(0, unitService.findCustomers(unit.getId(), "S", "01", "123").size());

        // SOFT DELETE
        unitService.deleteCustomer(adam.getId());
        assertEquals(0, unitService.findCustomers(unit.getId(), "ADAM", null, null).size()); // Blaha
        assertEquals(0, unitService.findCustomers(unit.getId(), "blaha", null, null).size()); // Blaha
        unitService.deleteCustomer(vaclav.getId());
        assertEquals(0, unitService.findCustomers(unit.getId(), null, "011111111", null).size());
        assertEquals(0, unitService.findCustomers(unit.getId(), null, null, "7001022222").size());
    }

    /** UnitService.deleteCustomer. */
    @Test // unit/patient/{id}/
    public void testDeleteCustomer() {
        final Customer firstCreated = createDefaultCustomer();
        final Unit unit = firstCreated.getUnit();
        final Customer secondCreated = createCustomer("a", "a", null, null, unit);
        assertEquals(2, unitService.getCustomersByUnit(unit.getId()).size());

        // delete first
        unitService.deleteCustomer(firstCreated.getId());
        try {
            unitService.getCustomerById(firstCreated.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }
        List<Customer> found = unitService.getCustomersByUnit(unit.getId());
        assertEquals(1, found.size());
        assertEquals(secondCreated.getId(), found.get(0).getId());

        final Customer thirdCreated = createCustomer("b", "b", null, null, unit);
        assertEquals(2, unitService.getCustomersByUnit(unit.getId()).size());
        unitService.deleteCustomer(firstCreated.getId()); // DO NOTHING
        unitService.deleteCustomer(secondCreated.getId());
        try {
            unitService.getCustomerById(secondCreated.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }
        found = unitService.getCustomersByUnit(unit.getId());
        assertEquals(1, found.size());
        assertEquals(thirdCreated.getId(), found.get(0).getId());
        unitService.deleteCustomer(thirdCreated.getId());

        unitService.deleteCustomer(thirdCreated.getId());
        try {
            unitService.getCustomerById(thirdCreated.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }
        found = unitService.getCustomersByUnit(unit.getId());
        assertEquals(0, found.size());
    }


    // -------------------------------------------------------- Procedure Stuff

    /** UnitService.storeProcedure (create). */
    @Test // unit/procedure/
    public void testStoreCreateProcedure() {
        final Unit unit = createDefaultUnit();

        // first procedure in the first unit
        final Procedure firstCreated = createProcedure(
                PROCEDURE_NAME,  PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT, Event.Type.IN_CALENDAR, unit);
        assertDefaultProcedure(firstCreated, Event.Type.IN_CALENDAR, true);

        // second procedure in the first unit
        final Procedure secondCreated = createProcedure("a", "aabbcc", 10, "text", Event.Type.IN_CALENDAR, unit);
        assertNotNull(secondCreated);
        assertNotNull(secondCreated.getId());
        assertNotNull(secondCreated.getUnit());
        assertNotNull(secondCreated.getUnit().getId());
        assertEquals(unit.getId(), secondCreated.getUnit().getId());
        assertEquals("a", secondCreated.getName());
        assertEquals("aabbcc", secondCreated.getColor());
        assertEquals(10, secondCreated.getTime());
        assertEquals("text", secondCreated.getMessageText());
        assertEquals(Event.Type.IN_CALENDAR, secondCreated.enumType());
        assertEquals(2, unitService.getProceduresByUnit(unit.getId(), null).size());

        try { // existing name
            createProcedure("a", "aabbcc", 10, "text", Event.Type.IN_CALENDAR, unit);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }

        // second unit (I can create user with Birth Number in other unit)
        final Unit secondUnit = createUnit("A", "desc", Unit.TextVariant.PATIENT, 10L, null);
        final Procedure thirdCreated = createProcedure("a", "aabbcc", 10, "text", Event.Type.IN_CALENDAR, secondUnit);
        assertEquals(secondUnit.getId(), thirdCreated.getUnit().getId());
        assertEquals("a", thirdCreated.getName());
        assertEquals(2, unitService.getProceduresByUnit(unit.getId(), null).size());
        assertEquals(1, unitService.getProceduresByUnit(secondUnit.getId(), null).size());
    }


    // -------------------------------------------------------- Assistant Stuff

}
