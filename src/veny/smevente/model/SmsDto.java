package veny.smevente.model;

import java.util.Date;

/**
 * DTO representing the SMS.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 12.11.2010
 */
public class SmsDto {

    /** Maximal number of attempts to send a SMS. */
    public static final int MAX_SEND_ATTEMPTS = 3;
    /** Status == deleted. */
    public static final int STATUS_DELETED = 16;
    /** Status == sent as special. */
    public static final int STATUS_SPECIAL = 32;

    /** Statistics - sum. */
    public static final String SUM = "SUM";
    /** Statistics - sent. */
    public static final String SENT = "SENT";
    /** Statistics - failed. */
    public static final String FAILED = "FAILED";
    /** Statistics - deleted. */
    public static final String DELETED = "DELETED";

    /** Primary Key. */
    private Long id;

    /** Text. */
    private String text;
    /** Author. */
    private UserDto author;
    /** Patient. */
    private PatientDto patient;
    /** Medical Help Category. */
    private MedicalHelpCategoryDto medicalHelpCategory;
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
    public UserDto getAuthor() {
        return author;
    }
    public void setAuthor(UserDto author) {
        this.author = author;
    }
    public PatientDto getPatient() {
        return patient;
    }
    public void setPatient(PatientDto patient) {
        this.patient = patient;
    }
    public MedicalHelpCategoryDto getMedicalHelpCategory() {
        return medicalHelpCategory;
    }
    public void setMedicalHelpCategory(MedicalHelpCategoryDto medicalHelpCategory) {
        this.medicalHelpCategory = medicalHelpCategory;
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

    // ---------------------------------------------------- Convenience Methods

    /**
     * Sets author ID to a new SMS object.
     * @param authorId user ID
     */
    public void setAuthorId(final Long authorId) {
        if (null == authorId || authorId.longValue() <= 0) {
            throw new IllegalArgumentException("invalid user ID (null or less than 0)");
        }
        setAuthor(new UserDto());
        getAuthor().setId(authorId);
    }

    /**
     * Sets patient ID to a new SMS object.
     * @param patientId patient ID
     */
    public void setPatientId(final Long patientId) {
        if (null == patientId || patientId.longValue() <= 0) {
            throw new IllegalArgumentException("invalid patient ID (null or less than 0)");
        }
        setPatient(new PatientDto());
        getPatient().setId(patientId);
    }

    /**
     * Sets medical help category ID to a new SMS object.
     * @param mhcId medical help category ID
     */
    public void setMedicalHelpCategoryId(final Long mhcId) {
        if (null == mhcId || mhcId.longValue() <= 0) {
            throw new IllegalArgumentException("invalid MHC ID (null or less than 0)");
        }
        setMedicalHelpCategory(new MedicalHelpCategoryDto());
        getMedicalHelpCategory().setId(mhcId);
    }

    // ----------------------------------------------------------- Object Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("Sms(id=")
            .append(getId())
            .append(", author=")
            .append(author.getUsername())
            .append(", patientId='")
            .append(patient.getFullname())
            .append("', mhc=")
            .append(medicalHelpCategory.getName())
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
            .append(null != notice && notice.length() > 10 ? notice.substring(0, 10) + "..." : notice)
            .append("')")
            .toString();
    }

}
