package veny.smevente.model;

import javax.persistence.Column;
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
    private String role;
    /** Significance of the membership to other memberships of a user. */
    private int significance;

    /** User in unit. */
    private User user;

    // CHECKSTYLE:OFF
    @Column
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    @Column
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
    // CHECKSTYLE:ON


    /**
     * Virtual attribute providing an enumeration entry to identify role.
     *
     * @return membership role or <i>Role.MEMBER</i> if not defined
     * @see Role
     */
    @Transient
    @JsonIgnore
    public Role getRoleEnum() {
        if (Strings.isNullOrEmpty(role)) {
            return Role.MEMBER;
        } else {
            return Role.valueOf(role.trim());
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
            .append(")")
            .toString();
    }

}
