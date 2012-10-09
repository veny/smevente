package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import veny.smevente.AbstractBaseTest;
import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Procedure;
import veny.smevente.model.Patient;
import veny.smevente.model.Event;
import veny.smevente.model.Unit;
import veny.smevente.model.User;

/**
 * Test of <code>SmsService</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 23.11.2010
 */
public class SmsServiceTest extends AbstractBaseTest {

    /** Do NOT send the SMSs. */
    @Before
    public void disableSendingSms() {
        System.setProperty("sms.gateway.fake", "true");
    }


    /** SmsService.createSms. */
    @SuppressWarnings("deprecation")
    @Test
    public void testCreateSms() {
        final Event created = createDefaultEvent();
        assertDefaultEvent(created, true);

        final List<Event> found = eventService.getAllSmss();
        assertEquals(1, found.size());
        assertDefaultEvent(found.get(0), true);

        final Date now = new Date();
        createEvent("a", now, 1, "a", created.getAuthor(), created.getPatient(), created.getProcedure());
        createEvent("b", new Date(now.getTime() + 1000), 2, "b",
                created.getAuthor(), created.getPatient(), created.getProcedure());
        assertEquals(3, eventService.getAllSmss().size());

        // Validation

        try { // start time is null
            createEvent("a", null, 1, "a", created.getAuthor(), created.getPatient(), created.getProcedure());
            assertEquals("expected NullPointerException", true, false);
        } catch (NullPointerException e) { assertEquals(true, true); }
        try { // length <= 0
            createEvent("a", now, 0, "a", created.getAuthor(), created.getPatient(), created.getProcedure());
            assertEquals("expected IllegalArgumentException", true, false);
        } catch (IllegalArgumentException e) { assertEquals(true, true); }
        try { // not the same unit by patient & unit
            final Unit unit = createUnit("zx", new HashMap<String, String>(), 0L);
            final Patient patient = createPatient("A", "B", null, null, unit);
            createEvent("a", now, 0, "a", created.getAuthor(), patient, created.getProcedure());
            assertEquals("expected IllegalArgumentException", true, false);
        } catch (IllegalArgumentException e) { assertEquals(true, true); }
    }

    /** SmsService.createAndSendSpecialSms. */
    @Test
    public void testCreateAndSendSpecialSms() {
        final User author = createDefaultUser();
        final Patient patient = createDefaultPatient();
        final Event sms = new Event();
        sms.setAuthor(author);
        sms.setPatient(patient);
        sms.setText("text");

        final Event created = eventService.createAndSendSpecialSms(sms);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertDefaultUser(created.getAuthor());
        assertDefaultPatient(created.getPatient(), false);
        assertNull(created.getProcedure());
        assertNotNull(created.getSent());
        assertTrue((created.getStatus().intValue() & Event.STATUS_SPECIAL) > 0);

        // Validation

        sms.setAuthor(null);
        try { // no author
            eventService.createAndSendSpecialSms(sms);
            assertEquals("expected NullPointerException", true, false);
        } catch (NullPointerException e) { assertEquals(true, true); }
        sms.setAuthor(created.getAuthor());
        sms.setPatient(null);
        try { // no patient
            eventService.createAndSendSpecialSms(sms);
            assertEquals("expected NullPointerException", true, false);
        } catch (NullPointerException e) { assertEquals(true, true); }
        sms.setPatient(created.getPatient());
        sms.setText("  ");
        try { // blank text
            eventService.createAndSendSpecialSms(sms);
            assertEquals("expected IllegalArgumentException", true, false);
        } catch (IllegalArgumentException e) { assertEquals(true, true); }
    }
    /** SmsService.createAndSendSpecialSms by Limited Unit. */
    @Test
    public void testCreateAndSendSpecialSmsByLimitedUnit() {
        final User author = createDefaultUser();
        final Unit limitedUnit = createUnit("limited", getDefaultUnitMetadata(), 1L);
        final Patient patient = createPatient("a", "b", "606146177", null, limitedUnit);
        assertEquals(1L, limitedUnit.getLimitedSmss().longValue());

        final Event sms = new Event();
        sms.setAuthor(author);
        sms.setPatient(patient);
        sms.setText("text");

        // first time - OK
        eventService.createAndSendSpecialSms(sms);
        final Unit decreasedUnit = unitService.getById(limitedUnit.getId());
        assertEquals(limitedUnit.getLimitedSmss().longValue() - 1L, decreasedUnit.getLimitedSmss().longValue());

        // second time - limit exceeded
        try {
            eventService.createAndSendSpecialSms(sms);
            assertEquals("expected IllegalStateException", true, false);
        } catch (IllegalStateException e) {
            assertEquals(SmsUtils.SMS_LIMIT_EXCEEDE, e.getMessage());
        }
    }

