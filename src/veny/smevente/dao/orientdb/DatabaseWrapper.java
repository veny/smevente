package veny.smevente.dao.orientdb;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.DisposableBean;

import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Membership;
import veny.smevente.model.Patient;
import veny.smevente.model.Unit;
import veny.smevente.model.User;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * Wrapper of OrientDB engine allowing to execute.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
public final class DatabaseWrapper implements DisposableBean {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(DatabaseWrapper.class.getName());


    // CHECKSTYLE:OFF
    private String databaseUrl;
    private String username;
    private String password;
    // CHECKSTYLE:ON

    /**
     * Callback interface to define an action that uses the OrientDB wrapper.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 18.8.2012
     * @param <T> data type handled by the action
     */
    public interface ODatabaseCallback<T> {
        /**
         * Calls a defined action with an active database wrapper.
         * Provider of the action does not need to care about activating or
         * closing the database connection or about the transaction
         * management.
         *
         * @param db interface to work with OrientDB engine
         * @return a result object, or <code>null</code> if none
         */
        T doWithDatabase(OObjectDatabaseTx db);
    }


    /**
     * Gets database URL.
     * @return database URL
     */
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * Sets database URL.
     * @param databaseUrl database URL
     */
    public void setDatabaseUrl(final String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    /**
     * Sets database user name.
     * @param username database user name
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Sets database user password.
     * @param password database user password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets a flag if schema initialization should be applied.
     * @param init <i>true</i> for schema initialization
     */
    public void setInit(final boolean init) {
        final OObjectDatabaseTx db = get();

        if (init) {

            // Object class workaround
            // https://groups.google.com/forum/?fromgroups=#!topic/orient-database/LmvtY-rQsbg
//            if (!db.getMetadata().getSchema().existsClass("Object")) {
//                db.getMetadata().getSchema().createClass("Object");
//            }

            // delete classes
//            if (db.getMetadata().getSchema().existsClass(Patient.class.getSimpleName())) {
//                db.command(new OCommandSQL("DELETE FROM " + Patient.class.getSimpleName())).execute();
//                db.getMetadata().getSchema().dropClass(Patient.class.getSimpleName());
//            }
//            if (db.getMetadata().getSchema().existsClass("Membership")) {
//                db.getMetadata().getSchema().dropClass("Membership");
//            }
//            if (db.getMetadata().getSchema().existsClass("Unit")) {
//                db.getMetadata().getSchema().dropClass("Unit");
//            }
//            if (db.getMetadata().getSchema().existsClass("User")) {
//                db.getMetadata().getSchema().dropClass("User");
//            }
//            if (db.getMetadata().getSchema().existsClass("AbstractEntity")) {
//                db.getMetadata().getSchema().dropClass("AbstractEntity");
//            }

            if (!db.getMetadata().getSchema().existsClass(AbstractEntity.class.getSimpleName())) {
                // AbstractEntity
                OClass entity = db.getMetadata().getSchema().createAbstractClass(AbstractEntity.class.getSimpleName());
                entity.createProperty("deleted", OType.BOOLEAN); //.setMandatory(true);
                entity.createProperty("revision", OType.STRING);
                // User
                OClass user = db.getMetadata().getSchema().createClass(User.class.getSimpleName(), entity);
                user.createProperty("username", OType.STRING).setMandatory(true).setNotNull(true);
                user.createProperty("password", OType.STRING).setMandatory(true).setNotNull(true);
                user.createProperty("fullname", OType.STRING).setMandatory(true).setNotNull(true);
                // Unit
                OClass unit = db.getMetadata().getSchema().createClass(Unit.class.getSimpleName(), entity);
                unit.createProperty("name", OType.STRING).setMandatory(true).setNotNull(true);
                // Membership
                OClass membership = db.getMetadata().getSchema().createClass(Membership.class.getSimpleName(), entity);
                membership.createProperty("user", OType.LINK, user).setMandatory(true);
                membership.createProperty("unit", OType.LINK, unit).setMandatory(true);
                membership.createProperty("role", OType.STRING).setMandatory(true).setNotNull(true);
                membership.createProperty("significance", OType.INTEGER);
                // Patient
                OClass patient = db.getMetadata().getSchema().createClass(Patient.class.getSimpleName(), entity);
                patient.createProperty("unit", OType.LINK, unit).setMandatory(true);
                patient.createProperty("firstname", OType.STRING).setMandatory(true).setNotNull(true);
                patient.createProperty("surname", OType.STRING).setMandatory(true).setNotNull(true);
                patient.createProperty("phoneNumber", OType.STRING);
                patient.createProperty("birthNumber", OType.STRING);
                patient.createProperty("degree", OType.STRING);
                patient.createProperty("street", OType.STRING);
                patient.createProperty("city", OType.STRING);
                patient.createProperty("zipCode", OType.STRING);
                patient.createProperty("employer", OType.STRING);
                patient.createProperty("careers", OType.STRING);
            }
        }

        db.getEntityManager().registerEntityClass(AbstractEntity.class);
        db.getEntityManager().registerEntityClass(User.class);
        db.getEntityManager().registerEntityClass(Unit.class);
        db.getEntityManager().registerEntityClass(Membership.class);
        db.getEntityManager().registerEntityClass(Patient.class);

        db.close();
    }


    /**
     * Gets interface to work with OrientDB engine.
     *
     * @return object represent API to access data
     */
//    public ODatabaseDocument get() {
//        return ODatabaseDocumentPool.global().acquire(databaseUrl, username, password);
//    }
    public OObjectDatabaseTx get() {
//        return OObjectDatabasePool.global().acquire(databaseUrl, username, password);
        OObjectDatabaseTx db = new OObjectDatabaseTx(databaseUrl).open(username, password);
        return db;
    }

    /**
     * Execute an action specified by the given callback object without transaction.
     *
     * @param callback callback object that specifies the action
     * @return an object returned by the action, or <code>null</code>
     * @param <T> data type handled by the action
     */
    public <T> T execute(final ODatabaseCallback<T> callback) {
        return execute(callback, false);
    }

    /**
     * Execute an action specified by the given callback object.
     *
     * @param callback callback object that specifies the action
     * @param tx whether the transaction mode should be enabled
     * @return an object returned by the action, or <code>null</code>
     * @param <T> data type handled by the action
     */
    public <T> T execute(final ODatabaseCallback<T> callback, final boolean tx) {
        final OObjectDatabaseTx db = this.get();
        T rslt = null;

        try {
            if (tx) { db.begin(); }

            rslt = callback.doWithDatabase(db);

            if (tx) { db.commit(); }

        } catch (final RuntimeException e) {
            LOG.log(Level.SEVERE, "failed to execute callback: " + e.getMessage(), e);
            if (tx) { db.rollback(); }
            throw e;
        } catch (final Exception e) {
            LOG.log(Level.SEVERE, "failed to execute callback: " + e.getMessage(), e);
            if (tx) { db.rollback(); }
            throw new IllegalStateException("failed to execute callback: " + e.getMessage(), e);
        } finally {
            if (null != db) {
                db.close();
            }
        }
        return rslt;
    }

    // --------------------------------------------------- DisposableBean Stuff

    /** {@inheritDoc} */
    @Override
    public void destroy() throws Exception {
        ODatabaseDocumentPool.global().close();
        LOG.info("Connection pool properly closed");
    }

    // ---------------------------------------- Document<->Entity Mapping Stuff

//    public ODocument createDocument(AbstractEntity entity) {
//        final ODocument doc = new ODocument(entity.getClass().getSimpleName());
//
//        final PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(entity);
//        for (PropertyDescriptor pd : pds) {
//            final Annotation col = pd.getReadMethod().getAnnotation(Column.class);
//            if (null != col) {
//                final String propName = pd.getName();
//                try {
//                    doc.field(propName, PropertyUtils.getProperty(entity, propName));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        // RID, Version
//        if (!Strings.isNullOrEmpty(entity.getId())) { doc.setIdentity(new ORecordId(entity.getId())); }
//        if (!Strings.isNullOrEmpty(entity.getVersion())) { doc.setVersion(Integer.parseInt(entity.getVersion())); }
//        doc.setClassName(entity.getClass().getSimpleName());
//
//        return doc;
//    }
//
//    public AbstractEntity createValueObject(final ODocument doc, final Class clazz) {
//        AbstractEntity rslt = null;
//        try {
//            rslt = (AbstractEntity) clazz.newInstance();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // RID -> ID
//        rslt.setId(doc.getIdentity().toString());
//        // document version
//        rslt.setVersion(Integer.toString(doc.getVersion()));
//
//        final String[] fieldNames = doc.fieldNames();
//        for (String fieldName : fieldNames) {
//            try {
//                PropertyUtils.setProperty(rslt, fieldName, doc.field(fieldName));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return rslt;
//    }

}
