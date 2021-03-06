package veny.smevente.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import veny.smevente.AbstractBaseTestWithAuth;
import veny.smevente.model.Procedure;
import veny.smevente.model.Membership;
import veny.smevente.model.Patient;
import veny.smevente.model.Unit;
import veny.smevente.model.User;

/**
 * Test of authorization.
 * There are tests only of permissions more than ROLE_AUTHENTICATED.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
public class AuthorizationTest extends AbstractBaseTestWithAuth {

    /** Test whether the authentication mechanism works. */
    @Test
    public void testLoginLogout() {
        userService.findUsers(unit1.getId(), null, null);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            userService.findUsers(unit1.getId(), null, null);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    // ------------------------------------------------------ UserService Stuff

    /** UserService.createUser(UserDto, Long, MembershipDto.Type, Integer). */
    @Test
    public void testUserServiceCreateUser() {
        final User userA = new User();
        userA.setUsername("a");
        userA.setFullname("a a");
        userA.setPassword("a");
        userService.storeUser(userA, unit1.getId(), Membership.Role.MEMBER, 0);

        try { // hasPermission(#unitId, 'V_UNIT_ADMIN')
            userService.storeUser(userA, unit2.getId(), Membership.Role.MEMBER, 0);
            assertEquals("expected AccessDeniedException", true, false);
        } catch (AccessDeniedException e) { assertEquals(true, true); }
    }

//XXX    /** UserService.updateUser(UserDto, Long, MembershipDto.Type, Integer). */
//    @Test
//    public void testUserServiceUpdateUser() {
//        userService.updateUser(user1, unit1.getId(), Membership.Role.MEMBER, 0);
//
//        try { // hasPermission(#unitId, 'V_UNIT_ADMIN')
//            userService.createUser(user1, unit2.getId(), Membership.Role.MEMBER, 0);
//            assertEquals("expected AccessDeniedException", true, false);
//        } catch (AccessDeniedException e) { assertEquals(true, true); }
//    }

    /** UserService.updateUserPassword. */
    @Test
    public void testUserServiceUpdateUserPassword() {
        userService.updateUserPassword(user1.getId(), "a", "newPassword");

        User user = new User();
        user.setUsername("a");
        user.setFullname("a a");
        user.setPassword("a");
        user = userService.storeUser(user, unit1.getId(), Membership.Role.MEMBER, 0);
        try { // hasPermission(#userId, 'V_MY_USER')
            userService.updateUserPassword(user.getId(), user.getPassword(), "newPassword");
            assertEquals("expected AccessDeniedException", true, false);
        } catch (AccessDeniedException e) { assertEquals(true, true); }

        logout();
        try { // hasPermission(#userId, 'V_MY_USER')
            userService.updateUserPassword(user1.getId(), "newPassword", "oldPassword");
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    // ------------------------------------------------------ UnitService Stuff

//XXX    /** UnitService.createUnit. */
//    @Test
//    public void testCreateUnit() {
//        final Unit toCreate = new Unit();
//        toCreate.setName(UNITNAME);
//        toCreate.setMetadata(getDefaultUnitMetadata());
//        toCreate.setLimitedSmss(LIMITED_SMSS);
//
//        unitService.createUnit(toCreate);
//
//        logout();
//        try { // hasRole('ROLE_AUTHENTICATED')
//            unitService.createUnit(toCreate);
//            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
//        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
//    }

    /** UnitService.getById. */
    @Test
    public void testGetById() {
        assertEquals(unit1.getName(), unitService.getUnit(unit1.getId()).getName());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getUnit(unit1.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.getAllUnits. */
    @SuppressWarnings("deprecation")
    @Test
    public void testGetAllUnits() {
        unitService.getAllUnits();

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getAllUnits();
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.createPatient. */
    @Test
    public void testCreatePatient() {
        final Patient toCreate = new Patient();
        toCreate.setUnit(unit1);
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

        unitService.storePatient(toCreate);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.storePatient(toCreate);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.updatePatient. */
    @Test
    public void testUpdatePatient() {
        final Patient toCreate = new Patient();
        toCreate.setUnit(unit1);
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

        final Patient created = unitService.storePatient(toCreate);
        // CHECKSTYLE:OFF
        final String NEW_PHONE_NUMBER = "666123456";
        // CHECKSTYLE:ON
        created.setPhoneNumber(NEW_PHONE_NUMBER);
        unitService.storePatient(created);
        assertEquals(1, unitService.findPatients(unit1.getId(), null, created.getPhoneNumber(), null).size());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            created.setPhoneNumber(PHONE_NUMBER);

            unitService.storePatient(created);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }

        login(user1);
        assertEquals(1, unitService.findPatients(unit1.getId(), null, NEW_PHONE_NUMBER, null).size());
        logout();
    }

    /** UnitService.getPatientsByUnit. */
    @Test
    public void testGetPatientsByUnit() {
        final Patient toCreate = new Patient();
        toCreate.setUnit(unit1);
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

        unitService.storePatient(toCreate);
        assertEquals(1, unitService.getPatientsByUnit(unit1.getId()).size());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getPatientsByUnit(unit1.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.findPatients. */
    @Test
    public void testFindPatients() {
        unitService.findPatients(unit1.getId(), null, null, null);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.findPatients(unit1.getId(), null, null, null);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.getPatientById. */
    @Test
    public void testGetPatientById() {
        final Patient toCreate = new Patient();
        toCreate.setUnit(unit1);
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

        final Patient created = unitService.storePatient(toCreate);
        assertEquals(FIRSTNAME, unitService.getPatientById(created.getId()).getFirstname());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getPatientById(created.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.deletePatient. */
    @Test
    public void testDeletePatient() {
        final Patient toCreate = new Patient();
        toCreate.setUnit(unit1);
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

        final Patient created1 = unitService.storePatient(toCreate);
        unitService.deletePatient(created1.getId());
        final Patient created2 = unitService.storePatient(created1);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.deletePatient(created2.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

//    /** UnitService.createMedicalHelpCategory. */
//    @Test
//    public void testCreateMedicalHelpCategory() {
//        final MedicalHelpCategory toCreate = new MedicalHelpCategory();
//        toCreate.setUnit(unit1);
//        toCreate.setName(MHC_NAME);
//        toCreate.setSmsText(MHC_MSGTEXT);
//        toCreate.setType(MedicalHelpCategory.TYPE_STANDARD);
//        toCreate.setColor(MHC_COLOR);
//        toCreate.setTime(MHC_TIME);
//
//        unitService.createMedicalHelpCategory(toCreate);
//
//        logout();
//        try { // hasRole('ROLE_AUTHENTICATED')
//            unitService.createMedicalHelpCategory(toCreate);
//            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
//        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
//    }
//
//    /** UnitService.updateMedicalHelpCategory. */
//    @Test
//    public void testUpdateMedicalHelpCategory() {
//        final MedicalHelpCategory toCreate = new MedicalHelpCategory();
//        toCreate.setUnit(unit1);
//        toCreate.setName(MHC_NAME);
//        toCreate.setSmsText(MHC_MSGTEXT);
//        toCreate.setType(MedicalHelpCategory.TYPE_STANDARD);
//        toCreate.setColor(MHC_COLOR);
//        toCreate.setTime(MHC_TIME);
//
//        final MedicalHelpCategory created = unitService.createMedicalHelpCategory(toCreate);
//        // CHECKSTYLE:OFF
//        final String NEW_MHC_COLOR = "CCBBAA";
//        // CHECKSTYLE:ON
//        created.setColor(NEW_MHC_COLOR);
//        unitService.updateMedicalHelpCategory(created);
//        assertEquals(NEW_MHC_COLOR, unitService.getMedicalHelpCategoryById(created.getId()).getColor());
//
//        logout();
//        try { // hasRole('ROLE_AUTHENTICATED')
//            created.setColor(MHC_COLOR);
//
//            unitService.updateMedicalHelpCategory(created);
//            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
//        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
//
//        login(user1);
//        assertEquals(NEW_MHC_COLOR, unitService.getMedicalHelpCategoryById(created.getId()).getColor());
//        logout();
//    }
//
//    /** UnitService.getMedicalHelpCategoriesByUnit. */
//    @Test
//    public void testGetMedicalHelpCategoriesByUnit() {
//        final MedicalHelpCategory toCreate = new MedicalHelpCategory();
//        toCreate.setUnit(unit1);
//        toCreate.setName(MHC_NAME);
//        toCreate.setSmsText(MHC_MSGTEXT);
//        toCreate.setType(MedicalHelpCategory.TYPE_STANDARD);
//        toCreate.setColor(MHC_COLOR);
//        toCreate.setTime(MHC_TIME);
//
//        unitService.createMedicalHelpCategory(toCreate);
//        assertEquals(1, unitService.getMedicalHelpCategoriesByUnit(unit1.getId(),
//                MedicalHelpCategory.TYPE_STANDARD).size());
//
//        logout();
//        try { // hasRole('ROLE_AUTHENTICATED')
//            unitService.getMedicalHelpCategoriesByUnit(unit1.getId(), MedicalHelpCategory.TYPE_STANDARD);
//            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
//        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
//    }
//
//    /** UnitService.getMedicalHelpCategoryById. */
//    @Test
//    public void testGetMedicalHelpCategoryById() {
//        final MedicalHelpCategory toCreate = new MedicalHelpCategory();
//        toCreate.setUnit(unit1);
//        toCreate.setName(MHC_NAME);
//        toCreate.setSmsText(MHC_MSGTEXT);
//        toCreate.setType(MedicalHelpCategory.TYPE_STANDARD);
//        toCreate.setColor(MHC_COLOR);
//        toCreate.setTime(MHC_TIME);
//
//        final MedicalHelpCategory created = unitService.createMedicalHelpCategory(toCreate);
//        assertEquals(MHC_NAME, unitService.getMedicalHelpCategoryById(created.getId()).getName());
//
//        logout();
//        try { // hasRole('ROLE_AUTHENTICATED')
//            unitService.getMedicalHelpCategoryById(created.getId());
//            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
//        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
//    }
//
//    /** UnitService.deleteMedicalHelpCategory. */
//    @Test
//    public void testDeleteMedicalHelpCategory() {
//        final MedicalHelpCategory toCreate = new MedicalHelpCategory();
//        toCreate.setUnit(unit1);
//        toCreate.setName(MHC_NAME);
//        toCreate.setSmsText(MHC_MSGTEXT);
//        toCreate.setType(MedicalHelpCategory.TYPE_STANDARD);
//        toCreate.setColor(MHC_COLOR);
//        toCreate.setTime(MHC_TIME);
//
//        final MedicalHelpCategory created1 = unitService.createMedicalHelpCategory(toCreate);
//        unitService.deleteMedicalHelpCategory(created1.getId());
//        final MedicalHelpCategory created2 = unitService.createMedicalHelpCategory(created1);
//
//        logout();
//        try { // hasRole('ROLE_AUTHENTICATED')
//            unitService.deleteMedicalHelpCategory(created2.getId());
//            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
//        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
//    }
}
