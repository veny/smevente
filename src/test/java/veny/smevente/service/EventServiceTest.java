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
import org.springframework.test.util.ReflectionTestUtils;

import veny.smevente.AbstractBaseTest;
import veny.smevente.client.utils.Pair;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.User;
import veny.smevente.service.SmsGatewayService.SmsException;

/**
 * Test of <code>EventService</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 21.6.2014
 */
public class EventServiceTest extends AbstractBaseTest {

    /**
     * Creates a mock of SMS Gateway.
     * @throws IOException technical problem indication
     * @throws SmsException business problem indication
     */
    @SuppressWarnings("unchecked")
    @Before
    public void mockSmsGateway() throws SmsException, IOException {
        final SmsGatewayService gatewayService = Mockito.mock(SmsGatewayService.class);
        Mockito.when(gatewayService.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap())).thenReturn(true);
        ReflectionTestUtils.setField(eventService, "smsGatewayService", gatewayService);
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


    /** EventService.bulkSend. */
    @Test
    public void testBulkSendSimple() {
        // no event in DB
        assertEquals(0, eventService.bulkSend());

        // 1 event in DB
        Event ev  = createDefaultEvent();
        assertEquals(null, ev.getSent());
        assertEquals(0, ev.getSendAttemptCount());

        assertEquals(1, eventService.bulkSend());
        Event sent = eventService.getEvent(ev.getId());
        assertNotNull(sent.getSent());
        assertTrue(sent.getSent().before(new Date()));
        assertEquals(1, ev.getSendAttemptCount());

        // next attempt to send the same event
        assertEquals(0, eventService.bulkSend());

        // next 2 events
        createEvent("text", new Date(System.currentTimeMillis() - (5 * 24 * 3600 * 1000)), 10, null,
                sent.getAuthor(), sent.getCustomer(), sent.getProcedure());
        createEvent("text", new Date(System.currentTimeMillis() - (5 * 24 * 3600 * 1000)), 10, null,
                sent.getAuthor(), sent.getCustomer(), sent.getProcedure());
        assertEquals(2, eventService.bulkSend());
        assertEquals(0, eventService.bulkSend());
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
     * @throws IOException technical problem indication
     * @throws SmsException business problem indication
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testBulkSendAttemptCount() throws SmsException, IOException {
        final SmsGatewayService gatewayService = Mockito.mock(SmsGatewayService.class);
        Mockito.when(gatewayService.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
                .thenThrow(SmsException.class);
        ReflectionTestUtils.setField(eventService, "smsGatewayService", gatewayService);

        final User author = createDefaultUser();
        final Customer patient = createDefaultCustomer();
        final Procedure procedure = createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME,
                PROCEDURE_MSGTEXT, null, patient.getUnit());
        Event ev = createEvent("exception", new Date(), EVENT_LEN, EVENT_NOTICE, author, patient, procedure);

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
    }

    // -------------------------------------------------------- Assistant Stuff

}
