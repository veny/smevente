package veny.smevente.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * DTO representing the Organizational Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
public class UnitDto implements Serializable {

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


    /** Key in metadata: unit type. */
    public static final String UNIT_TYPE = "type";

    /** Generated (1110303) serial version UID. */
    private static final long serialVersionUID = 4659769889589771514L;

    /** Primary Key. */
    private Long id;

    /** Unit name. */
    private String name;

    /** Unit metadata. */
    @JsonIgnore
    private Map<String, String> metadata;

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
    private List<MembershipDto> members;


    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @JsonIgnore
    public Map<String, String> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    public Long getLimitedSmss() {
        return limitedSmss;
    }
    public void setLimitedSmss(Long limitedSmss) {
        this.limitedSmss = limitedSmss;
    }
    public List<MembershipDto> getMembers() {
        return members;
    }
    public void setMembers(List<MembershipDto> members) {
        this.members = members;
    }
    // CHECKSTYLE:ON

    /**
     * Virtual attribute based on a metadata entry with key 'type'.
     * The 'type' defines a unit categorization (e.g. doctor, hairdressing, ...).
     * @return unit type or <i>TextVariant.PATIENT</i> if not defined
     * @see TextVariant
     */
    public TextVariant getType() {
        if (null == metadata || null == metadata.get(UNIT_TYPE) || 0 == metadata.get(UNIT_TYPE).trim().length()) {
            return TextVariant.PATIENT;
        } else {
            return TextVariant.valueOf(metadata.get(UNIT_TYPE).trim());
        }
    }

    /**
     * Adds a new entry into metadata.
     * @param key entry key
     * @param value entry value
     */
    public void addMetadata(final String key, final String value) {
        if (null == getMetadata()) { setMetadata(new HashMap<String, String>()); }
        metadata.put(key, value);
    }

    /**
     * Adds a new members.
     * @param member new member to be added
     */
    public void addMember(final MembershipDto member) {
        if (null == member.getUser()) { throw new IllegalArgumentException("membership has to have a user"); }
        if (null == members) { members = new ArrayList<MembershipDto>(); }
        members.add(member);
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
        if (null == members) {
            rslt.append(", members=null");
        } else {
            rslt.append(", members=[");
            for (MembershipDto u : members) {
                rslt.append(u.getUser().getUsername()).append(',');
            }
            rslt.deleteCharAt(rslt.length() - 1);
            rslt.append(']');
        }
        rslt.append(")");
        return rslt.toString();
    }

}
