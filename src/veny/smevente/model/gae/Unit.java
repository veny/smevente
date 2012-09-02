package veny.smevente.model.gae;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.beans.BeanUtils;

import veny.smevente.model.MembershipDto;
import veny.smevente.model.Unit;
import veny.smevente.service.TextUtils;

/**
 * GAE entity representing the Organizational Unit.
 * <p/>
 * Schema versions:<ul>
 * <li>{@link http://blog.burnayev.com/2010/02/gae-developer-tip-updating-database.html}
 * <li>0 - id,name,metadata
 * <li>1 - limitedSmss
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.6.2010
 */
@Entity
public class Unit implements Serializable {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 3633544473541322970L;

    /** Primary Key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Unit name. */
    private String name;
    /** Unit metadata. */
    private String metadata;
    /**
     * An unit can be limited in amount of SMS that can be sent.
     * @see Unit#limitedSmss
     */
    private Long limitedSmss;
    /** Infrastructure schema version. */
    private Integer version = 1; // keep it always up to date (see class JavaDoc)!

    /** Users (members) in units. */
    @Transient
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
    public String getMetadata() {
        return metadata;
    }
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    public Long getLimitedSmss() {
        return limitedSmss;
    }
    public void setLimitedSmss(Long limitedSmss) {
        this.limitedSmss = limitedSmss;
    }
    public Integer getVersion() {
        return (null == version ? 0 : version);
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<MembershipDto> getMembers() {
        return members;
    }
    public void setMembers(List<MembershipDto> members) {
        this.members = members;
    }
    // CHECKSTYLE:ON

    // ------------------------------------------------------ DTO Mapping Stuff

    /**
     * Maps instance of this into corresponding DTO object.
     * @return corresponding DTO
     */
    public Unit mapToDto() {
        final Unit rslt = new Unit();
        BeanUtils.copyProperties(this, rslt, new String[] { "metadata", "members" });
        rslt.setMetadata(TextUtils.stringToMap(getMetadata()));
        return rslt;
    }

    // ------------------------------------------------------------- Java Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder rslt = new StringBuilder("UnitGae(id=")
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
                rslt.append(u.getUser().getFullname()).append(',');
            }
            rslt.deleteCharAt(rslt.length() - 1);
            rslt.append(']');
        }
        rslt.append("')");
        return rslt.toString();
    }

}