    /** SmsService.updateSms. */
    @Test
    public void testUpdateSms() {
        final Event created = createDefaultEvent();
        final Procedure mhc =
            createMedicalHelpCategory("zzz", "000000", 1, "sms text", createUnit("iii", getDefaultUnitMetadata(), 0L));
        created.setText("Esemeskus");
        created.setNotice("zxc123asdf");
        created.setProcedure(mhc);
        final Event updated = eventService.storeEvent(created);
        assertEquals(created.getId(), updated.getId());
        assertEquals(created.getAuthor().getId(), updated.getAuthor().getId());
        assertEquals(created.getPatient().getId(), updated.getPatient().getId());
        assertEquals(mhc.getId(), updated.getProcedure().getId());
        assertEquals("Esemeskus", updated.getText());
        assertEquals("zxc123asdf", updated.getNotice());
    }

    /** SmsService.deleteSms. */
    @SuppressWarnings("deprecation")
    @Test
    public void testDeleteSms() {
        Event sms = createDefaultEvent();
        List<Event> found = eventService.findSms(sms.getAuthor().getId(), new Date(0), new Date());
        assertEquals(1, found.size());
        assertEquals(0, found.get(0).getStatus().intValue());

        // first delete hard - SMS not sent
        eventService.deleteSms(sms.getId());
        assertEquals(0, eventService.findSms(sms.getAuthor().getId(), new Date(0), new Date()).size());

        // create a new SMS and 'Sent' attribute
        sms = createEvent(EVENT_TEXT, EVENT_START, EVENT_LEN, EVENT_NOTICE,
                sms.getAuthor(), sms.getPatient(), sms.getMedicalHelpCategory());
        sms.setSent(new Date());
        sms = eventService.storeEvent(sms);

        // first delete soft - SMS already sent
        eventService.deleteSms(sms.getId());
        found = eventService.getAllSmss();
        assertEquals(1, found.size());
        assertEquals(sms.getId(), found.get(0).getId());
        assertTrue((found.get(0).getStatus() & Event.STATUS_DELETED) > 0);
    }

    /** SmsService.getSmsText. */
    @Test
    public void testGetSmsText() {
        final Event sms = createDefaultEvent();
        assertEquals(EVENT_TEXT, eventService.getSmsText(sms.getId()));

        sms.setText("replacement #{doctor}");
        eventService.updateSms(sms);
        assertEquals("replacement " + sms.getAuthor().getFullname(), eventService.getSmsText(sms.getId()));
        sms.setText("replacement #{date}");
        eventService.updateSms(sms);
        assertEquals("replacement 24.11.10", eventService.getSmsText(sms.getId()));
        sms.setText("replacement #{time}");
        eventService.updateSms(sms);
        assertEquals("replacement 0:00", eventService.getSmsText(sms.getId()));

        // convert2ascii
        sms.setText("Žluťoučký kůň pěl ďábelské ódy");
        eventService.updateSms(sms);
        assertEquals("Zlutoucky kun pel dabelske ody", eventService.getSmsText(sms.getId()));
    }

