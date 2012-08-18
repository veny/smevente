package veny.smevente.model;

import java.io.Serializable;


/**
 * DTO entity representing the Membership in an Organizational Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 9.11.2010
 */
public class MembershipDto implements Serializable {

    /** Generated (1110304) serial version UID. */
    private static final long serialVersionUID = -1683227367944340693L;
    /**
     * Enumeration of membership types.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 5.7.2010
     */
    public enum Type {
        /** Ordinary member. */
        MEMBER,
        /** Administrator. */
        ADMIN
    }

    /** Primary Key. */
    private Long id;

    /** Membership type (User, Admin, ...). */
    private Type type;

    /** Significance of the membership to other memberships of a user. */
    private int significance;

    /** User in unit. */
    private User user;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
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
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("Membership(id=")
            .append(getId())
            .append(", type=")
            .append(type)
            .append(", significance=")
            .append(significance)
            .append(", user=")
            .append(user.getUsername())
            .append(")")
            .toString();
    }

}
