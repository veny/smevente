package veny.smevente.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.dao.CustomerDao;
import veny.smevente.dao.EventDao;
import veny.smevente.dao.GenericDao;
import veny.smevente.dao.ProcedureDao;
import veny.smevente.dao.UnitDao;
import veny.smevente.dao.UserDao;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.service.EventService;
import veny.smevente.service.SmsGatewayService;
import veny.smevente.service.TextUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;


/**
 * Implementation of Event service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
public class EventServiceImpl implements EventService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(EventServiceImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UserDao userDao;
    /** Dependency. */
    @Autowired
    private UnitDao unitDao;
    /** Dependency. */
    @Autowired
    private CustomerDao customerDao;
    /** Dependency. */
    @Autowired
    private ProcedureDao procedureDao;
    /** Dependency. */
    @Autowired
    private EventDao eventDao;

    /** Dependency. */
    @Autowired
    private SmsGatewayService smsGatewayService;

    /** Dependency. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Dependency. */
    @Autowired
    private MailSender mailSender;

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Event storeEvent(final Event event) {
        // TODO [veny,B] here must be a strong authorization

        // part of validation (the aggregated entities have to exist and cannot be deleted)

        // author
        if (null == event.getAuthor() || null == event.getAuthor().getId()) {
            throw new NullPointerException("unknown author");
        }
        GenericDao.OPTIONS_HOLDER.get().put("detach", "false");
        final User author = userDao.getById(event.getAuthor().getId());
        event.setAuthor(author);
        // customer
        if (null == event.getCustomer() || null == event.getCustomer().getId()) {
            throw new NullPointerException("unknown customer");
        }
        GenericDao.OPTIONS_HOLDER.get().put("detach", "false");
        final Customer customer = customerDao.getById(event.getCustomer().getId());
        event.setCustomer(customer);
        // procedure
        if ((null == event.getProcedure() || null == event.getProcedure().getId())) {
            throw new NullPointerException("unknown procedure");
        }
        GenericDao.OPTIONS_HOLDER.get().put("detach", "false");
        final Procedure proc = procedureDao.getById(event.getProcedure().getId());
        event.setProcedure(proc);

        final Event toBeStored;
        if (null != event.getId()) {
            GenericDao.OPTIONS_HOLDER.get().put("detach", "false");
            toBeStored = eventDao.getById(event.getId());
            event.copyForUpdate(toBeStored);
        } else {
            toBeStored = event;
        }

        validateEvent(toBeStored);

        final Event rslt = eventDao.persist(toBeStored);
        LOG.info((null == toBeStored.getId() ? "created new event, " : "event updated, ") + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Event createAndSendSpecialEvent(final Event event) {
        LOG.debug("going to send special event...");
        event.setType(Event.Type.IMMEDIATE_MESSAGE.toString());
        final Event rslt = storeEvent(event);

        // next lines are a work-around, because unit has to be updated with limited count of messages
        // if the unit is limited,
        // if the unit is not loaded -> exception because of Unit#options cannot be 'null'
        final Customer realCust = customerDao.getById(event.getCustomer().getId());
        unitDao.detach(realCust.getUnit());
        rslt.setCustomer(realCust);

        return send(rslt);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteEvent(final Object eventId) {
        eventDao.remove(eventId);
        LOG.info("deleted event, id=" + eventId);
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public String getEventText(final Object eventId) {
        final Event event = eventDao.getById(eventId);
        final String text2send = format(event);
        if (LOG.isDebugEnabled()) {
            LOG.debug("constructed event text, eventId=" + eventId + ", text=" + text2send);
        }
        return text2send;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public Event getEvent(final Object id) {
        final Event rslt = eventDao.getById(id);
        LOG.debug("found event by id=" + id);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public List<Event> findEvents(final Object authorId, final Date from, final Date to) {
        final List<Event> rslt = eventDao.findByAuthorAndPeriod(authorId, from, to, false);
        LOG.info("found events, authorId=" + authorId + ", from=" + from + ", to=" + to + ", size=" + rslt.size());
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public Pair<Customer, List<Event>> findEventsByCustomer(final Object customerId) {
        final Customer client = customerDao.getById(customerId);
        final List<Event> events = eventDao.findByCustomer(customerId);
        LOG.info("found events by customer, customerId=" + customerId + ", size=" + events.size());
        return new Pair<Customer, List<Event>>(client, events);
    }

    /** {@inheritDoc} */
    @Override
    public Event send(final Event event2send) {
        return send(event2send, true);
    }

    /** {@inheritDoc} */
    @Override
    public int bulkSend() {
        // TODO [veny,B] time should be defined in configuration
        final Date olderThan = new Date(System.currentTimeMillis() + (3L * 24L * 3600L * 1000L));
        int sentCount = 0;
        LOG.debug("starting bulk send...");

        // process each unit in separate because of different timeouts and SMS Service options
        final List<Unit> units = unitDao.getAll();

        for (final Unit unit : units) {
            final List<Event> foundEvents = eventDao.findEvents2BulkSend(unit, olderThan);
            LOG.info("found events to bulk send, size=" + foundEvents.size() + ", unit=" + unit.getName());

            for (final Event fe : foundEvents) {
                /** BF23 begin */
                final Event event = eventDao.getById(fe.getId());
                event.getAuthor(); /** BF23: make eager load before detach */
                event.getCustomer(); event.getCustomer().getUnit();
                event.getProcedure(); event.getProcedure().getUnit();
                /** BF23 end */
                try {
                    final Event maybeSent = send(event, false);
                    if (null != maybeSent.getSent()) {
                        sentCount++;
                        // decrease the message limit if the unit is limited
                        if (null != unit.getMsgLimit()) {
                            unit.setMsgLimit(unit.getMsgLimit() - 1L);
                        }
                    }
                } catch (Throwable t) {
                    LOG.error("failed to send SMS, id=" + event.getId(), t);
                }
            }
            // store if the unit is limited
            if (null != unit.getMsgLimit()) { unitDao.persist(unit); }
        }

        LOG.info("sent " + sentCount + " event(s)");
        return sentCount;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public List<Pair<User, Map<String, Integer>>> getEventStatistics(
            final Object unitId, final Object userId, final Date from, final Date to) {

        throw new IllegalStateException("not implemented yet");
//        // find membership for given user on given unit
//        final List<Membership> memberships = membershipDao.findBy("unit", unitId, "user", userId, null);
//        if (0 == memberships.size()) {
//            throw new IllegalStateException("membership not found, unitId=" + unitId + ", userId=" + userId);
//        } else if (memberships.size() > 1) {
//            throw new IllegalStateException("too many membership found, unitId=" + unitId + ", userId=" + userId);
//        }
//        final Membership userMemb = memberships.get(0);
//
//        // get user included into statistic
//        final List<User> users = new ArrayList<User>();
//
//        // Admin -> return other members
//        if (Membership.Role.ADMIN.equals(userMemb.enumRole())) {
//            // find other users
//            final List<Membership> other = membershipDao.findBy("unit", unitId, null);
//            for (Membership m : other) {
//                users.add(m.getUser());
//            }
//        } else {
//            users.add(userMemb.getUser());
//        }
//
//        final List<Pair<User, Map<String, Integer>>> rslt = new ArrayList<Pair<User, Map<String, Integer>>>();
//        long eventCount = 0;
//
//        // get event statistics for collected users
//        for (User u : users) {
//            final List<Event> foundEvents = eventDao.findByAuthorAndPeriod(u.getId(), from, to, true);
//            eventCount += foundEvents.size();
//            int sent = 0;
//            int failed = 0;
//            int deleted = 0;
//            for (Event s : foundEvents) {
//                if (null != s.getSent()) { sent++; }
//                if (s.getSendAttemptCount() >= Event.MAX_SEND_ATTEMPTS) { failed++; }
//                if (0 != (s.getStatus() & Event.STATUS_DELETED)) { deleted++; }
//            }
//            Map<String, Integer> stat = new HashMap<String, Integer>();
//            stat.put(Event.DELETED, deleted);
//            stat.put(Event.SENT, sent);
//            stat.put(Event.FAILED, failed);
//            stat.put(Event.SUM, foundEvents.size());
//            rslt.add(new Pair<User, Map<String, Integer>>(u, stat));
//        }
//        LOG.info("collected statistics for " + rslt.size() + " user(s), eventCount=" + eventCount);
//
//        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        final List<Event> rslt = eventDao.getAll();
        LOG.info("found all events, size=" + rslt.size());
        return rslt;
    }

    // -------------------------------------------------------- Assistant Stuff


    /**
     * Sends event/message.
     * @param event2send event to be sent
     * @param watchLimitOnUnit whether limited unit should be persisted after sending (disabled for bulk sending)
     * @return the event with persisted corresponding attributes
     */
    private Event send(final Event event2send, final boolean watchLimitOnUnit) {
        Customer customer = event2send.getCustomer();
        Unit unit = customer.getUnit();
        // workaround for a strange behavior of ODB v1.7.9
        // ^^ sometimes the unit is null
        // ^^ after that, procedure of last sent event is stored with unit==null !!
        if (null == unit) {
            LOG.warn("BUG23, unit on customer is NULL, reload of customer with id=" + customer.getId());
            customer = customerDao.getById(customer.getId());
            unit = unitDao.getById(customer.getUnit().getId());
            //customer.setUnit(unit);
        }
        // end of workaround
        assertLimitedUnit(unit);

        final String text2send = format(event2send);

        // SMS
        boolean smsOk = false;
        if ((customer.getSendingChannel() & Event.CHANNEL_SMS) > 0) {
            smsOk = sendSms(event2send);
        }

        // EMAIL
        boolean emailOk = false;
        if ((customer.getSendingChannel() & Event.CHANNEL_EMAIL) > 0) {
            emailOk = sendEmail(event2send);
        }

        LOG.info("event to be sent, id=" + event2send.getId() + ", smsOk=" + smsOk + ", emailOk=" + emailOk
                + ", text=" + text2send);
        if (smsOk || emailOk) {
            event2send.setSent(new Date());
            if (watchLimitOnUnit) {
                decreaseLimitedMessages(unit);
            }
        }
        event2send.setSendAttemptCount(event2send.getSendAttemptCount() + 1);

        return eventDao.persist(event2send);
    }

    /**
     * Sends given events as email.
     * @param event2send event to be sent
     * @return <i>true</i> if successfully sent
     */
    private boolean sendEmail(final Event event2send) {
        final Customer customer = event2send.getCustomer();
        final Unit unit = event2send.getCustomer().getUnit();
        final String text2send = format(event2send);

        if (null == customer.getEmail() || 0 == customer.getEmail().trim().length()) {
            LOG.warn("email cannot be sent, wrong customer email address, eventId=" + event2send.getId());
            return false;
        }

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(unit.getEmail());
        message.setTo(customer.getEmail());
        message.setSubject(unit.getName());
        message.setText(text2send);

        boolean rslt;
        try {
            mailSender.send(message);
            rslt = true;
            LOG.info("event sent via email, id=" + event2send.getId() + ", address=" + customer.getEmail());
        } catch (Throwable t) {
            LOG.error("failed to send email, ID=" + event2send.getId(), t);
            rslt = false;
        }
        return rslt;
    }

    /**
     * Sends given events as SMS.
     * @param event2send event to be sent
     * @return <i>true</i> if successfully sent
     */
    private boolean sendSms(final Event event2send) {
        final Customer customer = event2send.getCustomer();
        final Unit unit = customer.getUnit();

        final String text2send = TextUtils.convert2ascii(format(event2send));
        final Map<String, Object> unitOptions = getUnitOptions(unit);
        @SuppressWarnings("unchecked")
        final Map<String, String> smsOptions = (Map<String, String>) unitOptions.get("sms");

        try {
            assertSmsOptions(smsOptions);
            smsGatewayService.send(customer.getPhoneNumber(), text2send, smsOptions);
            LOG.info("event sent via SMS, id=" + event2send.getId() + ", phone=" + customer.getPhoneNumber());
        } catch (Throwable t) {
            LOG.error("failed to send SMS, ID=" + event2send.getId(), t);
            return false;
        }
        return true;
    }

//    /**
//     * Method for email address validation.
//     * @param emailAddress email address
//     * @return true if is the email address valid
//     */
//    private boolean isValidEmailAddress(final String emailAddress) {
//        String  expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
//        CharSequence inputStr = emailAddress;
//        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(inputStr);
//        return matcher.matches();
//    }

    /**
     * Replaces in event text predefined sequences.
     * @param event event with text to format
     * @return formated event text
     */
    private String format(final Event event) {
        if (null == event.getAuthor()) { throw new IllegalArgumentException("event without author"); }

        final GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone(event.getAuthor().getTimezone()));
        final DateFormat df = new SimpleDateFormat("dd.MM.yy");
        final DateFormat tf = new SimpleDateFormat("H:mm");
        df.setCalendar(gc);
        tf.setCalendar(gc);

        final Map<String, String> replaceConf = new HashMap<String, String>();
        // date & time
        if (null != event.getStartTime()) { // can be null for special events
            replaceConf.put("#{date}", df.format(event.getStartTime()));
            replaceConf.put("#{time}", tf.format(event.getStartTime()));
        }
        // user
        if (event.getText().contains("#{doctor}")) {
            replaceConf.put("#{doctor}", event.getAuthor().getFullname());
        }

        return TextUtils.formatEventText(event.getText(), replaceConf);
    }

    /**
     * Validation of event before persistence.
     * @param event event to validate
     */
    private void validateEvent(final Event event) {
        if (null == event) { throw new NullPointerException("event cannot be null"); }
        if (null == event.getAuthor()) { throw new NullPointerException("author cannot be null"); }
        if (null == event.getAuthor().getId()) { throw new NullPointerException("author ID cannot be null"); }
        if (null == event.getCustomer()) { throw new NullPointerException("customer cannot be null"); }
        if (null == event.getCustomer().getId()) { throw new NullPointerException("customer ID cannot be null"); }
        if (null == event.getProcedure()) {
            throw new NullPointerException("procedure cannot be null");
        }
        if (null == event.getProcedure().getId()) {
            throw new NullPointerException("procedure ID cannot be null");
        }
        if (Strings.isNullOrEmpty(event.getText())) { throw new IllegalArgumentException("text cannot be blank"); }
        if (0 == event.getText().trim().length()) { throw new IllegalArgumentException("text cannot be blank"); }
        if (Event.Type.IN_CALENDAR == event.enumType()) {
            if (null == event.getStartTime()) { throw new NullPointerException("start time cannot be null"); }
            if (event.getLength() <= 0) { throw new IllegalArgumentException("length lesser then 0"); }
        }
    }

    /**
     * Asserts the unit where the SMS is sent from is not limited or the limit is not exceeded.
     * @param unit unit to assert
     */
    private void assertLimitedUnit(final Unit unit) {
        if (null != unit.getMsgLimit() && unit.getMsgLimit().longValue() <= 0) {
            throw new IllegalStateException(SmsUtils.MSG_LIMIT_EXCEEDE);
        }
    }

    /**
     * Asserts the SMS options are there and valid.
     * @param opts map with options
     */
    private void assertSmsOptions(final Map<String, String> opts) {
        if (null == opts) { throw new NullPointerException("unit options cannot be null"); }
        if (0 == opts.size() || !opts.containsKey("gateway")
                || !opts.containsKey(SmsGatewayService.METADATA_USERNAME)
                || !opts.containsKey(SmsGatewayService.METADATA_PASSWORD)) {
            throw new IllegalStateException("invalid or missing SMS options");
        }
    }

    /**
     * Decreases limit of messages to send in limited unit.
     * @param unit unit to check and decrease if necessary
     */
    private void decreaseLimitedMessages(final Unit unit) {
        if (null != unit.getMsgLimit()) {
            unit.setMsgLimit(unit.getMsgLimit() - 1L);
            unitDao.persist(unit);
            LOG.info("sent message from limited unit, unit=" + unit.getId() + ", currentLimit=" + unit.getMsgLimit());
        }
    }

    /**
     * Gets unit options as <code>Map</code>.
     * @param unit unit with options
     * @return options as Map
     */
    private Map<String, Object> getUnitOptions(final Unit unit) {
        if (null == unit || null == unit.getOptions() || 0 == unit.getOptions().trim().length()) {
            throw new IllegalArgumentException("unit options cannot be blank");
        }

        final Map<String, Object> rslt;
        try {
            rslt = objectMapper.readValue(unit.getOptions(), new TypeReference<HashMap<String, Object>>() { });
        } catch (IOException e) {
            LOG.error("failed to parse unit's options", e);
            throw new IllegalStateException("failed to parse unit's options", e);
        }
        return rslt;
    }

    /**
     * Only for testing purposes.
     * @param args CLI arguments
     * @throws IOException technical problem
     */
    public static void main(final String[] args) throws IOException {

        Map<String, Object> map = new HashMap<>();
        Map<String, String> smsGateway = new HashMap<>();
        smsGateway.put("username", "foo");
        smsGateway.put("password", "bar");
        map.put("smsGateway", smsGateway);
        map.put("key2", "value2");

        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();

        objectMapper.writeValue(stringWriter, map);

        System.out.println(stringWriter.toString()); //CSOFF

        Map<String, Object> mapFromString =
                objectMapper.readValue(
                        "{\"smsGateway\":{\"type\":\"sms.sluzba.cz\",\"username\":\"foo\",\"password\":\"bar\"}}",
                        new TypeReference<HashMap<String, Object>>() { });

        System.out.println(mapFromString); //CSOFF
    }

}