    /** SmsService.getById. */
    @Test
    public void testGetById() {
        final Event created = createDefaultEvent();
        final Event found = eventService.getById(created.getId());
        assertDefaultEvent(found, true);

        try {
            eventService.getById(1000L);
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
    }

    /** SmsService.findSms. */
    @SuppressWarnings("deprecation")
    @Test
    public void testFindSms() {
        final Date from = new Date(0, 0, 1); // 1.1.1900
        final Date to = new Date(); // now
        final Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("A", "B");
        final Unit unitA = createUnit("A", metadata, 0L);
        final Unit unitB = createUnit("B", metadata, 0L);
        final User authorA = createUser("A", "A", "AA", false);
        final User authorB = createUser("B", "B", "B", false);
        final Patient patientA = createPatient("A", "A", null, null, unitA);
        final Patient patientB = createPatient("B", "B", null, null, unitB);
        final Procedure mhcA = createProcedure("A", "AAAAAA", 10, "text", unitA);
        final Procedure mhcB = createProcedure("B", "BBBBBB", 10, "text", unitB);

        assertEquals(0, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(0, eventService.findSms(authorB.getId(), from, to).size());

        // all SMSs of author A - year 2000
        createEvent("text", new Date(100, 0, 1, 12, 0), 10, "notice", authorA, patientA, mhcA);
        assertEquals(1, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(0, eventService.findSms(authorB.getId(), from, to).size());

        createEvent("text", new Date(100, 1, 1, 12, 0), 10, "notice", authorA, patientA, mhcA);
        assertEquals(2, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(0, eventService.findSms(authorB.getId(), from, to).size());

        // other unit
        createEvent("text", new Date(100, 2, 1, 12, 0), 10, "notice", authorA, patientB, mhcB);
        assertEquals(3, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(0, eventService.findSms(authorB.getId(), from, to).size());

        // all SMSs of author B - year 2010
        createEvent("text", new Date(110, 0, 1, 12, 0), 10, "notice", authorB, patientB, mhcB);
        assertEquals(3, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(1, eventService.findSms(authorB.getId(), from, to).size());

        // other unit
        createEvent("text", new Date(110, 1, 1, 12, 0), 10, "notice", authorB, patientA, mhcA);
        assertEquals(3, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(2, eventService.findSms(authorB.getId(), from, to).size());

        // deleted SMS
        final Event toDel = createEvent("text", new Date(110, 2, 1, 12, 0), 10, "notice", authorB, patientB, mhcB);
        assertEquals(3, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(3, eventService.findSms(authorB.getId(), from, to).size());
        toDel.setStatus(0 | Event.STATUS_DELETED);
        eventService.updateSms(toDel);
        assertEquals(3, eventService.findSms(authorA.getId(), from, to).size());
        assertEquals(2, eventService.findSms(authorB.getId(), from, to).size());

        // time frames (TO)
        assertEquals(0, eventService.findSms(authorA.getId(), from, new Date(99, 11, 31)).size());
        assertEquals(1, eventService.findSms(authorA.getId(), from, new Date(100, 0, 1, 23, 59)).size());
        assertEquals(1, eventService.findSms(authorA.getId(), from, new Date(100, 0, 31)).size());
        assertEquals(2, eventService.findSms(authorA.getId(), from, new Date(100, 1, 1, 23, 59)).size());
        assertEquals(2, eventService.findSms(authorA.getId(), from, new Date(100, 1, 30)).size());
        assertEquals(3, eventService.findSms(authorA.getId(), from, new Date(100, 2, 1, 23, 59)).size());
        // time frames (FROM)
        assertEquals(3, eventService.findSms(authorA.getId(), new Date(99, 11, 31), to).size());
        assertEquals(2, eventService.findSms(authorA.getId(), new Date(100, 0, 1, 23, 59), to).size());
        assertEquals(2, eventService.findSms(authorA.getId(), new Date(100, 0, 31), to).size());
        assertEquals(1, eventService.findSms(authorA.getId(), new Date(100, 1, 1, 23, 59), to).size());
        assertEquals(1, eventService.findSms(authorA.getId(), new Date(100, 1, 30), to).size());
        assertEquals(0, eventService.findSms(authorA.getId(), new Date(100, 2, 1, 23, 59), to).size());
    }

    /** SmsService.findSmsByPatient. */
    @Test
    public void testFindSmsByPatient() {
        final Event first = createDefaultEvent();
        Pair<Patient, List<Event>> pair = eventService.findSmsByPatient(first.getPatient().getId());
        assertNotNull(pair.getA());
        assertEquals(1, pair.getB().size());
        assertNull(pair.getB().get(0).getPatient());
        assertNotNull(pair.getB().get(0).getAuthor());
        assertEquals(first.getAuthor().getId(), pair.getB().get(0).getAuthor().getId());
        assertNotNull(pair.getB().get(0).getProcedure());
        assertEquals(first.getProcedure().getId(), pair.getB().get(0).getProcedure().getId());
        // second, first (ordered by Start Time)
        final Event second = createEvent(
                "a", new Date(first.getStartTime().getTime() + 2000L), 10,
                "a", first.getAuthor(), first.getPatient(), first.getProcedure());
        pair = eventService.findSmsByPatient(first.getPatient().getId());
        List<Event> found = pair.getB();
        assertEquals(2, found.size());
        // ordered by Start Time
        assertEquals(second.getId(), found.get(0).getId());
        assertEquals(first.getId(), found.get(1).getId());
        // second, third, first (ordered by Start Time)
        final Event third = createEvent(
                "b", new Date(first.getStartTime().getTime() + 1000L), 20,
                "b", first.getAuthor(), first.getPatient(), first.getProcedure());
        pair = eventService.findSmsByPatient(first.getPatient().getId());
        found = pair.getB();
        assertEquals(3, found.size());
        // ordered by Start Time
        assertEquals(second.getId(), found.get(0).getId());
        assertEquals(third.getId(), found.get(1).getId());
        assertEquals(first.getId(), found.get(2).getId());

        // filter deleted SMSs
        second.setSent(new Date());
        eventService.updateSms(second); // set 'sent date' to be deleted soft
        eventService.deleteSms(second.getId());
        pair = eventService.findSmsByPatient(first.getPatient().getId());
        found = pair.getB();
        assertEquals(2, found.size());

        // filter special SMSs
        final Event sms = new Event();
        sms.setAuthor(first.getAuthor());
        sms.setPatient(first.getPatient());
        sms.setText("text");
        eventService.createAndSendSpecialSms(sms);
        pair = eventService.findSmsByPatient(first.getPatient().getId());
        found = pair.getB();
        assertEquals(2, found.size());
    }

    /** SmsService.sendSms. */
    @Test
    public void testSendSms() {
        final User author = createDefaultUser();
        final Patient patient = createDefaultPatient();
        final Procedure proc =
            createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT, patient.getUnit());

        final Event sms = new Event();
        sms.setAuthor(author);
        sms.setPatient(patient);
        sms.setProcedure(proc);
        sms.setText("text");
        sms.setStartTime(new Date());
        sms.setLength(10);

        final Event created = eventService.createEvent(sms);
        eventService.sendSms(created.getId());
        final Event found = eventService.getById(created.getId());
        assertTrue(null != found.getSent());
    }
    /** SmsService.sendSms by Limited Unit. */
    @Test
    public void testSendSmsByLimitedUnit() {
        final User author = createDefaultUser();
        final Unit limitedUnit = createUnit("limited", getDefaultUnitMetadata(), 1L);
        final Patient patient = createPatient("a", "b", "606146177", null, limitedUnit);
        final Procedure mhc =
            createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT, limitedUnit);

        final Event sms = new Event();
        sms.setAuthor(author);
        sms.setPatient(patient);
        sms.setProcedure(mhc);
        sms.setText("text");
        sms.setStartTime(new Date());
        sms.setLength(10);
        final Event created = eventService.createSms(sms);

        // first time - OK
        eventService.sendSms(created.getId());
        final Unit decreasedUnit = unitService.getById(limitedUnit.getId());
        assertEquals(limitedUnit.getLimitedSmss().longValue() - 1L, decreasedUnit.getLimitedSmss().longValue());

        // second time - limit exceeded
        try {
            eventService.sendSms(created.getId());
            assertEquals("expected IllegalStateException", true, false);
        } catch (IllegalStateException e) {
            assertEquals(SmsUtils.SMS_LIMIT_EXCEEDE, e.getMessage());
        }
    }

    /** SmsService.sendSms.
     * @throws Exception because of reflection */
    @Test
    public void testBF29() throws Exception {
        final Patient patient = createDefaultPatient();
        final Procedure mhc = createProcedure(
                PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT, patient.getUnit());
        @SuppressWarnings("deprecation")
        final Date date = new Date(111, 0, 1, 12, 30); // 1.1.2011 12:30
        final Event created = createSms("#{date} #{time}", date, EVENT_LEN, EVENT_NOTICE,
                createDefaultUser(), patient, mhc);

        // test formating
        final Method m = eventService.getClass().getDeclaredMethod("format", Event.class);
        m.setAccessible(true);
        final String smsText = (String) m.invoke(eventService, created);
        assertEquals("01.01.11 12:30", smsText);
    }

    /** SmsService.bulkSend. */
    @Test
    public void testBulkSend() {
        final Event created = createDefaultEvent();
        assertEquals(1, eventService.bulkSend());
        assertEquals(0, eventService.bulkSend());

        createSms(EVENT_TEXT, new Date(), EVENT_LEN, EVENT_NOTICE,
                created.getAuthor(), created.getPatient(), created.getMedicalHelpCategory());
        assertEquals(1, eventService.bulkSend());
        assertEquals(0, eventService.bulkSend());
    }
    /** SmsService.testBulkSend by Limited Unit. */
    @Test
    public void testBulkSendByLimitedUnit() {
        final User author = createDefaultUser();
        final Unit limitedUnit = createUnit("limited", getDefaultUnitMetadata(), 1L);
        final Patient patient = createPatient("a", "b", "606146177", null, limitedUnit);
        final Procedure mhc =
            createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT, limitedUnit);

        final Event firstSms = new Event();
        firstSms.setAuthor(author);
        firstSms.setPatient(patient);
        firstSms.setProcedure(mhc);
        firstSms.setText("text");
        firstSms.setStartTime(new Date());
        firstSms.setLength(10);
        final Event firstCreated = eventService.createSms(firstSms);

        final Event secondSms = new Event();
        secondSms.setAuthor(author);
        secondSms.setPatient(patient);
        secondSms.setProcedure(mhc);
        secondSms.setText("text");
        secondSms.setStartTime(new Date());
        secondSms.setLength(10);
        final Event secondCreated = eventService.createSms(secondSms);

        // bulk send
        eventService.bulkSend();
        assertEquals(0L, unitService.getById(limitedUnit.getId()).getLimitedSmss().longValue());
        assertNotNull("first SMS has to be sent", smsService.getById(firstCreated.getId()).getSent());
        assertEquals("second SMS has failed", 1, smsService.getById(secondCreated.getId()).getSendAttemptCount());
    }

}
