package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Entity class representing the Membership of User in Organizational Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 9.11.2010
 */
public class Membership extends AbstractEntity {

    /**
     * Enumeration of membership roles.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 5.7.2010
     */
    public enum Role {
        /** Ordinary member. */
        MEMBER,
        /** Administrator. */
        ADMIN
    }

    /** Membership role (User, Admin, ...). */
    @Column
    private String role;
    /** Significance of the membership to other memberships of a user. */
    @Column
    private int significance;

    /** User in unit. */
    @OneToOne
    private User user;
    /** Units where a user is member in. */
    @OneToOne
    private Unit unit;

    // CHECKSTYLE:OFF
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public int getSignificance() {
        return significance;
    }
    public void setSignificance(int significance) {
        this.significance = significance;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Unit getUnit() {
        return unit;
    }
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    // CHECKSTYLE:ON


    /**
     * Virtual attribute providing an enumeration entry to identify role.
     *
     * @return membership role or <i>Role.MEMBER</i> if not defined
     * @see Role
     */
    @Transient
    @JsonIgnore
    public Role enumRole() {
        String r = getRole();
        if (null == r || 0 == r.trim().length()) {
            return Role.MEMBER;
        } else {
            return Role.valueOf(r.trim());
        }
    }

    /**
     * Gets name of aggregated unit.
     *
     * @return name of aggregated unit
     */
    @Transient
    @JsonIgnore
    public String unitName() {
        if (null == getUnit() || null == getUnit().getName() || 0 == getUnit().getName().trim().length()) {
            throw new IllegalStateException("unit name not presented");
        }
        return getUnit().getName();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("Membership(id=")
            .append(getId())
            .append(", role=")
            .append(role)
            .append(", significance=")
            .append(significance)
            .append(", user=")
            .append(user.getUsername())
            .append(", unit=")
            .append(unit.getName())
            .append(")")
            .toString();
    }

}
