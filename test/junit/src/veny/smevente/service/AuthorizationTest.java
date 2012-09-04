package veny.smevente.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import veny.smevente.AbstractBaseTestWithAuth;
import veny.smevente.model.MedicalHelpCategoryDto;
import veny.smevente.model.Membership;
import veny.smevente.model.PatientDto;
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
        userService.createUser(userA, unit1.getId(), MembershipDto.Membership.MEMBER, 0);

        try { // hasPermission(#unitId, 'V_UNIT_ADMIN')
            userService.createUser(userA, unit2.getId(), MembershipDto.Membership.MEMBER, 0);
            assertEquals("expected AccessDeniedException", true, false);
        } catch (AccessDeniedException e) { assertEquals(true, true); }
    }

    /** UserService.updateUser(UserDto, Long, MembershipDto.Type, Integer). */
    @Test
    public void testUserServiceUpdateUser() {
        userService.updateUser(user1, unit1.getId(), MembershipDto.Membership.MEMBER, 0);

        try { // hasPermission(#unitId, 'V_UNIT_ADMIN')
            userService.createUser(user1, unit2.getId(), MembershipDto.Membership.MEMBER, 0);
            assertEquals("expected AccessDeniedException", true, false);
        } catch (AccessDeniedException e) { assertEquals(true, true); }
    }

    /** UserService.updateUserPassword. */
    @Test
    public void testUserServiceUpdateUserPassword() {
        userService.updateUserPassword(user1.getId(), "a", "newPassword");

        User user = new User();
        user.setUsername("a");
        user.setFullname("a a");
        user.setPassword("a");
        user = userService.createUser(user, unit1.getId(), MembershipDto.Membership.MEMBER, 0);
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

    /** UnitService.createUnit. */
    @Test
    public void testCreateUnit() {
        final Unit toCreate = new Unit();
        toCreate.setName(UNITNAME);
        toCreate.setMetadata(getDefaultUnitMetadata());
        toCreate.setLimitedSmss(LIMITED_SMSS);

        unitService.createUnit(toCreate);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.createUnit(toCreate);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.getById. */
    @Test
    public void testGetById() {
        assertEquals(unit1.getName(), unitService.getById(unit1.getId()).getName());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getById(unit1.getId());
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
        final PatientDto toCreate = new PatientDto();
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

        unitService.createPatient(toCreate);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.createPatient(toCreate);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.updatePatient. */
    @Test
    public void testUpdatePatient() {
        final PatientDto toCreate = new PatientDto();
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

        final PatientDto created = unitService.createPatient(toCreate);
        // CHECKSTYLE:OFF
        final String NEW_PHONE_NUMBER = "666123456";
        // CHECKSTYLE:ON
        created.setPhoneNumber(NEW_PHONE_NUMBER);
        unitService.updatePatient(created);
        assertEquals(1, unitService.findPatients(unit1.getId(), null, created.getPhoneNumber(), null).size());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            created.setPhoneNumber(PHONE_NUMBER);

            unitService.updatePatient(created);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }

        login(user1);
        assertEquals(1, unitService.findPatients(unit1.getId(), null, NEW_PHONE_NUMBER, null).size());
        logout();
    }

    /** UnitService.getPatientsByUnit. */
    @Test
    public void testGetPatientsByUnit() {
        final PatientDto toCreate = new PatientDto();
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

        unitService.createPatient(toCreate);
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
        final PatientDto toCreate = new PatientDto();
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

        final PatientDto created = unitService.createPatient(toCreate);
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
        final PatientDto toCreate = new PatientDto();
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

        final PatientDto created1 = unitService.createPatient(toCreate);
        unitService.deletePatient(created1.getId());
        final PatientDto created2 = unitService.createPatient(created1);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.deletePatient(created2.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.createMedicalHelpCategory. */
    @Test
    public void testCreateMedicalHelpCategory() {
        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
        toCreate.setUnit(unit1);
        toCreate.setName(MHC_NAME);
        toCreate.setSmsText(MHC_MSGTEXT);
        toCreate.setType(MedicalHelpCategoryDto.TYPE_STANDARD);
        toCreate.setColor(MHC_COLOR);
        toCreate.setTime(MHC_TIME);

        unitService.createMedicalHelpCategory(toCreate);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.createMedicalHelpCategory(toCreate);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.updateMedicalHelpCategory. */
    @Test
    public void testUpdateMedicalHelpCategory() {
        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
        toCreate.setUnit(unit1);
        toCreate.setName(MHC_NAME);
        toCreate.setSmsText(MHC_MSGTEXT);
        toCreate.setType(MedicalHelpCategoryDto.TYPE_STANDARD);
        toCreate.setColor(MHC_COLOR);
        toCreate.setTime(MHC_TIME);

        final MedicalHelpCategoryDto created = unitService.createMedicalHelpCategory(toCreate);
        // CHECKSTYLE:OFF
        final String NEW_MHC_COLOR = "CCBBAA";
        // CHECKSTYLE:ON
        created.setColor(NEW_MHC_COLOR);
        unitService.updateMedicalHelpCategory(created);
        assertEquals(NEW_MHC_COLOR, unitService.getMedicalHelpCategoryById(created.getId()).getColor());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            created.setColor(MHC_COLOR);

            unitService.updateMedicalHelpCategory(created);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }

        login(user1);
        assertEquals(NEW_MHC_COLOR, unitService.getMedicalHelpCategoryById(created.getId()).getColor());
        logout();
    }

    /** UnitService.getMedicalHelpCategoriesByUnit. */
    @Test
    public void testGetMedicalHelpCategoriesByUnit() {
        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
        toCreate.setUnit(unit1);
        toCreate.setName(MHC_NAME);
        toCreate.setSmsText(MHC_MSGTEXT);
        toCreate.setType(MedicalHelpCategoryDto.TYPE_STANDARD);
        toCreate.setColor(MHC_COLOR);
        toCreate.setTime(MHC_TIME);

        unitService.createMedicalHelpCategory(toCreate);
        assertEquals(1, unitService.getMedicalHelpCategoriesByUnit(unit1.getId(),
                MedicalHelpCategoryDto.TYPE_STANDARD).size());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getMedicalHelpCategoriesByUnit(unit1.getId(), MedicalHelpCategoryDto.TYPE_STANDARD);
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.getMedicalHelpCategoryById. */
    @Test
    public void testGetMedicalHelpCategoryById() {
        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
        toCreate.setUnit(unit1);
        toCreate.setName(MHC_NAME);
        toCreate.setSmsText(MHC_MSGTEXT);
        toCreate.setType(MedicalHelpCategoryDto.TYPE_STANDARD);
        toCreate.setColor(MHC_COLOR);
        toCreate.setTime(MHC_TIME);

        final MedicalHelpCategoryDto created = unitService.createMedicalHelpCategory(toCreate);
        assertEquals(MHC_NAME, unitService.getMedicalHelpCategoryById(created.getId()).getName());

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.getMedicalHelpCategoryById(created.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }

    /** UnitService.deleteMedicalHelpCategory. */
    @Test
    public void testDeleteMedicalHelpCategory() {
        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
        toCreate.setUnit(unit1);
        toCreate.setName(MHC_NAME);
        toCreate.setSmsText(MHC_MSGTEXT);
        toCreate.setType(MedicalHelpCategoryDto.TYPE_STANDARD);
        toCreate.setColor(MHC_COLOR);
        toCreate.setTime(MHC_TIME);

        final MedicalHelpCategoryDto created1 = unitService.createMedicalHelpCategory(toCreate);
        unitService.deleteMedicalHelpCategory(created1.getId());
        final MedicalHelpCategoryDto created2 = unitService.createMedicalHelpCategory(created1);

        logout();
        try { // hasRole('ROLE_AUTHENTICATED')
            unitService.deleteMedicalHelpCategory(created2.getId());
            assertEquals("expected AuthenticationCredentialsNotFoundException", true, false);
        } catch (AuthenticationCredentialsNotFoundException e) { assertEquals(true, true); }
    }
}
