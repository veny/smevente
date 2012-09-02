package veny.smevente.model;

import java.util.Date;

import javax.persistence.Column;

import org.codehaus.jackson.annotate.JsonIgnore;

import veny.smevente.misc.SoftDelete;

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
    @Column
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @JsonIgnore
    @Column
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Column
    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    @JsonIgnore
    @Column
    public boolean isRoot() {
        return root;
    }
    public void setRoot(boolean root) {
        this.root = root;
    }
    @JsonIgnore
    @Column
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
    public static User buildRoot() {
        final User user = new User();
        user.setId(ROOT_ID);
        user.setUsername(ROOT_USERNAME);
        user.setRoot(true);
        return user;
    }

}