package veny.smevente.model;

import java.util.Date;

import javax.persistence.Column;

import veny.smevente.misc.SoftDelete;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity class representing the User.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 8.11.2010
 */
@SoftDelete
public class User extends AbstractEntity {

    /** The flag used when user is updated, but the password will not be changed. */
    public static final String DO_NOT_CHANGE_PASSWORD = "###########################";

    /** ID of the fake root entity. */
    public static final String ROOT_ID = "-111";
    /** User name of the fake root entity. */
    public static final String ROOT_USERNAME = "root";

    /** User name. */
    @Column
    private String username;

    /** Password. */
    @JsonIgnore
    @Column
    private String password;

    /** User's full name. */
    @Column
    private String fullname;

    /** User's time zone. */
    @Column
    private String timezone;

    /** Whether the user is a system root. */
    @JsonIgnore
    @Column
    private boolean root = false;

    /** Last log in time. */
    @JsonIgnore
    @Column
    private Date lastLoggedIn;

    // CHECKSTYLE:OFF
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
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    @JsonIgnore
    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }
    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }
    @JsonIgnore
    public boolean isRoot() {
        return root;
    }
    public void setRoot(boolean root) {
        this.root = root;
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
    public static User buildRoot() {
        final User user = new User();
        user.setId(ROOT_ID);
        user.setUsername(ROOT_USERNAME);
        user.setRoot(true);
        return user;
    }

}
