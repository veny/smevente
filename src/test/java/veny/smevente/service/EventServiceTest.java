package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;

import veny.smevente.AbstractBaseTest;
import veny.smevente.client.utils.Pair;
import veny.smevente.dao.CustomerDao;
import veny.smevente.dao.UnitDao;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.service.SmsGatewayService.SmsException;

/**
 * Test of <code>EventService</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 21.6.2014
 */
public class EventServiceTest extends AbstractBaseTest {

    // CHECKSTYLE:OFF
    @Autowired
    protected UnitDao unitDao;
    @Autowired
    protected CustomerDao customerDao;
    // CHECKSTYLE:ON

    /** Mocked object simulating communication with SMS gateway. */
    private SmsGatewayService gatewayService;
    /** Mocked object simulating communication with mail server. */
    private MailSender mailSender;

    /**
     * Creates a mock of SMS Gateway.
     * @throws IOException technical problem indication
     * @throws SmsException business problem indication
     */
    @SuppressWarnings("unchecked")
    @Before
    public void mockSmsGateway() throws SmsException, IOException {
        gatewayService = Mockito.mock(SmsGatewayService.class);
        Mockito.when(gatewayService.send(
                Mockito.eq("badNumber"), Mockito.anyString(), Mockito.anyMap())).thenThrow(IOException.class);
        ReflectionTestUtils.setField(eventService, "smsGatewayService", gatewayService);

        mailSender = Mockito.mock(MailSender.class);
        Mockito.doNothing().when(mailSender).send(Mockito.any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(eventService, "mailSender", mailSender);
    }


    /** EventService.findEventsByCustomer. */
    @Test
    public void testFindEventsByCustomer() {
        final Event first  = createDefaultEvent();

        final Customer c = first.getCustomer();
        assertEquals(1, eventService.findEventsByCustomer(c.getId()).getB().size());

        // create second event
        Date start = new Date();
        createEvent("SECOND", start, EVENT_LEN, EVENT_NOTICE, first.getAuthor(), c, first.getProcedure());
        Pair<Customer, List<Event>> events = eventService.findEventsByCustomer(c.getId());
        assertEquals(c.getId(), events.getA().getId());
        assertEquals(2, events.getB().size());
        // ORDER BY startTime DESC
        assertEquals(EVENT_TEXT, events.getB().get(1).getText());
        assertEquals("SECOND", events.getB().get(0).getText());

        // create third event
        start = new Date();
        final Event third = createEvent(
                "THIRD", start, EVENT_LEN, EVENT_NOTICE, first.getAuthor(), c, first.getProcedure());
        events = eventService.findEventsByCustomer(c.getId());
        assertEquals(c.getId(), events.getA().getId());
        assertEquals(3, events.getB().size());
        // ORDER BY startTime DESC
        assertEquals("THIRD", events.getB().get(0).getText());

        // delete event
        eventService.deleteEvent(third.getId());
        events = eventService.findEventsByCustomer(c.getId());
        assertEquals(2, events.getB().size());

        // create special event
        final Event event = new Event();
        event.setAuthor(first.getAuthor());
        event.setCustomer(first.getCustomer());
        event.setProcedure(first.getProcedure());
        event.setText("SPECIAL");
        event.setType(Event.Type.IMMEDIATE_MESSAGE.toString());
        eventService.storeEvent(event);
        // --
        events = eventService.findEventsByCustomer(c.getId());
        assertEquals(2, events.getB().size());

        // event for other customer
        Customer secC = createCustomer("aa", "bb", "123123123", null, c.getUnit());
        createEvent(EVENT_TEXT, EVENT_START, EVENT_LEN, EVENT_NOTICE, first.getAuthor(), secC, first.getProcedure());
        events = eventService.findEventsByCustomer(secC.getId());
        assertEquals(1, events.getB().size());
        events = eventService.findEventsByCustomer(c.getId());
        assertEquals(2, events.getB().size());
    }


    /** EventService.send.
     * @throws IOException technical problem
     * @throws SmsException */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendSmsSimple() throws SmsException, IOException {
        final Event ev = createEventToBeSentAs(Event.CHANNEL_SMS);
        final Event sent = eventService.send(ev);

        assertEquals(ev.getId(), sent.getId());
        assertNotNull(sent.getSent());
        assertTrue(sent.getSent().before(new Date()));
        assertEquals(1, ev.getSendAttemptCount());
        // default unit created with limit of 11
        assertEquals(10L, ev.getCustomer().getUnit().getMsgLimit().longValue());
        Mockito.verify(gatewayService, Mockito.times(1)).send(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        Mockito.verify(mailSender, Mockito.times(0)).send(Mockito.any(SimpleMailMessage.class));
    }

    /** EventService.send.
     * @throws IOException technical problem
     * @throws SmsException */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendMailSimple() throws SmsException, IOException {
        final Event ev = createEventToBeSentAs(Event.CHANNEL_EMAIL);
        final Event sent = eventService.send(ev);

        assertEquals(ev.getId(), sent.getId());
        assertNotNull(sent.getSent());
        assertTrue(sent.getSent().before(new Date()));
        assertEquals(1, ev.getSendAttemptCount());
        // default unit created with limit of 11
        assertEquals(10L, ev.getCustomer().getUnit().getMsgLimit().longValue());
        Mockito.verify(gatewayService, Mockito.times(0)).send(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
    }

    /** EventService.send. */
    public void testSendSmsWrongUnitOptions() {
        final Event ev = createEventToBeSentAs(Event.CHANNEL_SMS);
        final Unit unit = ev.getCustomer().getUnit();
        unitDao.detach(unit);
        unit.setOptions("{\"sms\":{\"gateway\":\"sms.sluzba.cz\"}}");
        unitDao.persist(unit);

        final Event sent = eventService.send(ev);
        assertNull(sent.getSent());
        assertEquals(1, sent.getSendAttemptCount());
    }

    /** EventService.send. */
    @Test
    public void testSendSmsBadPhoneNumber() {
        Event ev  = createDefaultEvent();
        Customer c = ev.getCustomer();
        customerDao.detach(c);
        c.setPhoneNumber("badNumber");
        c.setSendingChannel(Event.CHANNEL_SMS);
        customerDao.persist(c);
        ev = eventService.send(ev);
        assertNull(ev.getSent());
        assertEquals(1, ev.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(11L, unitService.getUnit(c.getUnit().getId()).getMsgLimit().longValue());
        // and again
        ev = eventService.send(ev);
        assertNull(ev.getSent());
        assertEquals(2, ev.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(11L, unitService.getUnit(c.getUnit().getId()).getMsgLimit().longValue());
    }

    /** EventService.send. */
    @Test
    public void testSendEmailWrongAddress() {
        Event ev  = createDefaultEvent();
        Customer c = ev.getCustomer();
        customerDao.detach(c);
        c.setEmail(" ");
        c.setSendingChannel(Event.CHANNEL_EMAIL);
        customerDao.persist(c);
        ev = eventService.send(ev);
        assertNull(ev.getSent());
        assertEquals(1, ev.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(11L, unitService.getUnit(c.getUnit().getId()).getMsgLimit().longValue());
        // and again
        ev = eventService.send(ev);
        assertNull(ev.getSent());
        assertEquals(2, ev.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(11L, unitService.getUnit(c.getUnit().getId()).getMsgLimit().longValue());
    }

    /** EventService.send. */
    @Test(expected = IllegalStateException.class)
    public void testSendSmsExceedSmsLimit() {
        Event ev  = createDefaultEvent();
        Unit unit = ev.getCustomer().getUnit();
        unitDao.detach(unit);
        unit.setMsgLimit(0L);
        unitDao.persist(unit);
        eventService.send(ev);
    }

    /** EventService.createAndSendSpecialEvent.
     * @throws Exception if something wrong */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateAndSendSpecialEvent() throws Exception {
        // there is just test about storing, the rest is covered by test of 'sendSms'
        // which is internal used
        final User author = createDefaultUser();
        final Customer customer = createDefaultCustomer();
        final Procedure procedure = createProcedure(
                PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME, PROCEDURE_MSGTEXT,
                Event.Type.IMMEDIATE_MESSAGE, customer.getUnit());

        final Event event = new Event();
        event.setAuthorId(author.getId());
        event.setCustomerId(customer.getId());
        event.setProcedureId(procedure.getId());
        event.setText("some text");
        event.setStartTime(new Date());
        event.setLength(30);
        event.setNotice(null);
        Event sent = eventService.createAndSendSpecialEvent(event);
        assertNotNull(sent);
        assertNotNull(sent.getId());
        assertEquals(sent.getAuthor().getId(), event.getAuthor().getId());
        assertEquals(sent.getCustomer().getId(), event.getCustomer().getId());
        assertEquals(sent.getProcedure().getId(), event.getProcedure().getId());
        assertNotNull(sent.getSent());
        assertEquals(1, sent.getSendAttemptCount());
        assertTrue(sent.getSent().before(new Date()));
        assertEquals(Event.Type.IMMEDIATE_MESSAGE.toString(), sent.getType());
        Mockito.verify(gatewayService, Mockito.times(1)).send(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
    }

    /** EventService.bulkSend. */
    @Test
    public void testBulkSendSmsSimple() {
        // no event in DB
        assertEquals(0, eventService.bulkSend());

        // 1 event in DB
        Event ev  = createEventToBeSentAs(Event.CHANNEL_SMS);
        assertEquals(null, ev.getSent());
        assertEquals(0, ev.getSendAttemptCount());

        assertEquals(1, eventService.bulkSend());
        Event sent = eventService.getEvent(ev.getId());
        assertNotNull(sent.getSent());
        assertTrue(sent.getSent().before(new Date()));
        assertEquals(1, ev.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(10L, unitService.getUnit(ev.getCustomer().getUnit().getId()).getMsgLimit().longValue());

        // next attempt to send the same event
        assertEquals(0, eventService.bulkSend());
        // no decrease of limit on unit
        assertEquals(10L, unitService.getUnit(ev.getCustomer().getUnit().getId()).getMsgLimit().longValue());

        // next 2 events
        createEvent("A", new Date(System.currentTimeMillis() - (5 * 24 * 3600 * 1000)), 10, null,
                sent.getAuthor(), sent.getCustomer(), sent.getProcedure());
        createEvent("B", new Date(System.currentTimeMillis() - (5 * 24 * 3600 * 1000)), 10, null,
                sent.getAuthor(), sent.getCustomer(), sent.getProcedure());
        assertEquals(2, eventService.bulkSend());
        assertEquals(0, eventService.bulkSend());
        // no decrease of limit on unit
        assertEquals(8L, unitService.getUnit(ev.getCustomer().getUnit().getId()).getMsgLimit().longValue());
    }

    /** EventService.bulkSend. */
    @Test
    public void testBulkSendDeleted() {
        Event ev  = createDefaultEvent();
        eventService.deleteEvent(ev.getId());
        assertEquals(0, eventService.bulkSend());
    }

    /** EventService.bulkSend. */
    @Test
    public void testBulkSendNotYet() {
        final User author = createDefaultUser();
        final Customer patient = createDefaultCustomer();
        final Procedure procedure = createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME,
                PROCEDURE_MSGTEXT, null, patient.getUnit());
        final Event ev = createEvent(EVENT_TEXT, new Date(System.currentTimeMillis() + (5 * 24 * 3600 * 1000)),
                EVENT_LEN, EVENT_NOTICE, author, patient, procedure);
        assertEquals(0, eventService.bulkSend()); // the event is too fresh
        assertNull(eventService.getEvent(ev.getId()).getSent());
    }

    /**
     * EventService.bulkSend.
     */
    @Test
    public void testBulkSendAttemptCountByWrongPhoneNumber() {
        Event ev  = createDefaultEvent();
        Customer c = ev.getCustomer();
        customerDao.detach(c);
        c.setPhoneNumber("badNumber");
        c.setSendingChannel(Event.CHANNEL_SMS);
        customerDao.persist(c);

        assertEquals(0, eventService.bulkSend());
        Event byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(1, byId.getSendAttemptCount());

        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(2, byId.getSendAttemptCount());

        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(3, byId.getSendAttemptCount());

        // next attempt does not include the event, 3<= is limit
        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(3, byId.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(11L, unitService.getUnit(ev.getCustomer().getUnit().getId()).getMsgLimit().longValue());
    }

    /**
     * EventService.bulkSend.
     */
    @Test
    public void testBulkSendAttemptCountByWrongEmailAddress() {
        Event ev  = createDefaultEvent();
        Customer c = ev.getCustomer();
        customerDao.detach(c);
        c.setEmail("");
        c.setSendingChannel(Event.CHANNEL_EMAIL);
        customerDao.persist(c);

        assertEquals(0, eventService.bulkSend());
        Event byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(1, byId.getSendAttemptCount());

        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(2, byId.getSendAttemptCount());

        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(3, byId.getSendAttemptCount());

        // next attempt does not include the event, 3<= is limit
        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(3, byId.getSendAttemptCount());
        // no decrease of limit on unit
        assertEquals(11L, unitService.getUnit(ev.getCustomer().getUnit().getId()).getMsgLimit().longValue());
    }

    // -------------------------------------------------------- Assistant Stuff

    /** Creates event to be sent wia given channel.
     *
     * @param channel channel
     * @return the event
     */
    private Event createEventToBeSentAs(final int channel) {
        final Event ev  = createDefaultEvent();
        final Customer c = ev.getCustomer();
        customerDao.detach(c);
        c.setSendingChannel(channel);
        customerDao.persist(c);
        return ev;
    }

}
