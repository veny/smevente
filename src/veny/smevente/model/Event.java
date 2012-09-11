package veny.smevente.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.gwt.thirdparty.guava.common.base.Strings;

import veny.smevente.misc.SoftDelete;

/**
 * Entity class representing an Event.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 12.11.2010
 */
@SoftDelete
public class Event extends AbstractEntity {

    /**
     * Enumeration of event types.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 10.9.2012
     */
    public enum Type {
        /** Standard event to be sent by timer. */
        STANDARD,
        /** Event to be sent immediately after creation. */
        SPECIAL
    }

    /** Maximal number of attempts to send a SMS. */
    public static final int MAX_SEND_ATTEMPTS = 3;

    /** Statistics - sum. */
    public static final String SUM = "SUM";
    /** Statistics - sent. */
    public static final String SENT = "SENT";
    /** Statistics - failed. */
    public static final String FAILED = "FAILED";
    /** Statistics - deleted. */
    public static final String DELETED = "DELETED";

    /** Text. */
    @Column
    private String text;
    /** Author. */
    @ManyToOne
    private User author;
    /** Patient. */
    @ManyToOne
    private Patient patient;
    /** Medical Help Category. */
    @ManyToOne
    private MedicalHelpCategory medicalHelpCategory;
    /** Start time. */
    @Column
    private Date startTime;
    /** Length [minutes]. */
    @Column
    private int length;
    /** Notice. */
    @Column
    private String notice;
    /** Date when the event was sent. */
    @Column
    private Date sent;
    /** Count of attempts to send the SMS. */
    @Column
    private int sendAttemptCount;
    /** Event type. */
    @Column
    private String type;

    // CHECKSTYLE:OFF
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }
    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    public MedicalHelpCategory getMedicalHelpCategory() {
        return medicalHelpCategory;
    }
    public void setMedicalHelpCategory(MedicalHelpCategory medicalHelpCategory) {
        this.medicalHelpCategory = medicalHelpCategory;
    }
    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
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
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    // CHECKSTYLE:ON

    /**
     * Virtual attribute providing an enumeration entry to identify event type.
     *
     * @return event type or <i>Type.STANDARD</i> if not defined
     * @see Role
     */
    @Transient
    @JsonIgnore
    public Type enumType() {
        String t = getType();
        if (Strings.isNullOrEmpty(t)) {
            return Type.STANDARD;
        } else {
            return Type.valueOf(t.trim());
        }
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
            .append(patient.fullname())
            .append("', mhc=")
            .append(medicalHelpCategory.getName())
            .append(", startTime=")
            .append(startTime)
            .append(", length=")
            .append(length)
            .append(", text='")
            .append(text.length() > 10 ? text.substring(0, 10) + "..." : text)
            .append("', sent=")
            .append(sent)
            .append(", sendAttemptCount=")
            .append(sendAttemptCount)
            .append(", type=")
            .append(type)
            .append(", notice='")
            .append(null != notice && notice.length() > 10 ? notice.substring(0, 10) + "..." : notice)
            .append("')")
            .toString();
    }

}