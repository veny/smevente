package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import veny.smevente.AbstractBaseTest;
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
     * Creates a mock of Sms Gateway.
     */
    @SuppressWarnings("unchecked")
    @Before
    public void mockSmsGateway() {
        final SmsGatewayService gatewayService = Mockito.mock(SmsGatewayService.class);
        Mockito.when(gatewayService.send(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap())).thenReturn(true);
        ReflectionTestUtils.setField(eventService, "smsGatewayService", gatewayService);
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
        assertEquals(0, ev.getSendAttemptCount());

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
    public void testBulkSendNotYes() {
        final User author = createDefaultUser();
        final Customer patient = createDefaultCustomer();
        final Procedure procedure = createProcedure(PROCEDURE_NAME, PROCEDURE_COLOR, PROCEDURE_TIME,
                PROCEDURE_MSGTEXT, null, patient.getUnit());
        createEvent(EVENT_TEXT, new Date(System.currentTimeMillis() + (5 * 24 * 3600 * 1000)),
                EVENT_LEN, EVENT_NOTICE, author, patient, procedure);
        assertEquals(0, eventService.bulkSend()); // the event is too fresh
    }

    /** EventService.bulkSend. */
    @SuppressWarnings("unchecked")
    @Test
    public void testBulkSendAttemptCount() {
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

        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(4, byId.getSendAttemptCount());

        // next attempt does not include the event, 3<= is limit
        assertEquals(0, eventService.bulkSend());
        byId = eventService.getEvent(ev.getId());
        assertNull(byId.getSent());
        assertEquals(4, byId.getSendAttemptCount());
    }

    // -------------------------------------------------------- Assistant Stuff

}
