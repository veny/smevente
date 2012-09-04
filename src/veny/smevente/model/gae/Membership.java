package veny.smevente.model.gae;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.beans.BeanUtils;

import veny.smevente.model.Membership;

/**
 * GAE entity representing the Membership in an Organizational Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.6.2010
 */
@Entity
public class Membership implements Serializable {

    /** Generated serial version UID. */
    private static final long serialVersionUID = 5421006518425678262L;

    /** Primary Key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Membership type (User, Admin, ...). */
    private Membership.Type type;
    /** Significance of the membership to other memberships of a user. */
    private int significance;
    /** ID of user. */
    private Long userId;
    /** ID of unit. */
    private Long unitId;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Membership.Type getType() {
        return type;
    }
    public void setType(Membership.Type type) {
        this.type = type;
    }
    public int getSignificance() {
        return significance;
    }
    public void setSignificance(int significance) {
        this.significance = significance;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getUnitId() {
        return unitId;
    }
    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }
    // CHECKSTYLE:ON

    // ------------------------------------------------------ DTO Mapping Stuff

    /**
     * Maps instance of this into corresponding DTO object.
     * @return corresponding DTO
     */
    public Membership mapToDto() {
        final Membership rslt = new Membership();
        BeanUtils.copyProperties(this, rslt, new String[] { "userId", "unitId" });
        return rslt;
    }

    // ------------------------------------------------------------- Java Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("MembershipGae(id=")
            .append(getId())
            .append(", type=")
            .append(type)
            .append(", userId=")
            .append(userId)
            .append(", unitId=")
            .append(unitId)
            .append(")")
            .toString();
    }

}
