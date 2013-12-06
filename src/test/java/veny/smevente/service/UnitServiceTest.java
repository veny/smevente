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
import veny.smevente.model.Patient;
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
    @SuppressWarnings("deprecation")
    @Test
    public void testCreateUnit() {
        final Unit firstUnit = createDefaultUnit();
        final List<Unit> found = unitService.getAllUnits();
        assertEquals(1, found.size());
        assertDefaultUnit(found.get(0));

        final Unit secondUnit = createUnit("A", "desc", Unit.TextVariant.PATIENT, 10L, null);
        assertNotNull(secondUnit.getId());
        assertEquals("A", secondUnit.getName());
        assertEquals(new Long(10), secondUnit.getLimitedSmss());

        assertFalse(firstUnit.getId().equals(secondUnit.getId()));
        assertEquals(2, unitService.getAllUnits().size());

        // Validation
        Unit toCreate = new Unit();
        try { // existing unit name
            toCreate.setName("A");
            unitService.createUnit(toCreate);
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

        final Unit secondCreated = createUnit("A", "desc", Unit.TextVariant.PATIENT, 0L, "sms");
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

    // ---------------------------------------------------------- Patient Stuff

    /** UnitService.storePatient (create). */
    @Test // unit/patient/
    public void testStoreCreatePatient() {
        final Unit unit = createDefaultUnit();

        // first patient in the first unit
        final Patient toCreate = new Patient();
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

        final Patient firstCreated = unitService.storePatient(toCreate);
        assertDefaultPatient(firstCreated, true);
        assertEquals(unit.getId(), firstCreated.getUnit().getId());
        assertEquals("degree", firstCreated.getDegree());
        assertEquals("street", firstCreated.getStreet());
        assertEquals("city", firstCreated.getCity());
        assertEquals("zip code", firstCreated.getZipCode());
        assertEquals("employer", firstCreated.getEmployer());
        assertEquals("careers", firstCreated.getCareers());
        assertEquals(1, unitService.getPatientsByUnit(unit.getId()).size());

        // second patient in the first unit
        final Patient secondCreated = createPatient("a", "b", null, null, unit);
        assertNotNull(secondCreated);
        assertNotNull(secondCreated.getId());
        assertNotNull(secondCreated.getUnit());
        assertNotNull(secondCreated.getUnit().getId());
        assertEquals(unit.getId(), secondCreated.getUnit().getId());
        assertEquals("a", secondCreated.getFirstname());
        assertEquals("b", secondCreated.getSurname());
        assertNull(secondCreated.getPhoneNumber());
        assertNull(secondCreated.getBirthNumber());
        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());

        final Patient badPatient = new Patient();
        badPatient.setFirstname("aa");
        badPatient.setSurname("bb");
        badPatient.setBirthNumber(BIRTH_NUMBER);
        badPatient.setUnitId(unit.getId());
        try { // existing birth number
            unitService.storePatient(badPatient);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }

        // second unit (I can create user with Birth Number in other unit)
        final Unit secondUnit = createUnit("A", "desc", Unit.TextVariant.PATIENT, 10L, null);
        badPatient.setUnitId(secondUnit.getId());
        final Patient thirdCreated = unitService.storePatient(badPatient);
        assertEquals(secondUnit.getId(), thirdCreated.getUnit().getId());
        assertEquals(BIRTH_NUMBER, thirdCreated.getBirthNumber());
        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());
        assertEquals(1, unitService.getPatientsByUnit(secondUnit.getId()).size());

        // validation - birth number
        final Patient validation = new Patient();
        validation.setFirstname("a");
        validation.setSurname("a");
        validation.setBirthNumber("12345678");
        validation.setUnitId(unit.getId());
        try { // short birth number
            unitService.storePatient(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        validation.setBirthNumber("12345678901");
        try { // long birth number
            unitService.storePatient(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        // OK
        validation.setBirthNumber("1234567890");
        unitService.storePatient(validation);

        // validation - phone number
        validation.setBirthNumber(null);
        validation.setPhoneNumber("12345678");
        try { // short phone number
            unitService.storePatient(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        validation.setPhoneNumber("12345678901x");
        try { // phone number not a number
            unitService.storePatient(validation);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
        // OK
        validation.setPhoneNumber("123456789");
        unitService.storePatient(validation);
    }

    /** UnitService.storePatient (update). */
    @Test // unit/patient/
    public void testStoreUpdatePatient() {
        final Patient created = createDefaultPatient();
        assertNull(created.getCity());
        assertNull(created.getDegree());

        created.setCity("city");
        unitService.storePatient(created);
        Patient found = unitService.getPatientById(created.getId());
        assertDefaultPatient(found, true);
        assertEquals("city", found.getCity());
        assertNull(created.getDegree());

        created.setDegree("degree");
        unitService.storePatient(created);
        found = unitService.getPatientById(created.getId());
        assertDefaultPatient(found, true);
        assertEquals("city", found.getCity());
        assertEquals("degree", created.getDegree());

        // phone + birth number
        created.setPhoneNumber("987987987");
        created.setBirthNumber("789789789");
        unitService.storePatient(created);
        found = unitService.getPatientById(created.getId());
        assertEquals("987987987", created.getPhoneNumber());
        assertEquals("789789789", found.getBirthNumber());
    }

    /** UnitService.getPatientsByUnit. */
    @Test // unit/{id}/info/
    public void testGetPatientsByUnit() {
        final Patient created = createDefaultPatient();
        assertNotNull(created.getUnit().getId());
        List<Patient> found = unitService.getPatientsByUnit(created.getUnit().getId());
        assertNotNull(found);
        assertEquals(1, found.size());
        assertDefaultPatient(found.get(0), true);

        createPatient("a", "a", null, null, created.getUnit());
        found = unitService.getPatientsByUnit(created.getUnit().getId());
        assertEquals(2, found.size());
        assertDefaultUnit(found.get(0).getUnit());
        assertDefaultUnit(found.get(1).getUnit());

        createPatient("b", "b", null, null, created.getUnit());
        found = unitService.getPatientsByUnit(created.getUnit().getId());
        assertEquals(3, found.size());
        assertDefaultUnit(found.get(0).getUnit());
        assertDefaultUnit(found.get(1).getUnit());
        assertDefaultUnit(found.get(2).getUnit());

        final Unit secondUnit = createUnit("X", "desc", Unit.TextVariant.PATIENT, 0L, null);
        final Patient c = createPatient("c", "c", null, null, secondUnit);
        found = unitService.getPatientsByUnit(secondUnit.getId());
        assertEquals(1, found.size());
        assertEquals(secondUnit.getId(), found.get(0).getUnit().getId());
        assertEquals(3, unitService.getPatientsByUnit(created.getUnit().getId()).size());

        // SOFT DELETE
        // first unit
        unitService.deletePatient(created.getId());
        assertEquals(2, unitService.getPatientsByUnit(created.getUnit().getId()).size());
        // second unit
        unitService.deletePatient(c.getId());
        assertEquals(0, unitService.getPatientsByUnit(c.getUnit().getId()).size());
    }

    /** UnitService.findPatients. */
    @Test // unit/{id}/patient/
    public void testFindPatients() {
        final Unit unit = createDefaultUnit();
        assertTrue(unitService.findPatients(unit.getId(), null, null, null).isEmpty());
        final Patient adam = createPatient("Adam", "Bláha", "000000000", "7001011111", unit);
        final Patient vaclav = createPatient("Václav", "Sýkora", "011111111", "7001022222", unit);
        createPatient("John", "Žluťoučký", "012222222", "7003033333", unit);
        createPatient("Robert", "Kůň", "012333333", "7004044444", unit);
        createPatient("Norbert", "Kuře", "012344444", "7005055555", unit);
        createPatient("Gábina", "Buližníková", "012345555", "7006066666", unit);
        createPatient("Žulet", "Světlíková", "012345666", "7007077777", unit);
        createPatient("Šón", "Ďáblík", "012345677", "7008088889", unit);

        // by name
        assertEquals(1, unitService.findPatients(unit.getId(), "ADAM", null, null).size()); // Blaha
        assertEquals(1, unitService.findPatients(unit.getId(), "blaha", null, null).size()); // Blaha
        assertEquals(3, unitService.findPatients(unit.getId(), "S", null, null).size()); // Sykora,Svetlikova,Dablik
        assertEquals(2, unitService.findPatients(unit.getId(), "ova", null, null).size()); // Buliznikova,Svetlikova
        assertEquals(3, unitService.findPatients(unit.getId(), "li", null, null).size()); // Buliznikova,Dablik
        assertEquals(0, unitService.findPatients(unit.getId(), "x", null, null).size());

        // by phone number
        assertEquals(8, unitService.findPatients(unit.getId(), null, "0", null).size());
        assertEquals(4, unitService.findPatients(unit.getId(), null, "01234", null).size());
        assertEquals(1, unitService.findPatients(unit.getId(), null, "012345677", null).size());
        assertEquals(0, unitService.findPatients(unit.getId(), null, "9", null).size());
        assertEquals(2, unitService.findPatients(unit.getId(), null, "56", null).size());
        assertEquals(1, unitService.findPatients(unit.getId(), null, "77", null).size());

        // by birth number
        assertEquals(8, unitService.findPatients(unit.getId(), null, null, "70").size());
        assertEquals(2, unitService.findPatients(unit.getId(), null, null, "7001").size());
        assertEquals(1, unitService.findPatients(unit.getId(), null, null, "7003").size());
        assertEquals(1, unitService.findPatients(unit.getId(), null, null, "88").size());
        assertEquals(1, unitService.findPatients(unit.getId(), null, null, "89").size());
        assertEquals(0, unitService.findPatients(unit.getId(), null, null, "123").size());

        // combination
        assertEquals(8, unitService.findPatients(unit.getId(), null, null, null).size());
        assertEquals(3, unitService.findPatients(unit.getId(), "s", null, "70").size()); // Sykora,Svetlikova,Dablik
        assertEquals(2, unitService.findPatients(unit.getId(), "s", "0123456", "70").size()); // Svetlikova,Dablik
        assertEquals(1, unitService.findPatients(unit.getId(), "S", "01234567", null).size()); // Dablik
        assertEquals(0, unitService.findPatients(unit.getId(), "S", "01", "123").size());

        // SOFT DELETE
        unitService.deletePatient(adam.getId());
        assertEquals(0, unitService.findPatients(unit.getId(), "ADAM", null, null).size()); // Blaha
        assertEquals(0, unitService.findPatients(unit.getId(), "blaha", null, null).size()); // Blaha
        unitService.deletePatient(vaclav.getId());
        assertEquals(0, unitService.findPatients(unit.getId(), null, "011111111", null).size());
        assertEquals(0, unitService.findPatients(unit.getId(), null, null, "7001022222").size());
    }

    /** UnitService.deletePatient. */
    @Test // unit/patient/{id}/
    public void testDeletePatient() {
        final Patient firstCreated = createDefaultPatient();
        final Unit unit = firstCreated.getUnit();
        final Patient secondCreated = createPatient("a", "a", null, null, unit);
        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());

        // delete first
        unitService.deletePatient(firstCreated.getId());
        try {
            unitService.getPatientById(firstCreated.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }
        List<Patient> found = unitService.getPatientsByUnit(unit.getId());
        assertEquals(1, found.size());
        assertEquals(secondCreated.getId(), found.get(0).getId());

        final Patient thirdCreated = createPatient("b", "b", null, null, unit);
        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());
        unitService.deletePatient(firstCreated.getId()); // DO NOTHING
        unitService.deletePatient(secondCreated.getId());
        try {
            unitService.getPatientById(secondCreated.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }
        found = unitService.getPatientsByUnit(unit.getId());
        assertEquals(1, found.size());
        assertEquals(thirdCreated.getId(), found.get(0).getId());
        unitService.deletePatient(thirdCreated.getId());

        unitService.deletePatient(thirdCreated.getId());
        try {
            unitService.getPatientById(thirdCreated.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }
        found = unitService.getPatientsByUnit(unit.getId());
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
