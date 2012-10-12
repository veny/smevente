package veny.smevente.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.dao.EventDao;
import veny.smevente.dao.PatientDao;
import veny.smevente.dao.ProcedureDao;
import veny.smevente.dao.UnitDao;
import veny.smevente.dao.UserDao;
import veny.smevente.model.Event;
import veny.smevente.model.Patient;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.service.EventService;
import veny.smevente.service.SmsGatewayService.SmsException;
import veny.smevente.service.TextUtils;

import com.google.gwt.thirdparty.guava.common.base.Strings;


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
    private ProcedureDao mhcDao;
    /** Dependency. */
    @Autowired
    private EventDao eventDao;

    /** Dependency. */
//    @Autowired
//    private SmsGatewayService smsGatewayService;

    /** Date formatter. */
    private final DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");
    /** Time formatter. */
    private final DateFormat timeFormatter = new SimpleDateFormat("H:mm");

    /** Constructor. */
    public EventServiceImpl() {
        // inspired by http://groups.google.com/group/google-appengine-java/browse_thread/thread/d6800e75ad2ce28b

        // TODO [veny,B] should be configured in Unit
        final GregorianCalendar gc = new GregorianCalendar(new Locale("cs"));
        final TimeZone tz = TimeZone.getTimeZone("Europe/Prague");
        gc.setTimeZone(tz);
        dateFormatter.setCalendar(gc);
        timeFormatter.setCalendar(gc);
        LOG.info("TimeFormatter initialized ok, timeZone=" + timeFormatter.getTimeZone());
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Event createEvent(final Event event) {
        // TODO [veny,B] here must be a strong authorization
        validateEvent(event, true);

        // part of validation (the aggregated entities have to exist and cannot be deleted)
        userDao.getById(event.getAuthor().getId());
        patientDao.getById(event.getPatient().getId());
        mhcDao.getById(event.getProcedure().getId());

        final Event rslt = eventDao.persist(event);

        LOG.info("created event, " + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Event createAndSendSpecialEvent(final Event event) {
        final Event rslt = createEvent(event);

        final String text2send = format(rslt);

        final Unit unit = unitDao.getById(event.getPatient().getUnit().getId());
        assertLimitedUnit(unit);

//XXX        final Map<String, String> metadata = TextUtils.stringToMap(unitGae.getMetadata());
//        smsGatewayService.send(patientGae.getPhoneNumber(), text2send, metadata);

        // store the 'sent' timestamp
        rslt.setSent(new Date());
//XXX        smsGae.setStatus(sms.getStatus() | Event.STATUS_SPECIAL);
        eventDao.persist(rslt);

        // decrease the SMS limit if the unit is limited
        decreaseLimitedSmss(unit);

        LOG.info("SEND special, firstname=" + rslt.getPatient().getFirstname()
                + ", surname=" + rslt.getPatient().getSurname()
                + ", phone=" + rslt.getPatient().getPhoneNumber() + ", text=" + text2send);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Event updateEvent(final Event event) {
        validateEvent(event, false);

        // part of validation (the aggregated entities have to exist and cannot be deleted)
        userDao.getById(event.getAuthor().getId());
        patientDao.getById(event.getPatient().getId());
        mhcDao.getById(event.getProcedure().getId());

        eventDao.persist(event);
        LOG.info("updated event, " + event);
        return event;
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
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("constructed event text, eventId=" + eventId + ", text=" + text2send);
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
            e.getPatient().setUnit(null);
            e.getProcedure().setUnit(null);
        }
        LOG.info("found events, authorId=" + authorId + ", from=" + from + ", to=" + to + ", size=" + rslt.size());
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public Pair<Patient, List<Event>> findEventsByPatient(final Object patientId) {
        final Patient client = patientDao.getById(patientId);
        final List<Event> events = eventDao.findBy("patient", patientId, null);
        LOG.info("found events by patient, patientId=" + patientId + ", size=" + events.size());
        return new Pair<Patient, List<Event>>(client, events);
    }

    /** {@inheritDoc} */
    @Override
    public Event sendSms(final Object eventId) throws SmsException {
        final Event event2send = eventDao.getById(eventId);
        final Unit unit = event2send.getPatient().getUnit();
        assertLimitedUnit(unit);

        final String text2send = format(event2send);
//XXX        final Map<String, String> metadata = TextUtils.stringToMap(unitGae.getMetadata());
//        smsGatewayService.send(patientGae.getPhoneNumber(), text2send, metadata);

        // store the 'sent' timestamp
        event2send.setSent(new Date());
        eventDao.persist(event2send);

        // decrease the SMS limit if the unit is limited
        decreaseLimitedSmss(unit);

        LOG.info("SEND, id=" + eventId + ", phone=" + event2send.getPatient().getPhoneNumber()
                + ", text=" + text2send);
        return event2send;
    }

//    /** {@inheritDoc} */
//    @Override
//    public int bulkSend() {
//        // TODO [veny,B] time should be defined in configuration
//        final Date border = new Date(System.currentTimeMillis() + (3L * 24L * 3600L * 1000L));
//        LOG.info("trying to found SMSs to bulk send, border=" + border);
//
//        final List<Event> foundSmsGae = smsDao.findSms2BulkSend(border);
//        LOG.info("found SMSs to bulk send, size=" + foundSmsGae.size());
//
//        // cache of Units due to metadata & SMS limit
//        final Map<Long, Unit> unitCache = new HashMap<Long, Unit>();
//
//        int sentCount = 0;
//        for (Event smsGae : foundSmsGae) {
//            try {
//                final Patient patientGae = patientDao.getById(smsGae.getPatientId());
//
//                // unit cache
//                Unit unitGae = unitCache.get(patientGae.getUnitId());
//                if (null == unitGae) {
//                    unitGae = unitDao.getById(patientGae.getUnitId());
//                    unitCache.put(patientGae.getUnitId(), unitGae);
//                }
//                assertLimitedUnit(unitGae);
//
//                final Event sms = smsGae.mapToDto();
//                sms.setAuthor(userDao.getById(smsGae.getUserId()).mapToDto());
//
//                final String text2send = format(sms);
//                smsGatewayService.send(
//                        patientGae.getPhoneNumber(), text2send, TextUtils.stringToMap(unitGae.getMetadata()));
//
//                // store the 'sent' timestamp
//                smsGae.setSent(new Date());
//                smsDao.persist(smsGae);
//                // decrease the SMS limit if the unit is limited
//                if (null != unitGae.getLimitedSmss()) {
//                    unitGae.setLimitedSmss(unitGae.getLimitedSmss() - 1L);
//                }
//
//                sentCount++;
//            } catch (Throwable t) {
//                LOG.log(Level.WARNING, "failed to send SMS, id=" + smsGae.getId(), t);
//                smsGae.setSendAttemptCount(smsGae.getSendAttemptCount() + 1);
//                smsDao.persist(smsGae);
//            }
//        }
//        // store all units with limited SMSs
//        for (Unit unit : unitCache.values()) {
//            if (null != unit.getLimitedSmss()) {
//                unitDao.persist(unit);
//            }
//        }
//
//        LOG.info("sent " + sentCount + " SMSs");
//        return sentCount;
//    }

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
        final Map<String, String> replaceConf = new HashMap<String, String>();
        // date & time
        if (null != event.getStartTime()) { // can be null for special events
            replaceConf.put("#{date}", dateFormatter.format(event.getStartTime()));
            replaceConf.put("#{time}", timeFormatter.format(event.getStartTime()));
        }
        // user
        if (event.getText().contains("#{doctor}")) {
            if (null == event.getAuthor()) { throw new IllegalArgumentException("event without author"); }
            replaceConf.put("#{doctor}", event.getAuthor().getFullname());
        }

        return TextUtils.formatSmsText(event.getText(), replaceConf);
    }

    /**
     * Validation of event before persistence.
     * @param event event to validate
     * @param forCreate whether the object has to be created as new entry in DB
     */
    private void validateEvent(final Event event, final boolean forCreate) {
        if (null == event) { throw new NullPointerException("event cannot be null"); }
        if (null == event.getAuthor()) { throw new NullPointerException("author cannot be null"); }
        if (null == event.getAuthor().getId()) { throw new NullPointerException("author ID cannot be null"); }
        if (null == event.getPatient()) { throw new NullPointerException("patient cannot be null"); }
        if (null == event.getPatient().getId()) { throw new NullPointerException("patient ID cannot be null"); }
        if (null == event.getProcedure()) {
            throw new NullPointerException("procedure cannot be null");
        }
        if (null == event.getProcedure().getId()) {
            throw new NullPointerException("procedure ID cannot be null");
        }
        if (Strings.isNullOrEmpty(event.getText())) { throw new IllegalArgumentException("text cannot be blank"); }
        if (Event.Type.IN_CALENDAR == event.enumType()) {
            if (null == event.getStartTime()) { throw new NullPointerException("start time cannot be null"); }
            if (event.getLength() <= 0) { throw new IllegalArgumentException("length lesser then 0"); }
        }
        if (forCreate) {
            if (null != event.getId()) {
                throw new IllegalArgumentException("expected object with empty ID");
            }
        } else {
            if (null == event.getId()) { throw new NullPointerException("ID cannot be null"); }
            if (Strings.isNullOrEmpty(event.getId().toString())) {
                throw new IllegalArgumentException("ID cannot be blank");
            }
        }
    }

    /**
     * Asserts the the unit where the SMS is sent from is not limited
     * or the limit is not exceeded.
     * @param unitGae unit to assert
     */
    private void assertLimitedUnit(final Unit unitGae) {
        if (null != unitGae.getLimitedSmss() && unitGae.getLimitedSmss().longValue() <= 0) {
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
