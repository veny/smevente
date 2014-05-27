package veny.smevente.dao.orientdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.Patient;
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
                Patient.class.getSimpleName(),
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
            entity.createProperty("deleted", OType.BOOLEAN);
            entity.createProperty("revision", OType.STRING);
            LOG.info("class created, name=" + entity.getName());
            // User
            OClass user = db.getMetadata().getSchema().createClass(User.class.getSimpleName(), entity);
            user.createProperty("username", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("password", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("fullname", OType.STRING).setMandatory(true).setNotNull(true);
            user.createProperty("lastLoggedIn", OType.DATETIME);
            user.createProperty("root", OType.BOOLEAN);
            LOG.info("class created, name=" + user.getName());
            // Unit
            OClass unit = db.getMetadata().getSchema().createClass(Unit.class.getSimpleName(), entity);
            unit.createProperty("name", OType.STRING).setMandatory(true).setNotNull(true);
            unit.createProperty("description", OType.STRING);
            unit.createProperty("type", OType.STRING); // null == PATIENT
            unit.createProperty("limitedSmss", OType.LONG);
            unit.createProperty("smsGateway", OType.STRING);
            LOG.info("class created, name=" + unit.getName());
            // Membership
            OClass membership = db.getMetadata().getSchema().createClass(Membership.class.getSimpleName(), entity);
            membership.createProperty("user", OType.LINK, user).setMandatory(true);
            membership.createProperty("unit", OType.LINK, unit).setMandatory(true);
            membership.createProperty("role", OType.STRING).setMandatory(true).setNotNull(true);
            membership.createProperty("significance", OType.INTEGER);
            LOG.info("class created, name=" + membership.getName());
            // Patient
            OClass patient = db.getMetadata().getSchema().createClass(Patient.class.getSimpleName(), entity);
            patient.createProperty("unit", OType.LINK, unit).setMandatory(true);
            patient.createProperty("firstname", OType.STRING);
            patient.createProperty("surname", OType.STRING).setMandatory(true).setNotNull(true);
            patient.createProperty("asciiFullname", OType.STRING).setMandatory(true).setNotNull(true);
            patient.createProperty("phoneNumber", OType.STRING);
            patient.createProperty("birthNumber", OType.STRING);
            patient.createProperty("degree", OType.STRING);
            patient.createProperty("street", OType.STRING);
            patient.createProperty("city", OType.STRING);
            patient.createProperty("zipCode", OType.STRING);
            patient.createProperty("employer", OType.STRING);
            patient.createProperty("careers", OType.STRING);
            LOG.info("class created, name=" + patient.getName());
            // Procedure
            OClass procedure = db.getMetadata().getSchema().createClass(Procedure.class.getSimpleName(), entity);
            procedure.createProperty("unit", OType.LINK, unit).setMandatory(true);
            procedure.createProperty("name", OType.STRING).setMandatory(true).setNotNull(true);
            procedure.createProperty("messageText", OType.STRING).setMandatory(true).setNotNull(true);
            procedure.createProperty("type", OType.STRING); // null == Event.Type.IN_CALENDAR
            procedure.createProperty("color", OType.STRING);
            procedure.createProperty("time", OType.INTEGER);
            LOG.info("class created, name=" + procedure.getName());
            // Event
            OClass event = db.getMetadata().getSchema().createClass(Event.class.getSimpleName(), entity);
            event.createProperty("author", OType.LINK, user).setMandatory(true);
            event.createProperty("patient", OType.LINK, patient).setMandatory(true);
            event.createProperty("procedure", OType.LINK, procedure); // not mandatory for special messages
            event.createProperty("text", OType.STRING).setMandatory(true).setNotNull(true);
            event.createProperty("notice", OType.STRING);
            event.createProperty("startTime", OType.DATETIME); // can be 'null' for special events
            event.createProperty("length", OType.INTEGER); // can be 'null' for special events
            event.createProperty("sent", OType.DATETIME);
            event.createProperty("sendAttemptCount", OType.INTEGER);
            event.createProperty("type", OType.STRING);
            LOG.info("class created, name=" + event.getName());

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
        userDef.put("veny", "User SET username = 'veny', password = 'SHA:a8fdc205a9f19cc1c7507a60c4f01b13d11d7fd0', fullname = 'Vaclav Novy', root = true"); // password: 123
        userDef.put("max",  "User SET username = 'max',  password = 'SHA:a8fdc205a9f19cc1c7507a60c4f01b13d11d7fd0', fullname = 'Max Mustermann'");
        // Units
        Map<String, String> unitDef = new HashMap<String, String>();
        unitDef.put("foo", "Unit SET name = 'Foo', description = 'Desc of Foo', type = 'PATIENT', smsGateway = 'type=sms.sluzba.cz&username=foo&password=bar'");
        unitDef.put("bar", "Unit SET name = 'Bar', description = 'Desc of Bar', type = 'CUSTOMER', smsGateway = 'type=sms.sluzba.cz&username=alfa&password=bravo'");
        // Membership
        Map<String, String> membDef = new HashMap<String, String>();
        membDef.put("m1", "Membership SET user = %veny%, unit = %foo%, role = 'ADMIN', significance = 10");
        membDef.put("m2", "Membership SET user = %veny%, unit = %bar%, role = 'MEMBER', significance = 20");
        membDef.put("m3", "Membership SET user = %max%, unit = %foo%, role = 'MEMBER', significance = 20");
        membDef.put("m4", "Membership SET user = %max%, unit = %bar%, role = 'ADMIN', significance = 40");
        // Patient
        Map<String, String> patDef = new HashMap<String, String>();
        patDef.put("JanNovak",      "Patient SET unit = %foo%, firstname = 'Jan',  surname = 'Novák',     asciiFullname = 'JAN NOVAK',      birthNumber = '7001012000', phoneNumber = '606123123'");
        patDef.put("PetrZlutoucky", "Patient SET unit = %foo%, firstname = 'Petr', surname = 'Žluťoučký', asciiFullname = 'PETR ZLUTOUCKY', birthNumber = '7002023000', phoneNumber = '606123123'");
        patDef.put("LindaModra",    "Patient SET unit = %foo%, firstname = 'Lída', surname = 'Modrá',     asciiFullname = 'LIDA MODRA',     birthNumber = '7051011000', phoneNumber = '606123123'");
        patDef.put("me",            "Patient SET unit = %bar%, firstname = 'veny', surname = 'V',         asciiFullname = 'VENY V',         birthNumber = '7004045000', phoneNumber = '606146177'");
        // Procedure
        Map<String, String> procDef = new HashMap<String, String>();
        procDef.put("beleni", "Procedure SET unit = %foo%, name = 'Bělení', messageText = 'Prijdte na beleni', type = 'IN_CALENDAR', color = 'FF0000', time = 30");
        procDef.put("p2", "Procedure SET unit = %foo%, name = 'Extrakce', messageText = 'Prijdte na trhani', type = 'IN_CALENDAR', color = '00FF00', time = 60");
        procDef.put("p3", "Procedure SET unit = %foo%, name = 'Dovolená', messageText = 'Mame dovolenou', type => 'IMMEDIATE_MESSAGE'");
        // Event
        Map<String, String> eventDef = new HashMap<String, String>();
        eventDef.put("e1", "Event SET author = %veny%, patient = %JanNovak%, procedure = %beleni%, text = 'Message text', startTime = '2012-10-30 10:10:00:000', length = 30");
        eventDef.put("e2", "Event SET author = %veny%, patient = %me%, procedure = %beleni%, text = 'Message text', startTime = '2013-06-20 15:00:00:000', length = 60");

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

}
