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
    @Column
    private Date updatedAt;

    /**
     * ID of user who last time updated this object.
     */
    @Column
    private String updatedBy;

    /**
     * Time-stamp indicating what time this object has been soft deleted.
     * (not physically removed but flagged as deleted).
     */
    @Column
    private Date deletedAt;

    /**
     * ID of user who deleted this object.
     */
    @Column
    private String deletedBy;

    /**
     * Represents revision of the schema in which the entry has been stored.
     * It's support for schema evolution.
     */
    @Column
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
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    @JsonIgnore
    public String getUpdatedBy() {
        return updatedBy;
    }
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    @JsonIgnore
    public Date getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }
    @JsonIgnore
    public String getDeletedBy() {
        return deletedBy;
    }
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
    public String getRevision() {
        return revision;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
    // CHECKSTYLE:ON


    /**
     * Gets flag whether this entity is already deleted.
     * @return <i>true</i> if deleted
     */
    @JsonIgnore
    public boolean isDeleted() {
        return null != getDeletedAt();
    }

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
