package veny.smevente.dao.orientdb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * This class represents a management interface for schema manipulation and initialization.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.6.2013
 */
public class Schema {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Schema.class.getName());


    /** Dependency. */
    @Autowired
    private DatabaseWrapper dbWrapper;

    /**
     * Cleans the schema.
     * Attention: this is a destructive action without rollback.
     */
    public void drop() {
        final String[] classes = {
                Procedure.class.getSimpleName(),
                Customer.class.getSimpleName(),
                Membership.class.getSimpleName(),
                Unit.class.getSimpleName(),
                User.class.getSimpleName(),
                Event.class.getSimpleName(),
                AbstractEntity.class.getSimpleName()
        };

        final OObjectDatabaseTx db = dbWrapper.get();

        for (final String clazz : classes) {
            if (db.getMetadata().getSchema().existsClass(clazz)) {
                db.getMetadata().getSchema().dropClass(clazz);
                LOG.info("class droped, name=" + clazz);
            } else {
                LOG.info("class cannot be droped (not found), name=" + clazz);
            }
        }

        db.close();
    }

    /**
     * Creates new DB schema.
     */
    public void create() {
        drop();

        final OObjectDatabaseTx db = dbWrapper.get();

        if (!db.getMetadata().getSchema().existsClass(AbstractEntity.class.getSimpleName())) {
            // AbstractEntity
            OClass entity = db.getMetadata().getSchema().createAbstractClass(AbstractEntity.class.getSimpleName());
            entity.createProperty("revision", OType.STRING);
            entity.createProperty("updatedAt", OType.DATETIME);
            entity.createProperty("updatedBy", OType.STRING);
            entity.createProperty("deletedAt", OType.DATETIME);
            entity.createProperty("deletedBy", OType.STRING);
            LOG.info("class created, name=" + entity.getName());
            // User
            OClass user = db.getMetadata().getSchema().createClass(User.class.getSimpleName(), entity);
            user.createProperty("username", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("password", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("fullname", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("timezone", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("lastLoggedIn", OType.DATETIME);
            user.createProperty("root", OType.BOOLEAN);
            LOG.info("class created, name=" + user.getName());
            // Unit
            OClass unit = db.getMetadata().getSchema().createClass(Unit.class.getSimpleName(), entity);
            unit.createProperty("name", OType.STRING).setMandatory(true).setNotNull(true);
            unit.createProperty("description", OType.STRING);
            unit.createProperty("email", OType.STRING).setMandatory(true).setNotNull(true);
            unit.createProperty("type", OType.STRING); // null == PATIENT
            unit.createProperty("options", OType.STRING).setMandatory(true).setNotNull(true);
            unit.createProperty("msgLimit", OType.LONG);
            LOG.info("class created, name=" + unit.getName());
            // Membership
            OClass membership = db.getMetadata().getSchema().createClass(Membership.class.getSimpleName(), entity);
            membership.createProperty("user", OType.LINK, user).setMandatory(true).setNotNull(true);
            membership.createProperty("unit", OType.LINK, unit).setMandatory(true).setNotNull(true);
            membership.createProperty("role", OType.STRING).setMandatory(true).setNotNull(true);
            membership.createProperty("significance", OType.INTEGER);
            LOG.info("class created, name=" + membership.getName());
            // Customer
            OClass customer = db.getMetadata().getSchema().createClass(Customer.class.getSimpleName(), entity);
            customer.createProperty("unit", OType.LINK, unit).setMandatory(true).setNotNull(true);
            customer.createProperty("firstname", OType.STRING);
            customer.createProperty("surname", OType.STRING).setMandatory(true).setNotNull(true);
            customer.createProperty("asciiFullname", OType.STRING).setMandatory(true).setNotNull(true);
            customer.createProperty("phoneNumber", OType.STRING);
            customer.createProperty("email", OType.STRING);
            customer.createProperty("birthNumber", OType.STRING);
            customer.createProperty("degree", OType.STRING);
            customer.createProperty("street", OType.STRING);
            customer.createProperty("city", OType.STRING);
            customer.createProperty("zipCode", OType.STRING);
            customer.createProperty("employer", OType.STRING);
            customer.createProperty("careers", OType.STRING);
            customer.createProperty("sendingChannel", OType.INTEGER);
            LOG.info("class created, name=" + customer.getName());
            // Procedure
            OClass procedure = db.getMetadata().getSchema().createClass(Procedure.class.getSimpleName(), entity);
            procedure.createProperty("unit", OType.LINK, unit).setMandatory(true).setNotNull(true);
            procedure.createProperty("name", OType.STRING).setMandatory(true).setNotNull(true);
            procedure.createProperty("messageText", OType.STRING).setMandatory(true).setNotNull(true);
            procedure.createProperty("type", OType.STRING); // null == Event.Type.IN_CALENDAR
            procedure.createProperty("color", OType.STRING);
            procedure.createProperty("time", OType.INTEGER);
            LOG.info("class created, name=" + procedure.getName());
            // Event
            OClass event = db.getMetadata().getSchema().createClass(Event.class.getSimpleName(), entity);
            event.createProperty("author", OType.LINK, user).setMandatory(true).setNotNull(true);
            event.createProperty("customer", OType.LINK, customer).setMandatory(true).setNotNull(true);
            event.createProperty("procedure", OType.LINK, procedure).setMandatory(true).setNotNull(true);
            event.createProperty("text", OType.STRING).setMandatory(true).setNotNull(true);
            event.createProperty("notice", OType.STRING);
            event.createProperty("startTime", OType.DATETIME); // can be 'null' for events of type IMMEDIATE_MESSAGE
            event.createProperty("length", OType.INTEGER); // can be 'null' for events of type IMMEDIATE_MESSAGE
            event.createProperty("sent", OType.DATETIME);
            event.createProperty("sendAttemptCount", OType.INTEGER);
            event.createProperty("type", OType.STRING);
            LOG.info("class created, name=" + event.getName());

            // Indexes
            final OCommandSQL createIndex = new OCommandSQL("CREATE INDEX Event.startTime NOTUNIQUE");
            db.command(createIndex).execute(new Object[0]);

            db.close();
        }
    }

    /**
     * Initializes schema with a basic data set.
     */
    public void sampleData() {
        final OObjectDatabaseTx db = dbWrapper.get();

        Map<String, ORID> rids = new HashMap<String, ORID>();

        // Users
        Map<String, String> userDef = new HashMap<String, String>();
        userDef.put("veny", "User SET username = 'veny', password = 'SHA:40bd001563085fc35165329ea1ff5c5ecbdbbeef', fullname = 'Vaclav Novy', timezone = 'Europe/Prague', root = true"); // password: 123
        userDef.put("max",  "User SET username = 'max',  password = 'SHA:40bd001563085fc35165329ea1ff5c5ecbdbbeef', fullname = 'Max Mustermann', timezone = 'Europe/London'");
        // Units
        Map<String, String> unitDef = new HashMap<String, String>();
        unitDef.put("foo", "Unit SET name = 'Foo', description = 'Desc of Foo', email = 'vaclav.sykora@gmail.com', type = 'PATIENT',  options = '{\"sms\":{\"gateway\":\"sms.sluzba.cz\",\"username\":\"foo\",\"password\":\"bar\"}}'");
        unitDef.put("bar", "Unit SET name = 'Bar', description = 'Desc of Bar', email = 'vaclav.sykora@gmail.com', type = 'CUSTOMER', options = '{\"sms\":{\"gateway\":\"sms.sluzba.cz\",\"username\":\"alfa\",\"password\":\"bravo\"}}'");
        // Membership
        Map<String, String> membDef = new HashMap<String, String>();
        membDef.put("m1", "Membership SET user = %veny%, unit = %foo%, role = 'ADMIN', significance = 10");
        membDef.put("m2", "Membership SET user = %veny%, unit = %bar%, role = 'MEMBER', significance = 20");
        membDef.put("m3", "Membership SET user = %max%, unit = %foo%, role = 'MEMBER', significance = 20");
        membDef.put("m4", "Membership SET user = %max%, unit = %bar%, role = 'ADMIN', significance = 40");
        // Customer
        Map<String, String> patDef = new HashMap<String, String>();
        patDef.put("JanNovak",      "Customer SET unit = %foo%, firstname = 'Jan',  surname = 'Novák',     asciiFullname = 'JAN NOVAK',      birthNumber = '7001012000', phoneNumber = '606146177', email = 'vaclav.sykora@gmail.com'");
        patDef.put("PetrZlutoucky", "Customer SET unit = %foo%, firstname = 'Petr', surname = 'Žluťoučký', asciiFullname = 'PETR ZLUTOUCKY', birthNumber = '7002023000', phoneNumber = '606146177', email = 'vaclav.sykora@gmail.com'");
        patDef.put("LindaModra",    "Customer SET unit = %foo%, firstname = 'Lída', surname = 'Modrá',     asciiFullname = 'LIDA MODRA',     birthNumber = '7051011000', phoneNumber = '606146177'");
        patDef.put("me",            "Customer SET unit = %bar%, firstname = 'veny', surname = 'V',         asciiFullname = 'VENY V',         birthNumber = '7004045000', phoneNumber = '606146177'");
        patDef.put("SonDablik",     "Customer SET unit = %bar%, firstname = 'Šón',  surname = 'Ďáblík',    asciiFullname = 'SON DABLIK',     birthNumber = '7008088889', phoneNumber = '012345677'");
        // Procedure
        Map<String, String> procDef = new HashMap<String, String>();
        procDef.put("beleni", "Procedure SET unit = %foo%, name = 'Bělení', messageText = 'Prijdte na beleni dne #{date} v #{time}, #{doctor}', type = 'IN_CALENDAR', color = 'FF0000', time = 30");
        procDef.put("trhani", "Procedure SET unit = %foo%, name = 'Extrakce', messageText = 'Trhani, #{date}, #{time}, #{doctor}', type = 'IN_CALENDAR', color = '00FF00', time = 60");
        procDef.put("p3", "Procedure SET unit = %foo%, name = 'Dovolená', messageText = 'Mame dovolenou', type = 'IMMEDIATE_MESSAGE'");
        // Event
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Date today1 = new Date();
        today1.setHours(10); today1.setMinutes(0); today1.setSeconds(0);
        final Date today2 = new Date();
        today2.setHours(12); today2.setMinutes(0); today2.setSeconds(0);
        Map<String, String> eventDef = new HashMap<String, String>();
        eventDef.put("e1", "Event SET author = %veny%, customer = %JanNovak%, procedure = %beleni%, text = 'Prijdte na beleni dne #{date} v #{time}, #{doctor}', startTime = date('"
                + formatter.format(today1) + "', 'yyyy-MM-dd HH:mm:ss'), length = 30");
        eventDef.put("e2", "Event SET author = %veny%, customer = %me%, procedure = %trhani%, text = 'Message text, #{date}, #{time}, #{doctor}', startTime = '"
                + formatter.format(today2) + "', length = 60");

        Object[] all = { userDef, unitDef, membDef, patDef, procDef, eventDef };

        for (Object entity : all) {
            @SuppressWarnings("unchecked")
            final Map<String, String> entityMap = (Map<String, String>) entity;

            for (Entry<String, String> entry : entityMap.entrySet()) {
                String sql = "INSERT INTO " + entry.getValue();
                // placeholders replacement - not effective way but I don't care
                for (Entry<String, ORID> pair : rids.entrySet()) {
                    sql = sql.replaceAll("%" + pair.getKey() + "%", pair.getValue().toString());
                }

                AbstractEntity rslt = db.command(new OCommandSQL(sql.toString())).execute();
                rids.put(entry.getKey(), (ORID) rslt.getId());
            }
        }

        db.close();
    }


    // -- Enh#26 [X]
    // CREATE PROPERTY AbstractEntity.updatedAt DATETIME
    // CREATE PROPERTY AbstractEntity.updatedBy STRING

    // -- Enh#8 [X]
    // CREATE PROPERTY AbstractEntity.deletedAt DATETIME
    // CREATE PROPERTY AbstractEntity.deletedBy STRING
    // UPDATE Customer   SET deletedAt='2000-01-01 00:00:00' WHERE deleted=true
    // UPDATE Event      SET deletedAt='2000-01-01 00:00:00' WHERE deleted=true
    // UPDATE Membership SET deletedAt='2000-01-01 00:00:00' WHERE deleted=true
    // UPDATE Procedure  SET deletedAt='2000-01-01 00:00:00' WHERE deleted=true
    // UPDATE Unit       SET deletedAt='2000-01-01 00:00:00' WHERE deleted=true
    // UPDATE User       SET deletedAt='2000-01-01 00:00:00' WHERE deleted=true
    // UPDATE Customer   SET deletedBy='<X>' WHERE deleted=true
    // UPDATE Event      SET deletedBy='<X>' WHERE deleted=true
    // UPDATE Membership SET deletedBy='<X>' WHERE deleted=true
    // UPDATE Procedure  SET deletedBy='<X>' WHERE deleted=true
    // UPDATE Unit       SET deletedBy='<X>' WHERE deleted=true
    // UPDATE User       SET deletedBy='<X>' WHERE deleted=true
    // DROP PROPERTY AbstractEntity.deleted
    // UPDATE Customer   REMOVE deleted
    // UPDATE Event      REMOVE deleted
    // UPDATE Membership REMOVE deleted
    // UPDATE Procedure  REMOVE deleted
    // UPDATE Unit       REMOVE deleted
    // UPDATE User       REMOVE deleted

    // -- Enh#31: add Customer#email [X]
    // CREATE PROPERTY Customer.email STRING
    // -- Enh#31: add Unit#email [X]
    // CREATE PROPERTY Unit.email STRING
    // UPDATE Unit SET email = "evca.chladkova@email.cz" WHERE name LIKE "%Chládková%"
    // UPDATE Unit SET email = "noreply@smevente.com" WHERE name NOT LIKE "%Chládková%"
    // ALTER PROPERTY Unit.email MANDATORY true
    // ALTER PROPERTY Unit.email NOTNULL true
    // -- Enh#31: rename Unit#smsGateway -> Unit#options [X]
    // ALTER PROPERTY Unit.smsGateway NAME options
    // UPDATE Unit SET options = '{"sms":{"gateway":"sms.sluzba.cz","username":"veny","password":"XXX"}}' WHERE smsGateway LIKE '%veny%'
    // UPDATE Unit SET options = '{"sms":{"gateway":"sms.sluzba.cz","username":"chladkova","password":"XXX"}}' WHERE smsGateway LIKE '%chladkova%'
    // UPDATE Unit SET options = '{"sms":{"gateway":"sms.sluzba.cz","username":"smevente","password":"XXX"}}' WHERE smsGateway LIKE '%smevente%'
    // ALTER PROPERTY Unit.options MANDATORY true
    // ALTER PROPERTY Unit.options NOTNULL true
    // UPDATE Unit REMOVE smsGateway
    // -- Enh#31: add Customer#sendingChannel [X]
    // CREATE PROPERTY Customer.sendingChannel INTEGER
    // UPDATE Customer SET sendingChannel = 1 WHERE phoneNumber IS NOT NULL
    // UPDATE Customer SET sendingChannel = 2 WHERE email IS NOT NULL AND email.length() > 0
    // -- Enh#31: rename Unit#limitedSmss -> Unit#msgLimit [X]
    // ALTER PROPERTY Unit.limitedSmss NAME msgLimit
    // UPDATE Unit SET msgLimit = limitedSmss
    // UPDATE Unit REMOVE limitedSmss

}
