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
    private String name;
    /** Unit description. */
    private String description;
    /** Type of unit (doctor/...). */
    private String type;
    /** Configuration of SMS engine used by the unit. */
    @JsonIgnore
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
    private Long limitedSmss;

    /** Members in units. */
//XXX    private List<MembershipDto> members;


    // CHECKSTYLE:OFF
    @Column
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Column
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    @Column
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @JsonIgnore
    @Column
    public String getSmsEngine() {
        return smsEngine;
    }
    public void setSmsEngine(String smsEngine) {
        this.smsEngine = smsEngine;
    }
    @Column
    public Long getLimitedSmss() {
        return limitedSmss;
    }
    public void setLimitedSmss(Long limitedSmss) {
        this.limitedSmss = limitedSmss;
    }
//    public List<MembershipDto> getMembers() {
//        return members;
//    }
//    public void setMembers(List<MembershipDto> members) {
//        this.members = members;
//    }
    // CHECKSTYLE:ON

    /**
     * Virtual attribute based on a metadata entry with key 'type'.
     * The 'type' defines a unit categorization (e.g. doctor, hairdressing, ...).
     * @return unit type or <i>TextVariant.PATIENT</i> if not defined
     * @see TextVariant
     */
    @Transient
    @JsonIgnore
    public TextVariant getTypeEnum() {
        if (Strings.isNullOrEmpty(type)) {
            return TextVariant.PATIENT;
        } else {
            return TextVariant.valueOf(type.trim());
        }
    }

    /**
     * Adds a new members.
     * @param member new member to be added
     */
    public void addMember(final MembershipDto member) {
        if (null == member.getUser()) { throw new IllegalArgumentException("membership has to have a user"); }
//        if (null == members) { members = new ArrayList<MembershipDto>(); }
//        members.add(member);
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
//XXX        if (null == members) {
//            rslt.append(", members=null");
//        } else {
//            rslt.append(", members=[");
//            for (MembershipDto u : members) {
//                rslt.append(u.getUser().getUsername()).append(',');
//            }
//            rslt.deleteCharAt(rslt.length() - 1);
//            rslt.append(']');
//        }
        rslt.append(")");
        return rslt.toString();
    }

}