package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

import veny.smevente.server.OrientdbRid2JsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
    @JsonSerialize(using = OrientdbRid2JsonSerializer.class)
    private Object id;

    /**
     * Version of the entry.
     *
     * It's specific for some storage engines
     * or can be used for optimistic locking mechanism in RDBMS.
     */
    @Version
    private Object version;

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

}
