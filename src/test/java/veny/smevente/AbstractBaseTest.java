package veny.smevente;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

import veny.smevente.dao.orientdb.DatabaseWrapper;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.service.EventService;
import veny.smevente.service.UnitService;
import veny.smevente.service.UserService;

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
    @Autowired
    protected EventService eventService;
    // CHECKSTYLE:ON


    /**
     * Initializes and creates embedded database.
     * @throws Exception if something goes wrong
     */
    @BeforeClass
    public static void initEmbeddedServer() throws Exception {
        final Properties dbProp = new Properties();
        dbProp.load(AbstractBaseTest.class.getResourceAsStream("../../db.properties"));
        final String dbUrl = dbProp.getProperty("db.url");

        if (dbUrl.startsWith("memory:")) {
            final OServer server = OServerMain.create();
            server.startup(AbstractBaseTest.class.getResourceAsStream("../../embedded-server-junit-config.xml"));
            server.activate();

            // create DB if not exists
            final OObjectDatabaseTx db = new OObjectDatabaseTx(dbUrl);
            if (!db.exists()) {
                db.create();
            }
            db.close();
        }
    }

    /**
     * Stuff before all tests.
     */
    @Before
    public void deleteEntries() {
        final DatabaseWrapper dbw = (DatabaseWrapper) applicationContext.getBean("databaseWrapper");

        // delete data from DB
        final OObjectDatabaseTx db = dbw.get();
        db.command(new OCommandSQL("DELETE FROM Event")).execute();
        db.command(new OCommandSQL("DELETE FROM Procedure")).execute();
        db.command(new OCommandSQL("DELETE FROM Customer")).execute();
        db.command(new OCommandSQL("DELETE FROM Membership")).execute();
        db.command(new OCommandSQL("DELETE FROM Unit")).execute();
        db.command(new OCommandSQL("DELETE FROM User")).execute();
//        db.command(new OCommandSQL("TRUNCATE CLASS Event POLYMORPHIC")).execute();
//        db.command(new OCommandSQL("TRUNCATE CLASS Procedure POLYMORPHIC")).execute();
//        db.command(new OCommandSQL("TRUNCATE CLASS Customer POLYMORPHIC")).execute();
//        db.command(new OCommandSQL("TRUNCATE CLASS Membership POLYMORPHIC")).execute();
//        db.command(new OCommandSQL("TRUNCATE CLASS Unit POLYMORPHIC")).execute();
//        db.command(new OCommandSQL("TRUNCATE CLASS User POLYMORPHIC")).execute();
        db.close();
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
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullname(fullname);
        user.setRoot(root);
        user.setTimezone("Europe/Prague");
        final User created = userService.createUser(user);
        return created;
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
    public static final String DEFAULT_OPTIONS =
            "{\"sms\":{\"gateway\":\"sms.sluzba.cz\",\"username\":\"foo\",\"password\":\"bar\"}}";
    // CHECKSTYLE:ON

    /** @return a new created default unit */
    protected Unit createDefaultUnit() {
        return createUnit(
                UNITNAME, "unit's desc", Unit.TextVariant.PATIENT, 11L, DEFAULT_OPTIONS);
    }
    /**
     * Creates a new unit with given attributes.
     * @param name unit name
     * @param description unit's description
     * @param variant unit's text variant
     * @param limitedSmss limited amount of SMS that can be sent
     * @param options unit options
     * @return a new unit created
     */
    protected Unit createUnit(
            final String name, final String description, final Unit.TextVariant variant,
            final Long limitedSmss, final String options) {
        final Unit toCreate = new Unit();
        toCreate.setName(name);
        toCreate.setDescription(description);
        toCreate.setType(null == variant ? null : variant.toString());
        toCreate.setMsgLimit(limitedSmss);
        toCreate.setOptions(options);
        return unitService.storeUnit(toCreate);
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
        assertTrue(11L == unit.getMsgLimit());
        assertEquals(DEFAULT_OPTIONS, unit.getOptions());
    }

    // ------------------------------------------------ Patient Assistant Stuff

    // CHECKSTYLE:OFF
    public static final String PHONE_NUMBER = "606123456";
    public static final String BIRTH_NUMBER = "7001012222";
    // CHECKSTYLE:ON

    /** @return a new created default customer */
    protected Customer createDefaultCustomer() {
        final Unit unit = createDefaultUnit();
        return createCustomer(FIRSTNAME, SURNAME, PHONE_NUMBER, BIRTH_NUMBER, unit);
    }
    /**
     * Creates a new customer with given attributes.
     * @param firstname firstname
     * @param surname surname
     * @param phoneNumber phone number
     * @param birthNumber birth number
     * @param unit unit to be the patient put into
     * @return a new created customer
     */
    protected Customer createCustomer(
            final String firstname, final String surname,
            final String phoneNumber, final String birthNumber, final Unit unit) {
        final Customer toCreate = new Customer();
        toCreate.setFirstname(firstname);
        toCreate.setSurname(surname);
        toCreate.setPhoneNumber(phoneNumber);
        toCreate.setBirthNumber(birthNumber);
        toCreate.setUnitId(unit.getId());
        toCreate.setEmail("somebady@domain.com");
        toCreate.setSendingChannel(Event.CHANNEL_EMAIL | Event.CHANNEL_SMS);
        return unitService.storeCustomer(toCreate);
    }
    /**
     * Asserts default patient.
     * @param patient patient to be checked
     * @param aggregated whether to assert the aggregated objects too
     */
    protected void assertDefaultPatient(final Customer patient, final boolean aggregated) {
        assertNotNull(patient);
        assertNotNull(patient.getId());
        if (aggregated) {
            assertDefaultUnit(patient.getUnit());
        }
        assertEquals(FIRSTNAME, patient.getFirstname());
        assertEquals(SURNAME, patient.getSurname());
        assertEquals(PHONE_NUMBER, patient.getPhoneNumber());
        assertEquals(BIRTH_NUMBER, patient.getBirthNumber());
    }


    // ---------------------------------------------- Procedure Assistant Stuff

    // CHECKSTYLE:OFF
    public static final String PROCEDURE_NAME = "Procedure XY";
    public static final String PROCEDURE_COLOR = "AABBCC";
    public static final int PROCEDURE_TIME = 60;
    public static final String PROCEDURE_MSGTEXT = "some message with replace #{time}";
    // CHECKSTYLE:ON

    /** @return a new default procedure */
    protected Procedure createProcedure() {
        return createDefaultProcedure(null);
    }
    /**
     * @param type type of procedure
     * @return a new default procedure with specified type
     */
    protected Procedure createDefaultProcedure(final Event.Type type) {
        final Unit unit = createDefaultUnit();
        return createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT, type, unit);
    }
    /**
     * Creates a new procedure with given attributes.
     * @param name name
     * @param color color
     * @param time length of the transaction
     * @param msgText message test
     * @param unit unit where the procedure will be put into
     * @return a new created procedure
     */
    protected Procedure createProcedure(
            final String name, final String color, final int time, final String msgText, final Unit unit) {
        return createProcedure(name, color, time, msgText, null, unit);
    }
    /**
     * Creates a new procedure with given attributes.
     * @param name name
     * @param color color
     * @param time length of the transaction
     * @param msgText message test
     * @param type the type - for special types not all properties are set
     * @param unit unit where the procedure will be put into
     * @return a new created procedure
     */
    protected Procedure createProcedure(
            final String name, final String color, final int time,
            final String msgText, final Event.Type type, final Unit unit) {
        final Procedure toCreate = new Procedure();
        toCreate.setUnit(unit);
        toCreate.setName(name);
        toCreate.setMessageText(msgText);
        toCreate.setType(null == type ? null : type.toString());
        if (type == null || type == Event.Type.IN_CALENDAR) {
            toCreate.setColor(color);
            toCreate.setTime(time);
        }
        return unitService.storeProcedure(toCreate);
    }
    /**
     * Asserts a default procedure.
     * @param proc procedure to be checked
     * @param type type - for special types not all properties are set
     * @param aggregated whether to assert the aggregated objects too
     */
    protected void assertDefaultProcedure(final Procedure proc, final Event.Type type,
            final boolean aggregated) {
        assertNotNull(proc);
        assertNotNull(proc.getId());
        if (aggregated) {
            assertDefaultUnit(proc.getUnit());
        }
        assertEquals(PROCEDURE_NAME, proc.getName());
        assertEquals(PROCEDURE_MSGTEXT, proc.getMessageText());
        assertEquals(type.toString(), proc.getType());
        if (Event.Type.IN_CALENDAR == proc.enumType()) {
            assertEquals(PROCEDURE_COLOR, proc.getColor());
            assertEquals(PROCEDURE_TIME, proc.getTime());
        }
    }



    // -------------------------------------------------- Event Assistant Stuff

    // CHECKSTYLE:OFF
    public static final String EVENT_TEXT = "event text AB";
    @SuppressWarnings("deprecation")
    public static final Date EVENT_START = new Date(110 /* the year minus 1900 */, 10, 24);
    public static final int EVENT_LEN = 20;
    public static final String EVENT_NOTICE = "notice to SMS";
    // CHECKSTYLE:ON

    /** @return a new created default event */
    protected Event createDefaultEvent() {
        final User author = createDefaultUser();
        final Customer customer = createDefaultCustomer();
        final Procedure procedure = createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME,
                PROCEDURE_MSGTEXT, null, customer.getUnit());
        return createEvent(EVENT_TEXT, EVENT_START, EVENT_LEN, EVENT_NOTICE, author, customer, procedure);
    }
    /**
     * Creates a new event with given attributes.
     * @param text text
     * @param startTime start time
     * @param len length of the event
     * @param notice notice
     * @param author author
     * @param customer customer (recipient)
     * @param procedure procedure
     * @return a new created event
     */
    protected Event createEvent(final String text, final Date startTime, final int len, final String notice,
            final User author, final Customer customer, final Procedure procedure) {
        final Event event = new Event();
        event.setAuthor(author);
        event.setCustomer(customer);
        event.setProcedure(procedure);
        event.setText(text);
        event.setStartTime(startTime);
        event.setLength(len);
        event.setNotice(notice);
        return eventService.storeEvent(event);
    }
    /**
     * Asserts default event.
     * @param event event to be checked
     * @param aggregated whether to assert the aggregated objects too
     */
    protected void assertDefaultEvent(final Event event, final boolean aggregated) {
        assertNotNull(event);
        assertNotNull(event.getId());
        if (aggregated) {
            assertDefaultUser(event.getAuthor());
            assertDefaultPatient(event.getCustomer(), false);
            assertDefaultProcedure(event.getProcedure(), null, false);
        }
        assertEquals(EVENT_TEXT, event.getText());
        assertEquals(EVENT_START, event.getStartTime());
        assertEquals(EVENT_LEN, event.getLength());
        assertEquals(EVENT_NOTICE, event.getNotice());
        assertNull(event.getSent());
    }

}
