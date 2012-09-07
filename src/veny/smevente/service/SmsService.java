package veny.smevente.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.Patient;
import veny.smevente.model.SmsDto;
import veny.smevente.model.User;
import veny.smevente.service.SmsGatewayService.SmsException;

/**
 * SMS service.
 * There are following methods to send a SMS(s):<ul>
 * <li>sendSms(Long)
 * <li>createAndSendSpecialSms(SmsDto)
 * <li>bulkSend()
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
public interface SmsService {

    /**
     * Creates new SMS.
     *
     * @param sms SMS to be created
     * @return created <code>Sms</code> instance (inclusive aggregated entities on first level)
     */
    SmsDto createSms(SmsDto sms);

    /**
     * Creates and sends a new special SMS.
     *
     * @param sms SMS to be created
     * @return created SMS
     */
    SmsDto createAndSendSpecialSms(SmsDto sms);

    /**
     * Updates given SMS.
     *
     * @param sms SMS to be updated
     * @return the complex SMS (inclusive aggregated entities)
     */
    SmsDto updateSms(SmsDto sms);

    /**
     * Removes given SMS from underlying persistence service if it was not sent
     * or marks as deleted (soft delete) if already sent.
     *
     * @param smsId SMS ID to delete
     */
    void deleteSms(Long smsId);

    /**
     * Marks given SMS as deleted.
     *
     * @param smsId SMS ID to delete
     */
    @Deprecated // replaced with AI in deleteSms
    void deleteSmsSoft(Long smsId);

    /**
     * Gets text for a SMS with given ID constructed the same way how 'bulkSend' make it.
     * @param smsId SMS ID to construct the text
     * @return text of the SMS
     */
    String getSmsText(Long smsId);

    /**
     * Gets complex SMS by given ID.
     *
     * @param smsId SMS ID
     * @return the complex SMS (inclusive aggregated entities)
     */
    SmsDto getById(Long smsId);

    /**
     * Gets list of SMSs for given author and time period (not deleted).
     * This methods does not distinguish between units to see all terms of a given author.
     *
     * @param authorId author ID
     * @param from date from
     * @param to date to
     * @return list of complex SMS (inclusive aggregated entities)
     */
    List<SmsDto> findSms(Long authorId, Date from, Date to);

    /**
     * Gets list of SMSs for given patient (excluding the deleted and special SMSs).
     *
     * @param patientId patient ID
     * @return the patient and his list of SMS sorted descending by start date
     * (author & MHC is set, NO the unit)
     */
    Pair<Patient, List<SmsDto>> findSmsByPatient(Long patientId);

    /**
     * Sends SMS with given ID.
     *
     * @param smsId SMS ID
     * @return the complex SMS (inclusive aggregated entities)
     * @throws SmsException if sending fails
     */
    SmsDto sendSms(Long smsId) throws SmsException;

    /**
     * Invoked by cron task to send SMS.
     *
     * @return count of successfully sent SMSs
     */
    int bulkSend();

    /**
     * Gets SMS statistics for given user and unit.
     * Returns:<ul>
     * <li>only one item if given user is MEMBER in given unit
     * <li>list of items for all unit members if the given user is ADMIN in given unit
     * </ul>
     *
     * @param unitId unit ID
     * @param userId user ID
     * @param from date from
     * @param to date to
     * @return list of SMS statistics
     */
    List<Pair<User, Map<String, Integer>>> getSmsStatistic(Long unitId, Long userId, Date from, Date to);

    /**
     * Loads all SMSs.
     *
     * @return list of all SMSs
     */
    @Deprecated // only for unit testing purposes
    List<SmsDto> getAllSmss();

}
