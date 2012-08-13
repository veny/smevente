package veny.smevente.model.gae;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.beans.BeanUtils;

import veny.smevente.model.SmsDto;

/**
 * GAE entity representing the SMS.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.7.2010
 */
@Entity
public class Sms {

    /** Primary Key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Text. */
    private String text;
    /** Author. */
    private Long userId;
    /** Patient. */
    private Long patientId;
    /** Medical Help Category. */
    private Long medicalHelpCategoryId;
    /** Medical help start time. */
    private Date medicalHelpStartTime;
    /** Medical help length [minutes]. */
    private int medicalHelpLength;
    /** Notice. */
    private String notice;
    /** Date when the SMS was sent. */
    private Date sent;
    /** Count of attempts to send the SMS. */
    private int sendAttemptCount;
    /** SMS status. */
    private Integer status;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getPatientId() {
        return patientId;
    }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    public Long getMedicalHelpCategoryId() {
        return medicalHelpCategoryId;
    }
    public void setMedicalHelpCategoryId(Long medicalHelpCategoryId) {
        this.medicalHelpCategoryId = medicalHelpCategoryId;
    }
    public Date getMedicalHelpStartTime() {
        return medicalHelpStartTime;
    }
    public void setMedicalHelpStartTime(Date medicalHelpStartTime) {
        this.medicalHelpStartTime = medicalHelpStartTime;
    }
    public int getMedicalHelpLength() {
        return medicalHelpLength;
    }
    public void setMedicalHelpLength(int medicalHelpLength) {
        this.medicalHelpLength = medicalHelpLength;
    }
    public String getNotice() {
        return notice;
    }
    public void setNotice(String notice) {
        this.notice = notice;
    }
    public Date getSent() {
        return sent;
    }
    public void setSent(Date sent) {
        this.sent = sent;
    }
    public int getSendAttemptCount() {
        return sendAttemptCount;
    }
    public void setSendAttemptCount(int sendAttemptCount) {
        this.sendAttemptCount = sendAttemptCount;
    }
    public Integer getStatus() {
        return (null == status ? 0 : status);
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    // CHECKSTYLE:ON

    // ------------------------------------------------------ DTO Mapping Stuff

    /**
     * Maps instance of this into corresponding DTO object.
     * @return corresponding DTO
     */
    public SmsDto mapToDto() {
        final SmsDto rslt = new SmsDto();
        BeanUtils.copyProperties(this, rslt, new String[] { "userId", "patientId", "medicalHelpCategoryId" });
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param dto corresponding DTO
     * @return instance of this
     */
    public static Sms mapFromDto(final SmsDto dto) {
        final Sms rslt = new Sms();
        mapFromDto(dto, rslt);
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param source source DTO
     * @param destination destination GAE entity
     */
    public static void mapFromDto(final SmsDto source, final Sms destination) {
        BeanUtils.copyProperties(source, destination, new String[] { "author", "patient", "medicalHelpCategory" });
    }

    // ------------------------------------------------------------- Java Stuff
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("SmsGae(id=")
            .append(getId())
            .append(", authorId=")
            .append(userId)
            .append(", patientId=")
            .append(patientId)
            .append(", mhcId=")
            .append(medicalHelpCategoryId)
            .append(", mhStart=")
            .append(medicalHelpStartTime)
            .append(", mhcLen=")
            .append(medicalHelpLength)
            .append(", text='")
            .append(text.length() > 10 ? text.substring(0, 10) + "..." : text)
            .append("', sent=")
            .append(sent)
            .append(", sendAttemptCount=")
            .append(sendAttemptCount)
            .append(", status=")
            .append(status)
            .append(", notice='")
            .append(notice.length() > 10 ? notice.substring(0, 10) + "..." : notice)
            .append(')')
            .toString();
    }

}
