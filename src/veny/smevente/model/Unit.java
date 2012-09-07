package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import veny.smevente.misc.SoftDelete;

import com.google.gwt.thirdparty.guava.common.base.Strings;

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
    /** Type of unit (doctor/...). */
    @Column
    private String type;
    /** Configuration of SMS engine used by the unit. */
    @JsonIgnore
    @Column
    private String smsEngine;

    /**
     * An unit can be limited in amount of SMS that can be sent.
     * Mostly it is for a marketing purpose, just let a potential customer to try the application.
     * The values have following meaning:<ul>
     * <li>bigger than 0 - unit is limited but still able to send the SMSs
     * <li>equals to 0 or lesser than 0 - unit is limited and the credit is over
     * <li>'null' - unlimited
     * </ul>
     */
    @Column
    private Long limitedSmss;


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
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @JsonIgnore
    public String getSmsEngine() {
        return smsEngine;
    }
    public void setSmsEngine(String smsEngine) {
        this.smsEngine = smsEngine;
    }
    public Long getLimitedSmss() {
        return limitedSmss;
    }
    public void setLimitedSmss(Long limitedSmss) {
        this.limitedSmss = limitedSmss;
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
        if (Strings.isNullOrEmpty(type)) {
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
            .append("', limitedSmss=")
            .append(limitedSmss);
        rslt.append(")");
        return rslt.toString();
    }

}
