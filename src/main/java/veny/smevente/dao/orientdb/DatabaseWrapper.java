package veny.smevente.dao.orientdb;

import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.Patient;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;

import com.orientechnologies.orient.core.config.OStorageConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * Wrapper of OrientDB engine allowing to execute commands
 * as a Spring Template pattern.
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
     * Initializes the Entity Manager.
     */
    public void init() {
        final OObjectDatabaseTx db = get();
        db.getEntityManager().registerEntityClass(AbstractEntity.class);
        db.getEntityManager().registerEntityClass(User.class);
        db.getEntityManager().registerEntityClass(Unit.class);
        db.getEntityManager().registerEntityClass(Membership.class);
        db.getEntityManager().registerEntityClass(Patient.class);
        db.getEntityManager().registerEntityClass(Procedure.class);
        db.getEntityManager().registerEntityClass(Event.class);

        // to be sure the DB works with UTC time zone
        final OStorageConfiguration cfg = db.getStorage().getConfiguration();
        cfg.setTimeZone(TimeZone.getTimeZone("UTC"));
        cfg.update();
    }


    /**
     * Gets interface to work with OrientDB engine.
     *
     * @return object represent API to access data
     */
    public OObjectDatabaseTx get() {
        return OObjectDatabasePool.global().acquire(databaseUrl, username, password);
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
            LOG.error("failed to execute callback: " + e.getMessage(), e);
            if (tx) { db.rollback(); }
            throw e;
        } catch (final Exception e) {
            LOG.error("failed to execute callback: " + e.getMessage(), e);
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

}
