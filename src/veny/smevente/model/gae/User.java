package veny.smevente.model.gae;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.beans.BeanUtils;

import veny.smevente.model.UserDto;
import veny.smevente.misc.SoftDelete;

/**
 * GAE entity representing the User.
 *
 * <p/>
 * Schema versions:<ul>
 * <li>{@link http://blog.burnayev.com/2010/02/gae-developer-tip-updating-database.html}
 * <li>0 - id,username,password,fullname,status,root,lastLoggedIn
 * <li>1 - deleted, REMOVED status
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.4.2010
 */
@Entity
@SoftDelete
public class User implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -3222102702739801700L;

    /** Primary Key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username. */
    private String username;
    /** Password. */
    @JsonIgnore
    private String password;
    /** User's full name. */
    private String fullname;
    /** Flag whether the object is deleted. */
    private Boolean deleted = Boolean.FALSE;
    /** Infrastructure schema version. */
    private Integer version = 1; // keep it always up to date (see class JavaDoc)!

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
    public Boolean getDeleted() {
        return null == deleted ? Boolean.FALSE : deleted;
    }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    public Integer getVersion() {
        return (null == version ? 0 : version);
    }
    public void setVersion(Integer version) {
        this.version = version;
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

    // ------------------------------------------------------ DTO Mapping Stuff

    /**
     * Maps instance of this into corresponding DTO object.
     * @return corresponding DTO
     */
    public UserDto mapToDto() {
        final UserDto rslt = new UserDto();
        BeanUtils.copyProperties(this, rslt);
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param dto corresponding DTO
     * @return instance of this
     */
    public static User mapFromDto(final UserDto dto) {
        final User rslt = new User();
        mapFromDto(dto, rslt);
        return rslt;
    }

    /**
     * Creates and map a new instance of this from corresponding DTO object.
     * @param source source DTO
     * @param destination destination GAE entity
     */
    public static void mapFromDto(final UserDto source, final User destination) {
        String[] ignore = null;
        if (UserDto.DO_NOT_CHANGE_PASSWORD.equals(source.getPassword())) {
            ignore = new String[] { "lastLoggedIn", "password" };
        } else {
            ignore = new String[] { "lastLoggedIn" };
        }
        BeanUtils.copyProperties(source, destination, ignore);
    }

    // ------------------------------------------------------------- Java Stuff

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder rslt = new StringBuilder("UserGae(id=")
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

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (!(other instanceof User)) { return false; }

        return id.equals(((User) other).getId());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.intValue();
    }
}
