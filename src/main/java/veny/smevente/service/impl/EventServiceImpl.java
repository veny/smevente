package veny.smevente.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.dao.EventDao;
import veny.smevente.dao.PatientDao;
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
import veny.smevente.service.SmsGatewayService.SmsException;
import veny.smevente.service.TextUtils;

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
//    /** Dependency. */
//    @Autowired
//    private MembershipDao membershipDao;
    /** Dependency. */
    @Autowired
    private UnitDao unitDao;
    /** Dependency. */
    @Autowired
    private PatientDao patientDao;
    /** Dependency. */
    @Autowired
    private ProcedureDao procedureDao;
    /** Dependency. */
    @Autowired
    private EventDao eventDao;

    /** Dependency. */
    @Autowired
    private SmsGatewayService smsGatewayService;


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
        final User author = userDao.getById(event.getAuthor().getId());
        event.setAuthor(author);
        // customer
        if (null == event.getCustomer() || null == event.getCustomer().getId()) {
            throw new NullPointerException("unknown customer");
        }
        final Customer customer = patientDao.getById(event.getCustomer().getId());
        event.setCustomer(customer);
        // procedure
        if (null == event.getProcedure() || null == event.getProcedure().getId()) {
            throw new NullPointerException("unknown procedure");
        }
        final Procedure proc = procedureDao.getById(event.getProcedure().getId());
        event.setProcedure(proc);

        validateEvent(event);

        final Event rslt = eventDao.persist(event);
        LOG.info((null == event.getId() ? "created new event, " : "event updated, ") + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Event createAndSendSpecialEvent(final Event event) {
        event.setType(Event.Type.IMMEDIATE_MESSAGE.toString());
        final Event rslt = storeEvent(event);

        final String text2send = format(rslt);

        final Unit unit = unitDao.getById(event.getCustomer().getUnit().getId());
        assertLimitedUnit(unit);

        final Map<String, String> options = TextUtils.stringToMap(event.getCustomer().getUnit().getSmsGateway());
        smsGatewayService.send(event.getCustomer().getPhoneNumber(), text2send, options);

        // store the 'sent' timestamp
        rslt.setSent(new Date());
        eventDao.persist(rslt);

        // decrease the SMS limit if the unit is limited
        decreaseLimitedSmss(unit);

        LOG.info("SEND special, firstname=" + rslt.getCustomer().getFirstname()
                + ", surname=" + rslt.getCustomer().getSurname()
                + ", phone=" + rslt.getCustomer().getPhoneNumber() + ", text=" + text2send);
        return rslt;
    }

//    /** {@inheritDoc} */
//    @Transactional
//    @Override
//    public Event updateEvent(final Event event) {
//        validateEvent(event, false);
//
//        // part of validation (the aggregated entities have to exist and cannot be deleted)
//        userDao.getById(event.getAuthor().getId());
//        patientDao.getById(event.getPatient().getId());
//        mhcDao.getById(event.getProcedure().getId());
//
//        eventDao.persist(event);
//        LOG.info("updated event, " + event);
//        return event;
//    }

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
        LOG.info("found event by id=" + id);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public List<Event> findEvents(final Object authorId, final Date from, final Date to) {
        final List<Event> rslt = eventDao.findByAuthorAndPeriod(authorId, from, to, false);

        // delete 3-level associations because of:
        //  com.fasterxml.jackson.databind.JsonMappingException: Database 'remote:/smevente' is closed
        //  (through reference chain: java.util.HashMap["events"]->
        //    java.util.ArrayList[0]->veny.smevente.model.Event_$$_javassist_5["patient"]->
        //    veny.smevente.model.Patient_$$_javassist_3["unit"]->veny.smevente.model.Unit_$$_javassist_2["deleted"])
        //
        // and of course, we don't need them
        for (Event e : rslt) {
            e.getCustomer().setUnit(null);
            e.getProcedure().setUnit(null);
        }
        LOG.info("found events, authorId=" + authorId + ", from=" + from + ", to=" + to + ", size=" + rslt.size());
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public Pair<Customer, List<Event>> findEventsByPatient(final Object customerId) {
        final Customer client = patientDao.getById(customerId);
        final List<Event> events = eventDao.findByPatient(customerId);
        // delete customers, it is key A
        for (Event e : events) { e.setCustomer(null); }
        LOG.info("found events by customer, customerId=" + customerId + ", size=" + events.size());
        return new Pair<Customer, List<Event>>(client, events);
    }

    /** {@inheritDoc} */
    @Override
    public Event sendSms(final Object eventId) throws SmsException {
        final Event event2send = eventDao.getById(eventId);
        // unit is not loaded with event because of 'detachWithFirstLevelAssociations'
        final Unit unit = unitDao.getById(event2send.getCustomer().getUnit().getId());
        assertLimitedUnit(unit);

        final String text2send = format(event2send);
        final Map<String, String> options = TextUtils.stringToMap(unit.getSmsGateway());
        smsGatewayService.send(event2send.getCustomer().getPhoneNumber(), text2send, options);

        // store the 'sent' timestamp
        event2send.setSent(new Date());
        eventDao.persist(event2send);

        // decrease the SMS limit if the unit is limited
        decreaseLimitedSmss(unit);

        LOG.info("SEND, id=" + eventId + ", phone=" + event2send.getCustomer().getPhoneNumber()
                + ", text=" + text2send);
        return event2send;
    }

    /** {@inheritDoc} */
    @Override
    public int bulkSend() {
        // TODO [veny,B] time should be defined in configuration
        final Date olderThan = new Date(System.currentTimeMillis() + (3L * 24L * 3600L * 1000L));
        int sentCount = 0;

        // process each unit in separate because of different timeouts and SMS Service options
        final List<Unit> units = unitDao.getAll();

        for (final Unit unit : units) {
            final List<Event> foundEvents = eventDao.findEvents2BulkSend(unit, olderThan);
            LOG.info("found events to bulk send, size=" + foundEvents.size() + ", unit=" + unit.getName());

            for (Event event : foundEvents) {
                // assert limit of messages
                if (null != unit.getLimitedSmss() && unit.getLimitedSmss().longValue() <= 0) {
                    continue;
                }

                try {
                    final Customer customer = event.getCustomer();

                    final String text2send = format(event);
                    smsGatewayService.send(
                            customer.getPhoneNumber(), text2send, TextUtils.stringToMap(unit.getSmsGateway()));

                    // store the 'sent' timestamp
                    event.setSent(new Date());
                    eventDao.persist(event);
                    // decrease the SMS limit if the unit is limited
                    if (null != unit.getLimitedSmss()) {
                        unit.setLimitedSmss(unit.getLimitedSmss() - 1L);
                    }

                    sentCount++;
                } catch (Throwable t) {
                    LOG.log(Level.WARN, "failed to send SMS, id=" + event.getId(), t);
                    event.setSendAttemptCount(event.getSendAttemptCount() + 1);
                    eventDao.persist(event);
                }
            }
            // store changed unit with limited messages
            if (null != unit.getLimitedSmss()) {
                unitDao.persist(unit);
            }
        }

        LOG.info("sent " + sentCount + " SMSs");
        return sentCount;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public List<Pair<User, Map<String, Integer>>> getEventStatistic(
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

        return TextUtils.formatSmsText(event.getText(), replaceConf);
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
     * Asserts the the unit where the SMS is sent from is not limited or the limit is not exceeded.
     * @param unit unit to assert
     */
    private void assertLimitedUnit(final Unit unit) {
        if (null != unit.getLimitedSmss() && unit.getLimitedSmss().longValue() <= 0) {
            throw new IllegalStateException(SmsUtils.SMS_LIMIT_EXCEEDE);
        }
    }

    /**
     * Decreases limit of SMSs to send in limited unit.
     * @param unitGae unit to check and decrease if necessary
     */
    private void decreaseLimitedSmss(final Unit unitGae) {
        if (null != unitGae.getLimitedSmss()) {
            unitGae.setLimitedSmss(unitGae.getLimitedSmss() - 1L);
            unitDao.persist(unitGae);
            LOG.info("sent SMS from limited unit, current limit=" + unitGae.getLimitedSmss());
        }
    }

}
