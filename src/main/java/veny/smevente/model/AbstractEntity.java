package veny.smevente.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Basic class for all entities that holds common properties.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.8.2012
 */
@JsonIgnoreProperties({ "handler", "doc" })
public abstract class AbstractEntity {

    /**
     * Primary key.
     *
     * It's not a number because many storage engines like OrientDB or CouchDB
     * represents the entity identification in a textual form.
     *
     * @see OrientdbRid2JsonSerializer
     */
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private Object id;

    /**
     * Version of the entry.
     *
     * It's specific for some storage engines
     * or can be used for optimistic locking mechanism in RDBMS.
     */
    @Version
    @JsonSerialize(using = ToStringSerializer.class)
    private Object version;

    /**
     * Time-stamp indicating what time this object has been updated last time.
     */
    private Date updatedAt;

    /**
     * ID of user who last time updated this object.
     */
    private Object updatedBy;

    /**
     * Flag if the entity has been soft deleted
     * (not physically removed but flagged as deleted).
     */
    private boolean deleted;

    /**
     * Represents revision of the schema in which the entry has been stored.
     * It's support for schema evolution.
     */
    private String revision;

    // CHECKSTYLE:OFF
    public Object getId() {
        return id;
    }
    public void setId(Object id) {
        this.id = id;
    }
    @Transient
    public Object getVersion() {
        return version;
    }
    public void setVersion(Object version) {
        this.version = version;
    }
    @JsonIgnore
    @Column
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    @JsonIgnore
    @Column
    public Object getUpdatedBy() {
        return updatedBy;
    }
    public void setUpdatedBy(Object updatedBy) {
        this.updatedBy = updatedBy;
    }
    @JsonIgnore
    @Column
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    @Column
    public String getRevision() {
        return revision;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
    // CHECKSTYLE:ON

    /**
     * Copy attribute of this object typically constructed by background engine (Spring MVC in this case)
     * into an object loaded from DB for update. (see BF #23)
     * @param <I> class of a subclass
     * @param into the target object
     */
    public <I extends AbstractEntity> void copyForUpdate(final I into) {
        if (null != getVersion()) { into.setVersion(getVersion()); }
    }

}
