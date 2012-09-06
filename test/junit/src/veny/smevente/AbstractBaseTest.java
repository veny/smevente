package veny.smevente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import veny.smevente.dao.orientdb.DatabaseWrapper;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.service.UnitService;
import veny.smevente.service.UserService;

import com.orientechnologies.orient.core.sql.OCommandSQL;

/**
 * Base class for the <i>Smevente</i> unit tests.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.11.2010
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/appctx-common.xml", "/appctx-persistence-junit.xml", "/appctx-validation.xml" })
public abstract class AbstractBaseTest extends AbstractJUnit4SpringContextTests {

    // CHECKSTYLE:OFF
    @Autowired
    protected UserService userService;
    @Autowired
    protected UnitService unitService;
//    @Autowired
//    protected SmsService smsService;
    // CHECKSTYLE:ON


    /**
     * Stuff before all tests.
     */
    @Before
    public void deleteEntries() {
        final DatabaseWrapper dbw = (DatabaseWrapper) applicationContext.getBean("databaseWrapper");
        dbw.get().command(new OCommandSQL("DELETE FROM Unit")).execute();
        dbw.get().command(new OCommandSQL("DELETE FROM Membership")).execute();
        dbw.get().command(new OCommandSQL("DELETE FROM User")).execute();
    }


    // --------------------------------------------------- User Assistant Stuff

    // CHECKSTYLE:OFF
    public static final String USERNAME = "max.mustermann";
    public static final String PASSWORD = "asdRT12";
    public static final String FIRSTNAME = "Max";
    public static final String SURNAME = "Mustermann";
    public static final String FULLNAME = FIRSTNAME + " " + SURNAME;
    // CHECKSTYLE:ON

    /**
     * @return new default user
     */
    protected User createDefaultUser() {
        return createUser(USERNAME, PASSWORD, FULLNAME, false);
    }
    /**
     *
     * @param username username
     * @param password password
     * @param fullname full name
     * @param root whether the user is root
     * @return new default user
     */
    protected User createUser(
            final String username, final String password, final String fullname, final boolean root) {
        return userService.createUser(username, password, fullname, root);
    }
    /**
     * @param user user to assert
     */
    protected void assertDefaultUser(final User user) {
        assertNotNull(user);
        assertNotNull(user.getId());
        assertTrue(user.getId().toString().length() > 0);
        assertEquals(USERNAME, user.getUsername());
        assertEquals(userService.encodePassword(PASSWORD), user.getPassword());
        assertEquals(FULLNAME, user.getFullname());
        assertEquals(false, user.isRoot());
    }


    // --------------------------------------------------- Unit Assistant Stuff

    // CHECKSTYLE:OFF
    public static final String UNITNAME = "unitXY";
    // CHECKSTYLE:ON

    /** @return a new created default unit */
    protected Unit createDefaultUnit() {
        return createUnit(
                UNITNAME, "unit's desc", Unit.TextVariant.PATIENT.toString(),
                11L, "usr:x,passwd:y");
    }
    /**
     * Creates a new unit with given attributes.
     * @param name unit name
     * @param description unit's description
     * @param type unit's type
     * @param limitedSmss limited amount of SMS that can be sent
     * @param smsEngine SMS engine configuration
     * @return a new unit created
     */
    protected Unit createUnit(
            final String name, final String description, final String type,
            final Long limitedSmss, final String smsEngine) {
        final Unit toCreate = new Unit();
        toCreate.setName(name);
        toCreate.setDescription(description);
        toCreate.setType(type);
        toCreate.setLimitedSmss(limitedSmss);
        toCreate.setSmsEngine(smsEngine);
        return unitService.createUnit(toCreate);
    }
    /**
     * Asserts default unit.
     * @param unit unit to be checked
     */
    protected void assertDefaultUnit(final Unit unit) {
        assertNotNull(unit);
        assertNotNull(unit.getId());
        assertEquals(UNITNAME, unit.getName());
        assertEquals("unit's desc", unit.getDescription());
        assertEquals(Unit.TextVariant.PATIENT.toString(), unit.getType());
        assertTrue(11L == unit.getLimitedSmss());
        assertEquals("usr:x,passwd:y", unit.getSmsEngine());
        assertNull(unit.getMembers());
    }

