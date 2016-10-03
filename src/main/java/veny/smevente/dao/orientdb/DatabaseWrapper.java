package veny.smevente.dao.orientdb;

import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.DisposableBean;

import com.orientechnologies.orient.core.config.OStorageConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;

/**
 * Wrapper of OrientDB engine allowing to execute commands
 * as a Spring Template pattern.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
@SuppressWarnings("deprecation")
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
        db.getEntityManager().registerEntityClass(Customer.class);
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
        // Ugly hack for ODB 2.0.10 -> 2.2.7
        // com.orientechnologies.orient.core.exception.OConfigurationException: Error on opening database: the engine 'remote' was not found
        // with Jetty class loader in Super Dev mode
        try {
            Class.forName("com.orientechnologies.orient.client.remote.OEngineRemote");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("did you insert the orientdb-client.jar into your classpath?", e);
        }

        // Ugly hack for ODB 2.0.10 -> 2.2.7
        // java.lang.NoClassDefFoundError: Could not initialize class com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
        // see https://github.com/orientechnologies/orientdb/issues/5146
        if (ODatabaseRecordThreadLocal.INSTANCE == null) {
            System.err.println("Calling this manually normally prevent initialization issues."); //CSOFF
        }

        // OObjectDatabasePool is deprecated, but the new approach does not work in v2.2.7
        // => ODatabaseException: The database instance is not set in the current thread
        //pool = new OPartitionedDatabasePool(databaseUrl, username, password);
        //db = new OObjectDatabaseTx(pool.acquire());

        final OObjectDatabaseTx db = OObjectDatabasePool.global().acquire(databaseUrl, username, password);

        // Ugly hack for ODB 2.0.10 -> 2.2.7
        // OConcurrentModificationException occurs
        db.getLocalCache().invalidate();

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
     * @deprecated tx should be managed by Service layer, not by DAOs
     */
    public <T> T execute(final ODatabaseCallback<T> callback, final boolean tx) {
        final OObjectDatabaseTx db = this.get();
        T rslt = null;

        try {
            if (tx) {
                db.begin();
            }

            rslt = callback.doWithDatabase(db);

            if (tx) {
                db.commit();
            }

        } catch (final RuntimeException e) {
            LOG.log(Level.SEVERE, "failed to execute callback: " + e.getMessage(), e);
            if (tx) {
                db.rollback();
            }
            throw e;
        } catch (final Exception e) {
            LOG.log(Level.SEVERE, "failed to execute callback: " + e.getMessage(), e);
            if (tx) {
                db.rollback();
            }
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
