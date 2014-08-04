package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.Transient;

import veny.smevente.misc.SoftDelete;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity class representing the Organizational Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
@SoftDelete
public class Unit extends AbstractEntity {

    /**
     * This enumeration represents the variants of localized texts.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 2.4.2011
     */
    public enum TextVariant {
        /** Medical environment (patient, MHC, ...). */
        PATIENT,
        /** Commercial environment (client, type of work, ...). */
        CUSTOMER
    }


    /** Unit name. */
    @Column
    private String name;
    /** Unit description. */
    @Column
    private String description;
    /** Email. */
    @Column
    private String email;
    /** Type of unit (doctor/...). */
    @Column
    private String type;
    /** Configuration of SMS gateway used by the unit. */
    @JsonIgnore
    @Column
    private String options;

    /**
     * An unit can be limited in amount of messages that can be sent.
     * Mostly it is for a marketing purpose, just let a potential customer to try the application.
     * The values have following meaning:<ul>
     * <li>bigger than 0 - unit is limited but still able to send messages
     * <li>equals to 0 or lesser than 0 - unit is limited and the credit is over
     * <li>'null' - unlimited
     * </ul>
     */
    @Column
    private Long msgLimit;


    // CHECKSTYLE:OFF
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @JsonIgnore
    public String getOptions() {
        return options;
    }
    public void setOptions(String options) {
        this.options = options;
    }
    public Long getMsgLimit() {
        return msgLimit;
    }
    public void setMsgLimit(Long msgLimit) {
        this.msgLimit = msgLimit;
    }
    // CHECKSTYLE:ON

    /**
     * Virtual attribute providing an enumeration entry to identify type.
     * The 'type' defines a unit categorization (e.g. doctor, hairdressing, ...).
     * @return unit type or <i>TextVariant.PATIENT</i> if not defined
     * @see TextVariant
     */
    @Transient
    @JsonIgnore
    public TextVariant enumTextVariant() {
        String t = getType();
        if (null == t || 0 == t.trim().length()) {
            return TextVariant.PATIENT;
        } else {
            return TextVariant.valueOf(type.trim());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder rslt = new StringBuilder("Unit(id=")
            .append(getId())
            .append(", name='")
            .append(name)
            .append("', msgLimit=")
            .append(msgLimit);
        rslt.append(")");
        return rslt.toString();
    }

    /**
     * Filters unit's options for client side usage.
     * Currently it lets out a flag whether the unit can send SMS messages.
     * @return options from client side perspective
     */
    @Transient
    @JsonProperty
    public String getClientOptions() {
        final String opts = getOptions();
        return "sms=" + (null != opts && opts.contains("sms") && opts.contains("gateway"));
    }
    /**
     * Gets flag whether the unit can send SMS messages.
     * @return flag whether the unit can send SMS messages.
     */
    @Transient
    @JsonIgnore
    public boolean isSmsEnabled() {
        return (null != options && options.contains("sms=true"));
    }

}