//    // CHECKSTYLE:OFF
//    public static final String PHONE_NUMBER = "606123456";
//    public static final String BIRTH_NUMBER = "7001012222";
//    // CHECKSTYLE:ON
//
//    /** @return a new created default patient */
//    protected PatientDto createDefaultPatient() {
//        final UnitDto unit = createDefaultUnit();
//        return createPatient(FIRSTNAME, SURNAME, PHONE_NUMBER, BIRTH_NUMBER, unit);
//    }
//    /**
//     * Creates a new patient with given attributes.
//     * @param firstname firstname
//     * @param surname surname
//     * @param phoneNumber phone number
//     * @param birthNumber birth number
//     * @param unit unit to be the patient put into
//     * @return a new created patient
//     */
//    protected PatientDto createPatient(
//            final String firstname, final String surname,
//            final String phoneNumber, final String birthNumber, final UnitDto unit) {
//        final PatientDto toCreate = new PatientDto();
//        toCreate.setUnit(unit);
//        toCreate.setFirstname(firstname);
//        toCreate.setSurname(surname);
//        toCreate.setPhoneNumber(phoneNumber);
//        toCreate.setBirthNumber(birthNumber);
//        return unitService.createPatient(toCreate);
//    }
//    /**
//     * Asserts default patient.
//     * @param patient patient to be checked
//     * @param aggregated whether to assert the aggregated objects too
//     */
//    protected void assertDefaultPatient(final PatientDto patient, final boolean aggregated) {
//        assertNotNull(patient);
//        assertNotNull(patient.getId());
//        if (aggregated) { assertDefaultUnit(patient.getUnit()); }
//        assertEquals(FIRSTNAME, patient.getFirstname());
//        assertEquals(SURNAME, patient.getSurname());
//        assertEquals(PHONE_NUMBER, patient.getPhoneNumber());
//        assertEquals(BIRTH_NUMBER, patient.getBirthNumber());
//    }
//
//    // CHECKSTYLE:OFF
//    public static final String MHC_NAME = "MedicalHelpCategory XY";
//    public static final String MHC_COLOR = "AABBCC";
//    public static final long MHC_TIME = 60;
//    public static final String MHC_MSGTEXT = "some message with replace #{time}";
//    // CHECKSTYLE:ON
//
//    /** @return a new default MHC */
//    protected MedicalHelpCategoryDto createDefaultMedicalHelpCategory() {
//        return createDefaultMedicalHelpCategory(null);
//    }
//    /**
//     * @param categoryType the type of category
//     * @return a new default MHC with specified category type
//     */
//    protected MedicalHelpCategoryDto createDefaultMedicalHelpCategory(final Short categoryType) {
//        final UnitDto unit = createDefaultUnit();
//        return createMedicalHelpCategory(MHC_NAME, MHC_COLOR, MHC_TIME, MHC_MSGTEXT, categoryType, unit);
//    }
//    /**
//     * Creates a new MHC with given attributes.
//     * @param name name
//     * @param color color
//     * @param time length of the transaction
//     * @param msgText SMS test
//     * @param unit unit where the MHC will be put into
//     * @return a new created MHC
//     */
//    protected MedicalHelpCategoryDto createMedicalHelpCategory(
//            final String name, final String color, final long time, final String msgText, final UnitDto unit) {
//        return createMedicalHelpCategory(name, color, time, msgText, null, unit);
//    }
//    /**
//     * Creates a new MHC with given attributes.
//     * @param name name
//     * @param color color
//     * @param time length of the transaction
//     * @param msgText SMS test
//     * @param categoryType the type - for special types not all properties are set
//     * @param unit unit where the MHC will be put into
//     * @return a new created MHC
//     */
//    protected MedicalHelpCategoryDto createMedicalHelpCategory(
//            final String name, final String color, final long time,
//            final String msgText, final Short categoryType, final UnitDto unit) {
//        final MedicalHelpCategoryDto toCreate = new MedicalHelpCategoryDto();
//        toCreate.setUnit(unit);
//        toCreate.setName(name);
//        toCreate.setSmsText(msgText);
//        toCreate.setType(categoryType);
//        if (categoryType == null || categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            toCreate.setColor(color);
//            toCreate.setTime(time);
//        }
//        return unitService.createMedicalHelpCategory(toCreate);
//    }
//    /**
//     * Asserts a default MHC.
//     * @param mhc MHC to be checked
//     * @param categoryType the type - for special types not all properties are set
//     * @param aggregated whether to assert the aggregated objects too
//     */
//    protected void assertDefaultMedicalHelpCategory(final MedicalHelpCategoryDto mhc, final Short categoryType,
//            final boolean aggregated) {
//        assertNotNull(mhc);
//        assertNotNull(mhc.getId());
//        if (aggregated) { assertDefaultUnit(mhc.getUnit()); }
//        assertEquals(MHC_NAME, mhc.getName());
//        assertEquals(MHC_MSGTEXT, mhc.getSmsText());
//        assertEquals(categoryType, mhc.getType());
//        if (categoryType == null || categoryType.shortValue() == MedicalHelpCategoryDto.TYPE_STANDARD) {
//            assertEquals(MHC_COLOR, mhc.getColor());
//            assertEquals(MHC_TIME, mhc.getTime());
//        }
//    }
//
//    // CHECKSTYLE:OFF
//    public static final String SMS_TEXT = "SMS text AB";
//    @SuppressWarnings("deprecation")
//    public static final Date SMS_MH_START = new Date(110 /* the year minus 1900 */, 10, 24);
//    public static final int SMS_MH_LEN = 20;
//    public static final String SMS_NOTICE = "notice to SMS";
//    // CHECKSTYLE:ON
//
//    /** @return a new created default SMS */
//    protected SmsDto createDefaultSms() {
//        final User author = createDefaultUser();
//        final PatientDto patient = createDefaultPatient();
//        final MedicalHelpCategoryDto mhc = createMedicalHelpCategory(
//                MHC_NAME, MHC_COLOR, MHC_TIME, MHC_MSGTEXT, null, patient.getUnit());
//        return createSms(SMS_TEXT, SMS_MH_START, SMS_MH_LEN, SMS_NOTICE, author, patient, mhc);
//    }
//    /**
//     * Creates a new SMS with given attributes.
//     * @param text text
//     * @param startTime start time
//     * @param len length of the transaction
//     * @param notice notice
//     * @param author author
//     * @param patient patient (recipient)
//     * @param mhc MHC
//     * @return a new created SMS
//     */
//    protected SmsDto createSms(final String text, final Date startTime, final int len, final String notice,
//            final User author, final PatientDto patient, final MedicalHelpCategoryDto mhc) {
//        final SmsDto sms = new SmsDto();
//        sms.setAuthor(author);
//        sms.setPatient(patient);
//        sms.setMedicalHelpCategory(mhc);
//        sms.setText(text);
//        sms.setMedicalHelpStartTime(startTime);
//        sms.setMedicalHelpLength(len);
//        sms.setNotice(notice);
//        return smsService.createSms(sms);
//    }
//    /**
//     * Asserts default SMS.
//     * @param sms SMS to be checked
//     * @param aggregated whether to assert the aggregated objects too
//     */
//    protected void assertDefaultSms(final SmsDto sms, final boolean aggregated) {
//        assertNotNull(sms);
//        assertNotNull(sms.getId());
//        if (aggregated) {
//            assertDefaultUser(sms.getAuthor());
//            assertDefaultPatient(sms.getPatient(), false);
//            assertDefaultMedicalHelpCategory(sms.getMedicalHelpCategory(), null, false);
//        }
//        assertEquals(SMS_TEXT, sms.getText());
//        assertEquals(SMS_MH_START, sms.getMedicalHelpStartTime());
//        assertEquals(SMS_MH_LEN, sms.getMedicalHelpLength());
//        assertEquals(SMS_NOTICE, sms.getNotice());
//        assertEquals(new Integer(0), sms.getStatus());
//        assertNull(sms.getSent());
//    }

}
