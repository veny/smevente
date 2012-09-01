package veny.smevente.model;

import javax.persistence.Column;
import javax.persistence.Transient;

/**
 * Basic class for all entities that holds common properties.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.8.2012
 */
public abstract class AbstractEntity {

    /**
     * Primary key.
     *
     * It's not a number because many storage engines like OrientDB or CouchDB
     * represents the entity identification in a textual form.
     */
    private String id;

    /**
     * Version of the entry.
     *
     * It's specific for some storage engines
     * or can be used for optimistic locking mechanism in RDBMS.
     */
    private String version;

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
    @Transient
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @Transient
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
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
