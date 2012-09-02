package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import veny.smevente.AbstractBaseTest;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Unit;

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

        final Unit secondUnit = createUnit("A", "desc", null, 10L, null);
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

        final Unit secondCreated = createUnit("A", "desc", "type", 0L, "sms");
        final Unit secondFound = unitService.getUnit(secondCreated.getId());
        assertEquals(secondCreated.getId(), secondFound.getId());
        assertEquals("A", secondFound.getName());

        try { // get by non-existing ID
            unitService.getUnit("xx");
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
    }

    // ---------------------------------------------------------- Patient Stuff

//    /** UnitService.createPatient. */
//    @Test
//    public void testCreatePatient() {
//        final Unit unit = createDefaultUnit();
//
//        // first patient in the first unit
//        final PatientDto toCreate = new PatientDto();
//        toCreate.setUnit(unit);
//        toCreate.setFirstname(FIRSTNAME);
//        toCreate.setSurname(SURNAME);
//        toCreate.setPhoneNumber(PHONE_NUMBER);
//        toCreate.setBirthNumber(BIRTH_NUMBER);
//        toCreate.setDegree("degree");
//        toCreate.setStreet("street");
//        toCreate.setCity("city");
//        toCreate.setZipCode("zip code");
//        toCreate.setEmployer("employer");
//        toCreate.setCareers("careers");
//
//        final PatientDto firstCreated = unitService.createPatient(toCreate);
//        assertDefaultPatient(firstCreated, true);
//        assertEquals(unit.getId(), firstCreated.getUnit().getId());
//        assertEquals("degree", firstCreated.getDegree());
//        assertEquals("street", firstCreated.getStreet());
//        assertEquals("city", firstCreated.getCity());
//        assertEquals("zip code", firstCreated.getZipCode());
//        assertEquals("employer", firstCreated.getEmployer());
//        assertEquals("careers", firstCreated.getCareers());
//        assertEquals(1, unitService.getPatientsByUnit(unit.getId()).size());
//
//        // second patient in the first unit
//        final PatientDto secondCreated = createPatient("a", "b", null, null, unit);
//        assertNotNull(secondCreated);
//        assertNotNull(secondCreated.getId());
//        assertNotNull(secondCreated.getUnit());
//        assertNotNull(secondCreated.getUnit().getId());
//        assertEquals(unit.getId(), secondCreated.getUnit().getId());
//        assertEquals("a", secondCreated.getFirstname());
//        assertEquals("b", secondCreated.getSurname());
//        assertNull(secondCreated.getPhoneNumber());
//        assertNull(secondCreated.getBirthNumber());
//        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());
//
//        final PatientDto badPatient = new PatientDto();
//        badPatient.setUnit(unit);
//        badPatient.setFirstname("aa");
//        badPatient.setSurname("bb");
//        badPatient.setBirthNumber(BIRTH_NUMBER);
//        try { // existing birth number
//            unitService.createPatient(badPatient);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//
//        // second unit (I can create user with Birth Number in other unit)
//        final Unit secondUnit = createUnit("x", getDefaultUnitMetadata(), LIMITED_SMSS);
//        badPatient.setUnit(secondUnit);
//        final PatientDto thirdCreated = unitService.createPatient(badPatient);
//        assertEquals(secondUnit.getId(), thirdCreated.getUnit().getId());
//        assertEquals(BIRTH_NUMBER, thirdCreated.getBirthNumber());
//        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());
//        assertEquals(1, unitService.getPatientsByUnit(secondUnit.getId()).size());
//
//        // validation - birth number
//        final PatientDto validation = new PatientDto();
//        validation.setUnit(unit);
//        validation.setFirstname("a");
//        validation.setSurname("a");
//        validation.setBirthNumber("12345678");
//        try { // short birth number
//            unitService.createPatient(validation);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//        validation.setBirthNumber("12345678901");
//        try { // long birth number
//            unitService.createPatient(validation);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//        // OK
//        validation.setBirthNumber("1234567890");
//        unitService.createPatient(validation);
//
//        // validation - phone number
//        validation.setBirthNumber(null);
//        validation.setPhoneNumber("12345678");
//        try { // short phone number
//            unitService.createPatient(validation);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//        validation.setPhoneNumber("12345678901x");
//        try { // phone number not a number
//            unitService.createPatient(validation);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//        // OK
//        validation.setPhoneNumber("123456789");
//        unitService.createPatient(validation);
//
//        // SOFT DELETE
//        // deleted patient doesn't block a unique birth number
//        unitService.deletePatient(firstCreated.getId());
//
//        final PatientDto softDel = new PatientDto();
//        softDel.setUnit(unit);
//        softDel.setFirstname("aa");
//        softDel.setSurname("bb");
//        softDel.setBirthNumber(BIRTH_NUMBER);
//        unitService.createPatient(softDel);
//    }
//
//    /** UnitService.updatePatient. */
//    @Test
//    public void testUpdatePatient() {
//        final PatientDto created = createDefaultPatient();
//        assertNull(created.getCity());
//        assertNull(created.getDegree());
//
//        created.setCity("city");
//        unitService.updatePatient(created);
//        PatientDto found = unitService.getPatientById(created.getId());
//        assertDefaultPatient(found, true);
//        assertEquals("city", found.getCity());
//        assertNull(created.getDegree());
//
//        created.setDegree("degree");
//        unitService.updatePatient(created);
//        found = unitService.getPatientById(created.getId());
//        assertDefaultPatient(found, true);
//        assertEquals("city", found.getCity());
//        assertEquals("degree", created.getDegree());
//
//        // phone + birth number
//        created.setPhoneNumber("987987987");
//        created.setBirthNumber("789789789");
//        unitService.updatePatient(created);
//        found = unitService.getPatientById(created.getId());
//        assertEquals("987987987", created.getPhoneNumber());
//        assertEquals("789789789", found.getBirthNumber());
//    }
//
//    /** UnitService.getPatientsByUnit. */
//    @Test
//    public void testGetPatientsByUnit() {
//        final PatientDto created = createDefaultPatient();
//        List<PatientDto> found = unitService.getPatientsByUnit(created.getUnit().getId());
//        assertNotNull(found);
//        assertEquals(1, found.size());
//        assertDefaultPatient(found.get(0), true);
//
//        createPatient("a", "a", null, null, created.getUnit());
//        found = unitService.getPatientsByUnit(created.getUnit().getId());
//        assertEquals(2, found.size());
//        assertDefaultUnit(found.get(0).getUnit());
//        assertDefaultUnit(found.get(1).getUnit());
//
//        createPatient("b", "b", null, null, created.getUnit());
//        found = unitService.getPatientsByUnit(created.getUnit().getId());
//        assertEquals(3, found.size());
//        assertDefaultUnit(found.get(0).getUnit());
//        assertDefaultUnit(found.get(1).getUnit());
//        assertDefaultUnit(found.get(2).getUnit());
//
//        final Unit secondUnit = createUnit("X", getDefaultUnitMetadata(), LIMITED_SMSS);
//        final PatientDto c = createPatient("c", "c", null, null, secondUnit);
//        found = unitService.getPatientsByUnit(secondUnit.getId());
//        assertEquals(1, found.size());
//        assertEquals("X", found.get(0).getUnit().getName());
//        assertEquals(3, unitService.getPatientsByUnit(created.getUnit().getId()).size());
//
//        // SOFT DELETE
//        // first unit
//        unitService.deletePatient(created.getId());
//        assertEquals(2, unitService.getPatientsByUnit(created.getUnit().getId()).size());
//        // second unit
//        unitService.deletePatient(c.getId());
//        assertEquals(0, unitService.getPatientsByUnit(c.getUnit().getId()).size());
//    }
//
//    /** UnitService.findPatients. */
//    @Test
//    public void testFindPatients() {
//        final Unit unit = createDefaultUnit();
//        final PatientDto adam = createPatient("Adam", "Bláha", "000000000", "7001011111", unit);
//        final PatientDto vaclav = createPatient("Václav", "Sýkora", "011111111", "7001022222", unit);
//        createPatient("John", "Žluťoučký", "012222222", "7003033333", unit);
//        createPatient("Robert", "Kůň", "012333333", "7004044444", unit);
//        createPatient("Norbert", "Kuře", "012344444", "7005055555", unit);
//        createPatient("Gábina", "Buližníková", "012345555", "7006066666", unit);
//        createPatient("Žulet", "Světlíková", "012345666", "7007077777", unit);
//        createPatient("Šón", "Ďáblík", "012345677", "7008088888", unit);
//
//        // by name
//        assertEquals(1, unitService.findPatients(unit.getId(), "ADAM", null, null).size()); // Blaha
//        assertEquals(1, unitService.findPatients(unit.getId(), "blaha", null, null).size()); // Blaha
//        assertEquals(3, unitService.findPatients(unit.getId(), "S", null, null).size()); // Sykora,Svetlikova,Dablik
//        assertEquals(2, unitService.findPatients(unit.getId(), "k", null, null).size()); // Kun,Kure
//        assertEquals(0, unitService.findPatients(unit.getId(), "x", null, null).size()); // Kun,Kure
//
//        // by phone number
//        assertEquals(8, unitService.findPatients(unit.getId(), null, "0", null).size());
//        assertEquals(4, unitService.findPatients(unit.getId(), null, "01234", null).size());
//        assertEquals(1, unitService.findPatients(unit.getId(), null, "012345677", null).size());
//        assertEquals(0, unitService.findPatients(unit.getId(), null, "9", null).size());
//
//        // by birth number
//        assertEquals(8, unitService.findPatients(unit.getId(), null, null, "70").size());
//        assertEquals(2, unitService.findPatients(unit.getId(), null, null, "7001").size());
//        assertEquals(1, unitService.findPatients(unit.getId(), null, null, "7003").size());
//        assertEquals(0, unitService.findPatients(unit.getId(), null, null, "8").size());
//
//        // combination
//        assertEquals(8, unitService.findPatients(unit.getId(), null, null, null).size());
//        assertEquals(3, unitService.findPatients(unit.getId(), "s", null, "70").size()); // Sykora,Svetlikova,Dablik
//        assertEquals(2, unitService.findPatients(unit.getId(), "s", "0123456", "70").size()); // Svetlikova,Dablik
//        assertEquals(1, unitService.findPatients(unit.getId(), "S", "01234567", null).size()); // Dablik
//        assertEquals(0, unitService.findPatients(unit.getId(), "S", "01", "8").size());
//
//        // SOFT DELETE
//        unitService.deletePatient(adam.getId());
//        assertEquals(0, unitService.findPatients(unit.getId(), "ADAM", null, null).size()); // Blaha
//        assertEquals(0, unitService.findPatients(unit.getId(), "blaha", null, null).size()); // Blaha
//        unitService.deletePatient(vaclav.getId());
//        assertEquals(0, unitService.findPatients(unit.getId(), null, "011111111", null).size());
//        assertEquals(0, unitService.findPatients(unit.getId(), null, null, "7001022222").size());
//    }
//
//    /** UnitService.deletePatient. */
//    @Test
//    public void testDeletePatient() {
//        final PatientDto firstCreated = createDefaultPatient();
//        final Unit unit = firstCreated.getUnit();
//        final PatientDto secondCreated = createPatient("a", "a", null, null, unit);
//        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());
//
//        // delete first
//        unitService.deletePatient(firstCreated.getId());
//        try {
//            unitService.getPatientById(firstCreated.getId());
//            assertEquals("expected DeletedObjectException", true, false);
//        } catch (DeletedObjectException e) { assertEquals(true, true); }
//        List<PatientDto> found = unitService.getPatientsByUnit(unit.getId());
//        assertEquals(1, found.size());
//        assertEquals(secondCreated.getId(), found.get(0).getId());
//
//        final PatientDto thirdCreated = createPatient("b", "b", null, null, unit);
//        assertEquals(2, unitService.getPatientsByUnit(unit.getId()).size());
//        unitService.deletePatient(firstCreated.getId()); // DO NOTHING
//        unitService.deletePatient(secondCreated.getId());
//        try {
//            unitService.getPatientById(secondCreated.getId());
//            assertEquals("expected DeletedObjectException", true, false);
//        } catch (DeletedObjectException e) { assertEquals(true, true); }
//        found = unitService.getPatientsByUnit(unit.getId());
//        assertEquals(1, found.size());
//        assertEquals(thirdCreated.getId(), found.get(0).getId());
//        unitService.deletePatient(thirdCreated.getId());
//
//        unitService.deletePatient(thirdCreated.getId());
//        try {
//            unitService.getPatientById(thirdCreated.getId());
//            assertEquals("expected DeletedObjectException", true, false);
//        } catch (DeletedObjectException e) { assertEquals(true, true); }
//        found = unitService.getPatientsByUnit(unit.getId());
//        assertEquals(0, found.size());
//    }
//
//    // ---------------------------------------------- MedicalHelpCategory Stuff
//
//    /**
//     * UnitService.createMedicalHelpCategory.
//     * UnitService.getMedicalHelpCategoriesByUnit.
//     */
//    @Test
//    public void testCreateMedicalHelpCategoryStandard() {
//        testCreateMedicalHelpCategory(null);
//    }
//    /**
//     * UnitService.createMedicalHelpCategory.
//     * UnitService.getMedicalHelpCategoriesByUnit.
//     */
//    @Test
//    public void testCreateMedicalHelpCategoryStandardEx() {
//        testCreateMedicalHelpCategory(MedicalHelpCategoryDto.TYPE_STANDARD);
//    }
//    /**
//     * UnitService.createMedicalHelpCategory.
//     * UnitService.getMedicalHelpCategoriesByUnit.
//     */
//    @Test
//    public void testCreateMedicalHelpCategorySpecial() {
//        testCreateMedicalHelpCategory(MedicalHelpCategoryDto.TYPE_SPECIAL);
//    }
//    /**
//     * UnitService.createMedicalHelpCategory.
//     * UnitService.getMedicalHelpCategoriesByUnit.
//     * @param categoryType the type of category
//     */
//    private void testCreateMedicalHelpCategory(final Short categoryType) {
//        final Unit unit = createDefaultUnit();
//
//        // first ST in the first unit
//        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
//        toCreate.setUnit(unit);
//        toCreate.setName(MHC_NAME);
//        toCreate.setSmsText(MHC_MSGTEXT);
//        toCreate.setType(categoryType);
//        if (categoryType == null || categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            toCreate.setColor(MHC_COLOR);
//            toCreate.setTime(MHC_TIME);
//        }
//
//        final MedicalHelpCategoryDto firstCreated = unitService.createMedicalHelpCategory(toCreate);
//        assertDefaultMedicalHelpCategory(firstCreated, categoryType, true);
//        assertEquals(unit.getId(), firstCreated.getUnit().getId());
//
//        List<MedicalHelpCategoryDto> found = unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType);
//        assertEquals(1, found.size());
//        assertDefaultMedicalHelpCategory(found.get(0), categoryType, true);
//
//        // second ST in the first unit
//        final MedicalHelpCategoryDto secondCreated =
//            createMedicalHelpCategory("A", "AAAAAA", 10, "text", categoryType, unit);
//        assertNotNull(secondCreated);
//        assertNotNull(secondCreated.getId());
//        assertNotNull(secondCreated.getUnit());
//        assertNotNull(secondCreated.getUnit().getId());
//        assertEquals(unit.getId(), secondCreated.getUnit().getId());
//        assertEquals("A", secondCreated.getName());
//        assertEquals("text", secondCreated.getSmsText());
//        assertEquals(categoryType, secondCreated.getType());
//        if (categoryType == null || categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            assertEquals("AAAAAA", secondCreated.getColor());
//            assertEquals(10, secondCreated.getTime());
//        }
//        found = unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType);
//        assertEquals(2, found.size());
//        // sorted by name
//        assertEquals("A", found.get(0).getName());
//        assertEquals(MHC_NAME, found.get(1).getName());
//
//        // VALIDATION
//        final MedicalHelpCategoryDto bad2Create = new MedicalHelpCategoryDto();
//        final Unit badUnit = new Unit();
//        badUnit.setId(Long.MAX_VALUE);
//        bad2Create.setUnit(badUnit);
//        bad2Create.setName(MHC_NAME);
//        bad2Create.setSmsText("xy");
//        bad2Create.setType(categoryType);
//        if (categoryType == null || categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            bad2Create.setColor("E");
//            bad2Create.setTime(5);
//        }
//
//        try { // invalid unit
//            unitService.createMedicalHelpCategory(bad2Create);
//            assertEquals("expected ObjectNotFoundException", true, false);
//        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
//
//        bad2Create.setUnit(unit);
//        if (categoryType == null || categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            try { // invalid color
//                unitService.createMedicalHelpCategory(bad2Create);
//                assertEquals("expected IllegalArgumentException", true, false);
//            } catch (IllegalArgumentException e) { assertEquals(true, true); }
//        }
//
//        bad2Create.setColor("EEEEEE");
//        try { // duplicate name
//            unitService.createMedicalHelpCategory(bad2Create);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//
//        // second unit (i can create Service Type with Name in other unit)
//        final Unit secondUnit = createUnit("x", getDefaultUnitMetadata(), LIMITED_SMSS);
//        bad2Create.setUnit(secondUnit);
//        final MedicalHelpCategoryDto thirdCreated = unitService.createMedicalHelpCategory(bad2Create);
//        assertEquals(secondUnit.getId(), thirdCreated.getUnit().getId());
//        assertEquals(MHC_NAME, thirdCreated.getName());
//        assertEquals(2, unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType).size());
//        assertEquals(1, unitService.getMedicalHelpCategoriesByUnit(secondUnit.getId(), categoryType).size());
//
//        // SOFT DELETE
//        // deleted category doesn't block a unique name
//        unitService.deleteMedicalHelpCategory(firstCreated.getId());
//        assertEquals(1, unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType).size());
//        unitService.deleteMedicalHelpCategory(secondCreated.getId());
//        assertEquals(0, unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType).size());
//
//        final MedicalHelpCategoryDto softDel = new MedicalHelpCategoryDto();
//        softDel.setUnit(unit);
//        softDel.setColor(secondCreated.getColor());
//        softDel.setName(secondCreated.getName());
//        softDel.setSmsText(secondCreated.getSmsText());
//        softDel.setTime(secondCreated.getTime());
//        softDel.setType(secondCreated.getType());
//        unitService.createMedicalHelpCategory(softDel);
//        found = unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType);
//        assertEquals(1, found.size());
//        assertEquals(secondCreated.getName(), found.get(0).getName());
//    }
//
//    /** UnitService.updateMedicalHelpCategory. */
//    @Test
//    public void testUpdateMedicalHelpCategoryStandard() {
//        testUpdateMedicalHelpCategory(null);
//    }
//    /** UnitService.updateMedicalHelpCategory. */
//    @Test
//    public void testUpdateMedicalHelpCategoryStandardEx() {
//        testUpdateMedicalHelpCategory(MedicalHelpCategoryDto.TYPE_STANDARD);
//    }
//    /** UnitService.updateMedicalHelpCategory. */
//    @Test
//    public void testUpdateMedicalHelpCategorySpecial() {
//        testUpdateMedicalHelpCategory(MedicalHelpCategoryDto.TYPE_SPECIAL);
//    }
//    /**
//     * UnitService.updateMedicalHelpCategory.
//     * @param categoryType the type of category
//     */
//    private void testUpdateMedicalHelpCategory(final Short categoryType) {
//        final MedicalHelpCategoryDto created = createDefaultMedicalHelpCategory(categoryType);
//        assertDefaultMedicalHelpCategory(created, categoryType, true);
//
//        created.setColor("000000");
//        unitService.updateMedicalHelpCategory(created);
//        MedicalHelpCategoryDto found = unitService.getMedicalHelpCategoryById(created.getId());
//        assertEquals("000000", found.getColor());
//
//        created.setName("TestName");
//        unitService.updateMedicalHelpCategory(created);
//        found = unitService.getMedicalHelpCategoryById(created.getId());
//        assertEquals("000000", found.getColor());
//        assertEquals("TestName", created.getName());
//
//        created.setSmsText("TestSMSText");
//        unitService.updateMedicalHelpCategory(created);
//        found = unitService.getMedicalHelpCategoryById(created.getId());
//        assertEquals("000000", found.getColor());
//        assertEquals("TestName", created.getName());
//        assertEquals("TestSMSText", created.getSmsText());
//
//        created.setTime(25 * 60000);
//        unitService.updateMedicalHelpCategory(created);
//        found = unitService.getMedicalHelpCategoryById(created.getId());
//        assertEquals("000000", found.getColor());
//        assertEquals("TestName", created.getName());
//        assertEquals("TestSMSText", created.getSmsText());
//        assertEquals(25 * 60000, created.getTime());
//    }
//
//    /** UnitService.getMedicalHelpCategoriesByUnit. */
//    @Test
//    public void testGetMedicalHelpCategoriesByUnitStandard() {
//        testGetMedicalHelpCategoriesByUnit(null);
//    }
//    /** UnitService.getMedicalHelpCategoriesByUnit. */
//    @Test
//    public void testGetMedicalHelpCategoriesByUnitStandardEx() {
//        testGetMedicalHelpCategoriesByUnit(MedicalHelpCategoryDto.TYPE_STANDARD);
//    }
//    /** UnitService.getMedicalHelpCategoriesByUnit. */
//    @Test
//    public void testGetMedicalHelpCategoriesByUnitSpecial() {
//        testGetMedicalHelpCategoriesByUnit(MedicalHelpCategoryDto.TYPE_SPECIAL);
//    }
//
//    /**
//     * UnitService.getMedicalHelpCategoriesByUnit.
//     * @param categoryType the type of category
//     */
//    private void testGetMedicalHelpCategoriesByUnit(final Short categoryType) {
//        final MedicalHelpCategoryDto created = createDefaultMedicalHelpCategory(categoryType);
//        List<MedicalHelpCategoryDto> found =
//            unitService.getMedicalHelpCategoriesByUnit(created.getUnit().getId(), categoryType);
//        assertNotNull(found);
//        assertEquals(1, found.size());
//        assertDefaultMedicalHelpCategory(found.get(0), categoryType, true);
//
//        createMedicalHelpCategory("Name1", "000000", 600000, "msgText1", categoryType, created.getUnit());
//        found = unitService.getMedicalHelpCategoriesByUnit(created.getUnit().getId(), categoryType);
//        assertEquals(2, found.size());
//        assertDefaultUnit(found.get(0).getUnit());
//        assertDefaultUnit(found.get(1).getUnit());
//
//        createMedicalHelpCategory("Name2", "000000", 600000, "msgText2", categoryType, created.getUnit());
//        found = unitService.getMedicalHelpCategoriesByUnit(created.getUnit().getId(), categoryType);
//        assertEquals(3, found.size());
//        assertDefaultUnit(found.get(0).getUnit());
//        assertDefaultUnit(found.get(1).getUnit());
//        assertDefaultUnit(found.get(2).getUnit());
//
//        final Unit secondUnit = createUnit("X", getDefaultUnitMetadata(), LIMITED_SMSS);
//        final MedicalHelpCategoryDto createdInSecondUnit = createMedicalHelpCategory(
//                "Name3", "000000", 600000, "msgText3", categoryType, secondUnit);
//        found = unitService.getMedicalHelpCategoriesByUnit(secondUnit.getId(), categoryType);
//        assertEquals(1, found.size());
//        assertEquals("X", found.get(0).getUnit().getName());
//        assertEquals(3, unitService.getMedicalHelpCategoriesByUnit(created.getUnit().getId(), categoryType).size());
//
//        // SOFT DELETE
//        // first unit
//        unitService.deleteMedicalHelpCategory(created.getId());
//        assertEquals(2, unitService.getMedicalHelpCategoriesByUnit(created.getUnit().getId(), categoryType).size());
//        // second unit
//        unitService.deleteMedicalHelpCategory(createdInSecondUnit.getId());
//        assertEquals(0, unitService.getMedicalHelpCategoriesByUnit(
//                createdInSecondUnit.getUnit().getId(), categoryType).size());
//    }
//
//    /** UnitService.deleteMedicalHelpCategory. */
//    @Test
//    public void testDeleteMedicalHelpCategoryStandard() {
//        testDeleteMedicalHelpCategory(null);
//    }
//    /** UnitService.deleteMedicalHelpCategory. */
//    @Test
//    public void testDeleteMedicalHelpCategoryStandardEx() {
//        testDeleteMedicalHelpCategory(MedicalHelpCategoryDto.TYPE_STANDARD);
//    }
//    /** UnitService.deleteMedicalHelpCategory. */
//    @Test
//    public void testDeleteMedicalHelpCategorySpecial() {
//        testDeleteMedicalHelpCategory(MedicalHelpCategoryDto.TYPE_SPECIAL);
//    }
//    /**
//     * UnitService.deleteMedicalHelpCategory.
//     * @param categoryType the type of category
//     */
//    private void testDeleteMedicalHelpCategory(final Short categoryType) {
//        final MedicalHelpCategoryDto firstCreated = createDefaultMedicalHelpCategory(categoryType);
//        final Unit unit = firstCreated.getUnit();
//        final MedicalHelpCategoryDto secondCreated = createMedicalHelpCategory("Name1", "000000", 600000,
//                "msgText1", categoryType, unit);
//        assertEquals(2, unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType).size());
//
//        // delete first
//        unitService.deleteMedicalHelpCategory(firstCreated.getId());
//        List<MedicalHelpCategoryDto> found = unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType);
//        assertEquals(1, found.size());
//        assertEquals(secondCreated.getId(), found.get(0).getId());
//
//        final MedicalHelpCategoryDto thirdCreated = createMedicalHelpCategory("Name2", "000000", 600000,
//                "msgText2", categoryType, unit);
//        assertEquals(2, unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType).size());
//        unitService.deleteMedicalHelpCategory(firstCreated.getId()); // DO NOTHING
//        unitService.deleteMedicalHelpCategory(secondCreated.getId());
//        found = unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType);
//        assertEquals(1, found.size());
//        assertEquals(thirdCreated.getId(), found.get(0).getId());
//        unitService.deleteMedicalHelpCategory(thirdCreated.getId());
//
//        unitService.deleteMedicalHelpCategory(thirdCreated.getId());
//        found = unitService.getMedicalHelpCategoriesByUnit(unit.getId(), categoryType);
//        assertEquals(0, found.size());
//    }

    // -------------------------------------------------------- Assistant Stuff

}
