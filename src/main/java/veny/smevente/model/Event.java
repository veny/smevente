package veny.smevente.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;





import com.fasterxml.jackson.annotation.JsonProperty;

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
        IN_CALENDAR,
        /** Event to be sent immediately after creation. */
        IMMEDIATE_MESSAGE
    }

    /** Maximal number of attempts to send an event. */
    public static final int MAX_SEND_ATTEMPTS = 3;

    /** Statistics - sum. */
    public static final String SUM = "SUM";
    /** Statistics - sent. */
    public static final String SENT = "SENT";
    /** Statistics - failed. */
    public static final String FAILED = "FAILED";
    /** Statistics - deleted. */
    public static final String DELETED = "DELETED";

    /** Bit mask of SMS channel to send message. */
    public static final int CHANNEL_SMS = 1;
    /** Bit mask of email channel to send message. */
    public static final int CHANNEL_EMAIL = 2;

    /** Text. */
    @Column
    @JsonProperty
    private String text;
    /** Author. */
    @ManyToOne
    private User author;
    /** Customer. */
    @ManyToOne
    private Customer customer;
    /** Procedure. */
    @ManyToOne
    private Procedure procedure;
    /** Start time. */
    @Column
    @JsonProperty
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
    /** Count of attempts to send the event. */
    @Column
    private int sendAttemptCount;
    /** Event type. @see Event.Type */
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
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public Procedure getProcedure() {
        return procedure;
    }
    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }
    @JsonGetter
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
     * @return event type or <i>Type.IN_CALENDAR</i> if not defined
     */
    @Transient
    @JsonIgnore
    public Type enumType() {
        final String t = getType();
        if (null == t || 0 == t.trim().length()) {
            return Type.IN_CALENDAR;
        } else {
            return Type.valueOf(t.trim());
        }
    }

    /**
     * Sets author ID to a new user object.
     * @param authorId user ID
     */
    public void setAuthorId(final Object authorId) {
        if (null == authorId) { throw new NullPointerException("author ID is null"); }
        setAuthor(new User());
        getAuthor().setId(authorId);
    }
    /**
     * Sets customer ID to a new customer object.
     * @param customerId customer ID
     */
    public void setCustomerId(final Object customerId) {
        if (null == customerId) { throw new NullPointerException("customer ID is null"); }
        setCustomer(new Customer());
        getCustomer().setId(customerId);
    }
    /**
     * Sets procedure ID to a new procedure object.
     * @param procedureId procedure ID
     */
    public void setProcedureId(final Object procedureId) {
        if (null == procedureId) { throw new NullPointerException("procedure ID is null"); }
        setProcedure(new Procedure());
        getProcedure().setId(procedureId);
    }

    /** {@inheritDoc} */
    public void copyForUpdate(final Event into) {
        super.copyForUpdate(into);
        if (null != getText()) { into.setText(getText()); }
        if (null != getAuthor()) { into.setAuthor(getAuthor()); }
        if (null != getCustomer()) { into.setCustomer(getCustomer()); }
        if (null != getProcedure()) { into.setProcedure(getProcedure()); }
        into.setStartTime(getStartTime());
        into.setLength(getLength());
        if (null != getNotice()) { into.setNotice(getNotice()); }
        if (null != getType()) { into.setType(getType()); }
    }


    // ----------------------------------------------------------- Object Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("Event(id=")
            .append(getId())
            .append(", author=")
            .append(author.getUsername())
            .append(", customerFN='")
            .append(customer.fullname())
            .append("', procedure=")
            .append(procedure.getName())
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
