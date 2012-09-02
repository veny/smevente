package veny.smevente.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import veny.smevente.client.utils.Triple;
import veny.smevente.dao.jpa.gae.MedicalHelpCategoryDaoGae;
import veny.smevente.dao.jpa.gae.MembershipDaoGae;
import veny.smevente.dao.jpa.gae.PatientDaoGae;
import veny.smevente.dao.jpa.gae.SmsDaoGae;
import veny.smevente.dao.jpa.gae.UserDaoGae;
import veny.smevente.dao.orientdb.UnitDaoImpl;
import veny.smevente.model.MembershipDto;
import veny.smevente.model.PatientDto;
import veny.smevente.model.SmsDto;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.model.gae.MedicalHelpCategory;
import veny.smevente.model.gae.Membership;
import veny.smevente.model.gae.Patient;
import veny.smevente.model.gae.Sms;
import veny.smevente.model.gae.Unit;
import veny.smevente.model.gae.User;
import veny.smevente.service.SmsGatewayService;
import veny.smevente.service.SmsGatewayService.SmsException;
import veny.smevente.service.SmsService;
import veny.smevente.service.TextUtils;


/**
 * Implementation of SMS service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
public class SmsServiceImpl implements SmsService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(SmsServiceImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UserDaoGae userDao;
    /** Dependency. */
    @Autowired
    private MembershipDaoGae membershipDao;
    /** Dependency. */
    @Autowired
    private UnitDaoImpl unitDao;
    /** Dependency. */
    @Autowired
    private PatientDaoGae patientDao;
    /** Dependency. */
    @Autowired
    private MedicalHelpCategoryDaoGae mhcDao;
    /** Dependency. */
    @Autowired
    private SmsDaoGae smsDao;

    /** Dependency. */
    @Autowired
    private SmsGatewayService smsGatewayService;

    /** Date formatter. */
    private final DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");
    /** Time formatter. */
    private final DateFormat timeFormatter = new SimpleDateFormat("H:mm");

    /** Constructor. */
    public SmsServiceImpl() {
//        final TimeZone tz = TimeZone.getTimeZone("GMT+2:00");
//        dateFormatter.setTimeZone(tz);
//        timeFormatter.setTimeZone(tz);

        // inspired by http://groups.google.com/group/google-appengine-java/browse_thread/thread/d6800e75ad2ce28b

        // TODO [veny,B] should be configured in Unit
        final GregorianCalendar gc = new GregorianCalendar(new Locale("cs"));
        final TimeZone tz = TimeZone.getTimeZone("Europe/Prague");
        gc.setTimeZone(tz);
        dateFormatter.setCalendar(gc);
        timeFormatter.setCalendar(gc);
        LOG.info("TimeFormatter initialized ok, timeZone=" + timeFormatter.getTimeZone());
    }

    /*
     * Here is not used a TX because of GAE Entity Group limit for transaction.
     * I avoided to use TX (not needed here).
     */
    /** {@inheritDoc} */
    @Override
    public SmsDto createSms(final SmsDto sms) {
        // TODO [veny,B] here must be a strong authorization

        validateSms(sms);

        // part of validation (the aggregated entities has to exist)
        final Triple<User, Patient, MedicalHelpCategory> agg = readAggregated(sms);
        if (agg.getB().getUnitId().longValue() != agg.getC().getUnitId().longValue()) {
            throw new IllegalArgumentException("MHC and Patient not from the same unit");
        }

        final Sms smsGae = Sms.mapFromDto(sms);
        smsGae.setUserId(sms.getAuthor().getId());
        smsGae.setPatientId(sms.getPatient().getId());
        smsGae.setMedicalHelpCategoryId(sms.getMedicalHelpCategory().getId());

        smsDao.create(smsGae);
        final SmsDto rslt = smsGae.mapToDto();
        fillAggregated(rslt, agg.getA(), agg.getB(), agg.getC());

        LOG.info("created SMS, " + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    public SmsDto createAndSendSpecialSms(final SmsDto sms) {
        if (null == sms.getAuthor()) { throw new NullPointerException("author cannot be null"); }
        if (null == sms.getAuthor().getId()) { throw new NullPointerException("author ID cannot be null"); }
        if (null == sms.getPatient()) { throw new NullPointerException("patient cannot be null"); }
        if (null == sms.getPatient().getId()) { throw new NullPointerException("patient ID cannot be null"); }
        if (null == sms.getText()) { throw new NullPointerException("SMS text cannot be null"); }
        if (sms.getText().trim().isEmpty()) { throw new IllegalArgumentException("SMS text cannot be blank"); }

        final User authorGae = userDao.getById(sms.getAuthor().getId());
        final Patient patientGae = patientDao.getById(sms.getPatient().getId());

        sms.setAuthor(authorGae.mapToDto());
        final String text2send = format(sms);

        final Unit unitGae = unitDao.getById(patientGae.getUnitId());
        assertLimitedUnit(unitGae);
        final Map<String, String> metadata = TextUtils.stringToMap(unitGae.getMetadata());
        smsGatewayService.send(patientGae.getPhoneNumber(), text2send, metadata);

        // store the 'sent' timestamp
        final Sms smsGae = Sms.mapFromDto(sms);
        smsGae.setSent(new Date());
        smsGae.setStatus(sms.getStatus() | SmsDto.STATUS_SPECIAL);
        smsDao.persist(smsGae);

        // decrease the SMS limit if the unit is limited
        decreaseLimitedSmss(unitGae);

        final SmsDto rslt = smsGae.mapToDto();
        fillAggregated(rslt, authorGae, patientGae, null);

        LOG.info("SEND special, firstname=" + patientGae.getFirstname() + ", surname=" + patientGae.getSurname()
                + ", phone=" + patientGae.getPhoneNumber() + ", text=" + text2send);
        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    public SmsDto updateSms(final SmsDto sms) {
        if (null == sms.getId()) { throw new NullPointerException("SMS ID cannot be null"); }
        validateSms(sms);

        // part of validation (the aggregated entities has to exist)
        final Triple<User, Patient, MedicalHelpCategory> agg = readAggregated(sms);

        final Sms smsGae = smsDao.getById(sms.getId());
        Sms.mapFromDto(sms, smsGae);
        smsGae.setUserId(sms.getAuthor().getId());
        smsGae.setPatientId(sms.getPatient().getId());
        smsGae.setMedicalHelpCategoryId(sms.getMedicalHelpCategory().getId());
        smsDao.persist(smsGae);

        fillAggregated(sms, agg.getA(), agg.getB(), agg.getC());
        LOG.info("updated SMS, " + sms);
        return sms;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteSms(final Long smsId) {
        if (smsDao.removeHardOrSoft(smsId)) {
            LOG.info("deleted SMS, id=" + smsId);
        } else {
            LOG.info("deleted SMS (soft), id=" + smsId);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteSmsSoft(final Long smsId) {
        final Sms smsGae = smsDao.getById(smsId);
        smsGae.setStatus(smsGae.getStatus() | SmsDto.STATUS_DELETED);
        smsDao.persist(smsGae);
        LOG.info("deleted SMS (soft), id=" + smsId);
    }

    /** {@inheritDoc} */
    @Override
    public String getSmsText(final Long smsId) {
        final Sms smsGae = smsDao.getById(smsId);
        final User authorGae = userDao.getById(smsGae.getUserId());

        final SmsDto sms = smsGae.mapToDto();
        sms.setAuthor(authorGae.mapToDto());
        final String text2send = format(sms);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("constructed SMS text, smsId=" + smsId + ", text=" + text2send);
        }
        return text2send;
    }

    /** {@inheritDoc} */
    @Override
    public SmsDto getById(final Long smsId) {
        final Sms smsGae = smsDao.getById(smsId);
        final SmsDto rslt = smsGae.mapToDto();
        rslt.setAuthorId(smsGae.getUserId());
        rslt.setPatientId(smsGae.getPatientId());
        rslt.setMedicalHelpCategoryId(smsGae.getMedicalHelpCategoryId());
        readAndFillAggregated(rslt);
        return rslt;
    }

    /*
     * Here is not used a TX because of GAE Entity Group limit for transaction.
     * I avoided to use TX (not needed here).
     */
    /** {@inheritDoc} */
    @Override
    public List<SmsDto> findSms(final Long userId, final Date from, final Date to) {
        final List<Sms> foundSmsGae = smsDao.findByAuthorAndPeriod(userId, from, to, false);
        final List<SmsDto> rslt = new ArrayList<SmsDto>();

        for (Sms smsGae : foundSmsGae) {
            final SmsDto sms = smsGae.mapToDto();
            sms.setAuthorId(smsGae.getUserId());
            sms.setPatientId(smsGae.getPatientId());
            sms.setMedicalHelpCategoryId(smsGae.getMedicalHelpCategoryId());
            readAndFillAggregated(sms);
            rslt.add(sms);
        }

        LOG.info("found SMSs, userId=" + userId + ", from=" + from + ", to=" + to + ", size=" + rslt.size());
        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    public Pair<PatientDto, List<SmsDto>> findSmsByPatient(final Long patientId) {
        final PatientDto patient = patientDao.getById(patientId).mapToDto();

        final List<Sms> foundSmsGae = smsDao.findByPatient(patientId);
        final List<SmsDto> smss = new ArrayList<SmsDto>();

        final Map<Long, User> authorCache = new HashMap<Long, User>();

        for (Sms smsGae : foundSmsGae) {
            final SmsDto sms = smsGae.mapToDto();
            // Author
            if (!authorCache.containsKey(smsGae.getUserId())) {
                final User user = userDao.getById(smsGae.getUserId()).mapToDto();
                authorCache.put(smsGae.getUserId(), user);
            }
            // MHC
            final MedicalHelpCategory mhcGae = mhcDao.getById(smsGae.getMedicalHelpCategoryId());
            sms.setAuthor(authorCache.get(smsGae.getUserId()));
            sms.setMedicalHelpCategory(mhcGae.mapToDto());

            smss.add(sms);
        }

        LOG.info("found SMSs by patient, patientId=" + patientId + ", size=" + smss.size());
        return new Pair<PatientDto, List<SmsDto>>(patient, smss);
    }

    /** {@inheritDoc} */
    @Override
    public SmsDto sendSms(final Long smsId) throws SmsException {
        final Sms smsGae = smsDao.getById(smsId);
        final User authorGae = userDao.getById(smsGae.getUserId());
        final Patient patientGae = patientDao.getById(smsGae.getPatientId());
        final MedicalHelpCategory mhcGae = mhcDao.getById(smsGae.getMedicalHelpCategoryId());
        final Unit unitGae = unitDao.getById(patientGae.getUnitId());
        assertLimitedUnit(unitGae);

        final SmsDto rslt = smsGae.mapToDto();
        fillAggregated(rslt, authorGae, patientGae, mhcGae);

        final String text2send = format(rslt);
        final Map<String, String> metadata = TextUtils.stringToMap(unitGae.getMetadata());
        smsGatewayService.send(patientGae.getPhoneNumber(), text2send, metadata);
        // store the 'sent' timestamp
        smsGae.setSent(new Date());
        rslt.setSent(smsGae.getSent());
        smsDao.persist(smsGae);
        // decrease the SMS limit if the unit is limited
        decreaseLimitedSmss(unitGae);

        LOG.info("SEND, id=" + smsId + ", phone=" + patientGae.getPhoneNumber() + ", text=" + text2send);
        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    public int bulkSend() {
        // TODO [veny,B] time should be defined in configuration
        final Date border = new Date(System.currentTimeMillis() + (3L * 24L * 3600L * 1000L));
        LOG.info("trying to found SMSs to bulk send, border=" + border);

        final List<Sms> foundSmsGae = smsDao.findSms2BulkSend(border);
        LOG.info("found SMSs to bulk send, size=" + foundSmsGae.size());

        // cache of Units due to metadata & SMS limit
        final Map<Long, Unit> unitCache = new HashMap<Long, Unit>();

        int sentCount = 0;
        for (Sms smsGae : foundSmsGae) {
            try {
                final Patient patientGae = patientDao.getById(smsGae.getPatientId());

                // unit cache
                Unit unitGae = unitCache.get(patientGae.getUnitId());
                if (null == unitGae) {
                    unitGae = unitDao.getById(patientGae.getUnitId());
                    unitCache.put(patientGae.getUnitId(), unitGae);
                }
                assertLimitedUnit(unitGae);

                final SmsDto sms = smsGae.mapToDto();
                sms.setAuthor(userDao.getById(smsGae.getUserId()).mapToDto());

                final String text2send = format(sms);
                smsGatewayService.send(
                        patientGae.getPhoneNumber(), text2send, TextUtils.stringToMap(unitGae.getMetadata()));

                // store the 'sent' timestamp
                smsGae.setSent(new Date());
                smsDao.persist(smsGae);
                // decrease the SMS limit if the unit is limited
                if (null != unitGae.getLimitedSmss()) {
                    unitGae.setLimitedSmss(unitGae.getLimitedSmss() - 1L);
                }

                sentCount++;
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "failed to send SMS, id=" + smsGae.getId(), t);
                smsGae.setSendAttemptCount(smsGae.getSendAttemptCount() + 1);
                smsDao.persist(smsGae);
            }
        }
        // store all units with limited SMSs
        for (Unit unit : unitCache.values()) {
            if (null != unit.getLimitedSmss()) {
                unitDao.persist(unit);
            }
        }

        LOG.info("sent " + sentCount + " SMSs");
        return sentCount;
    }

    /** {@inheritDoc} */
    @Override
    public List<Pair<User, Map<String, Integer>>> getSmsStatistic(
            final Long unitId, final Long userId, final Date from, final Date to) {

        // find membership for given user on given unit
        final List<Membership> memberships = membershipDao.findBy("unitId", unitId, "userId", userId, null);
        if (0 == memberships.size()) {
            throw new IllegalStateException("membership not found, unitId=" + unitId + ", userId=" + userId);
        } else if (memberships.size() > 1) {
            throw new IllegalStateException("too many membership found, unitId=" + unitId + ", userId=" + userId);
        }
        final Membership userMemb = memberships.get(0);

        // get user included into statistic
        final List<User> users = new ArrayList<User>();

        // Admin -> return other members
        if (MembershipDto.Type.ADMIN.equals(userMemb.getType())) {
            // find other users
            final List<Membership> other = membershipDao.findBy("unitId", unitId, null);
            for (Membership m : other) {
                users.add(userDao.getById(m.getUserId()));
            }
        } else {
            users.add(userDao.getById(userId));
        }

        final List<Pair<User, Map<String, Integer>>> rslt = new ArrayList<Pair<User, Map<String, Integer>>>();
        long smsCount = 0;

        // get SMS statistics for collected users
        for (User u : users) {
            final User userDto = u.mapToDto();
            final List<Sms> foundSmsGae = smsDao.findByAuthorAndPeriod(u.getId(), from, to, true);
            smsCount += foundSmsGae.size();
            int sent = 0;
            int failed = 0;
            int deleted = 0;
            for (Sms s : foundSmsGae) {
                if (null != s.getSent()) { sent++; }
                if (s.getSendAttemptCount() >= SmsDto.MAX_SEND_ATTEMPTS) { failed++; }
                if (0 != (s.getStatus() & SmsDto.STATUS_DELETED)) { deleted++; }
            }
            Map<String, Integer> stat = new HashMap<String, Integer>();
            stat.put(SmsDto.DELETED, deleted);
            stat.put(SmsDto.SENT, sent);
            stat.put(SmsDto.FAILED, failed);
            stat.put(SmsDto.SUM, foundSmsGae.size());
            rslt.add(new Pair<User, Map<String, Integer>>(userDto, stat));
        }
        LOG.info("collected statistics for " + rslt.size() + " user(s), smsCount=" + smsCount);

        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<SmsDto> getAllSmss() {
        final List<Sms> found = smsDao.getAll();
        LOG.info("found all SMSs, size=" + found.size());

        final List<SmsDto> rslt = new ArrayList<SmsDto>();
        for (Sms smsGae : found) {
            final SmsDto sms = smsGae.mapToDto();
            final Patient patientGae = patientDao.getById(smsGae.getPatientId());
            final Unit unit = unitDao.getById(patientGae.getUnitId()).mapToDto();
            sms.setAuthor(userDao.getById(smsGae.getUserId()).mapToDto());
            sms.setPatient(patientGae.mapToDto());
            sms.getPatient().setUnit(unit);
            sms.setMedicalHelpCategory(mhcDao.getById(smsGae.getMedicalHelpCategoryId()).mapToDto());
            sms.getMedicalHelpCategory().setUnit(unit);
            rslt.add(sms);
        }
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
     * Replaces in SMS text predefined sequences.
     * @param sms SMS
     * @return replaced SMS text
     */
    private String format(final SmsDto sms) {
        final Map<String, String> replaceConf = new HashMap<String, String>();
        // date & time
        if (null != sms.getMedicalHelpStartTime()) { // can be null for special SMSs
            replaceConf.put("#{date}", dateFormatter.format(sms.getMedicalHelpStartTime()));
            replaceConf.put("#{time}", timeFormatter.format(sms.getMedicalHelpStartTime()));
        }
        // user
        if (sms.getText().contains("#{doctor}")) {
            if (null == sms.getAuthor()) { throw new IllegalArgumentException("SMS without author"); }
//            final UserGae user = userDao.getById(sms.getAuthor().getId());
            replaceConf.put("#{doctor}", sms.getAuthor().getFullname());
        }

        return TextUtils.formatSmsText(sms.getText(), replaceConf);
    }

    /**
     * Validation of a SMS before persistence.
     * @param sms SMS to validate
     */
    private void validateSms(final SmsDto sms) {
        if (null == sms.getAuthor()) { throw new NullPointerException("author cannot be null"); }
        if (null == sms.getAuthor().getId()) { throw new NullPointerException("author ID cannot be null"); }
        if (null == sms.getPatient()) { throw new NullPointerException("patient cannot be null"); }
        if (null == sms.getPatient().getId()) { throw new NullPointerException("patient ID cannot be null"); }
        if (null == sms.getMedicalHelpCategory()) {
            throw new NullPointerException("service type cannot be null");
        }
        if (null == sms.getMedicalHelpCategory().getId()) {
            throw new NullPointerException("service type ID category cannot be null");
        }
        if (null == sms.getMedicalHelpStartTime()) { throw new NullPointerException("start time cannot be null"); }
        if (sms.getMedicalHelpLength() <= 0) { throw new IllegalArgumentException("length lesser then 0"); }
    }

    /**
     * Fills given SMS DTO with GAE entities.
     * @param fill SMS to be filled
     * @param authorGae author
     * @param patientGae patient
     * @param mhcGae service type
     */
    private void fillAggregated(
            final SmsDto fill, final User authorGae,
            final Patient patientGae, final MedicalHelpCategory mhcGae) {

        fill.setAuthor(authorGae.mapToDto());
        fill.setPatient(patientGae.mapToDto());
        if (null != mhcGae) { fill.setMedicalHelpCategory(mhcGae.mapToDto()); } // can be null for special SMS
    }

    /**
     * Reads aggregated GAE entities.
     * @param fill SMS inclusive IDs of desired aggregated entities
     * @return triple of aggregated entities
     */
    private Triple<User, Patient, MedicalHelpCategory> readAggregated(final SmsDto fill) {
        final User authorGae = userDao.getById(fill.getAuthor().getId());
        final Patient patientGae = patientDao.getById(fill.getPatient().getId());
        final MedicalHelpCategory mhcGae = mhcDao.getById(fill.getMedicalHelpCategory().getId());
        return new Triple<User, Patient, MedicalHelpCategory>(authorGae, patientGae, mhcGae);
    }

    /**
     * Reads aggregated GAE entities and fills with it the given SMS DTO.
     * @param fill SMS to be filled (inclusive IDs of desired aggregated entities)
     */
    private void readAndFillAggregated(final SmsDto fill) {
        final Triple<User, Patient, MedicalHelpCategory> found = readAggregated(fill);
        fillAggregated(fill, found.getA(), found.getB(), found.getC());
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
     * @param unitGae unit to check and decrease if neccessary
     */
    private void decreaseLimitedSmss(final Unit unitGae) {
        if (null != unitGae.getLimitedSmss()) {
            unitGae.setLimitedSmss(unitGae.getLimitedSmss() - 1L);
            unitDao.persist(unitGae);
            LOG.info("sent SMS from limited unit, current limit=" + unitGae.getLimitedSmss());
        }
    }

}
