package veny.smevente.model;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * DTO entity representing the User.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
public class UserDto implements Serializable {

    /** The flag used when user is updated, but the password will not be changed. */
    public static final String DO_NOT_CHANGE_PASSWORD = "###########################";

    /** ID of the fake root entity. */
    public static final Long ROOT_ID = new Long(-111);
    /** Username of the fake root entity. */
    public static final String ROOT_USERNAME = "root";

    /** Generated (101220) serial version UID. */
    private static final long serialVersionUID = -2177298093237212572L;

    /** Primary Key. */
    private Long id;

    /** Username. */
    private String username;

    /** Password. */
    @JsonIgnore
    private String password;

    /** User's full name. */
    private String fullname;

    /** Whether the user is a system root. */
    @JsonIgnore
    private boolean root = false;

    /** Last log in time. */
    @JsonIgnore
    private Date lastLoggedIn;

    // CHECKSTYLE:OFF
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @JsonIgnore
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    @JsonIgnore
    public boolean isRoot() {
        return root;
    }
    public void setRoot(boolean root) {
        this.root = root;
    }
    @JsonIgnore
    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }
    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder rslt = new StringBuilder("User(id=")
            .append(getId())
            .append(", username='")
            .append(username)
            .append("', fullname='")
            .append(fullname)
            .append("', root=")
            .append(root)
            .append(')');
        return rslt.toString();
    }

    /**
     * Builds a fake root user to be able to log in without any user in database.
     * @return fake root user
     */
    public static UserDto buildRoot() {
        final UserDto user = new UserDto();
        user.setId(ROOT_ID);
        user.setUsername(ROOT_USERNAME);
        user.setRoot(true);
        return user;
    }

}
