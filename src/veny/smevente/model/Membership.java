package veny.smevente.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.gwt.thirdparty.guava.common.base.Strings;



/**
 * Entity class representing the Membership of User in Organizational Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 9.11.2010
 */
public class Membership extends AbstractEntity {

    /**
     * Enumeration of membership types.
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
    @OneToMany
    private Set<Unit> units;

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
    public Set<Unit> getUnits() {
        return units;
    }
    public void setUnits(Set<Unit> units) {
        this.units = units;
    }
    // CHECKSTYLE:ON


    /**
     * Adds a new unit.
     * @param unit new unit to be added
     */
    public void addUnit(final Unit unit) {
        if (null == units) { units = new HashSet<Unit>(); }
        units.add(unit);
    }

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
        if (Strings.isNullOrEmpty(r)) {
            return Role.MEMBER;
        } else {
            return Role.valueOf(r.trim());
        }
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
            .append(", units=")
            .append(null == units ? "null" : units.size())
            .append(")")
            .toString();
    }

}
