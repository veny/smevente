package veny.smevente.dao.orientdb;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;

/**
 * Wrapper of OrientDB engine allowing to execute.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
public final class DatabaseWrapper {

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
        T doWithDatabase(ODatabaseDocument db);
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
     * Gets interface to work with OrientDB engine.
     *
     * @return object represent API to access data
     */
    public ODatabaseDocument get() {
        return ODatabaseDocumentPool.global().acquire(databaseUrl, username, password);
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
        final ODatabaseDocument db = this.get();
        T rslt = null;

        try {
            if (tx) { db.begin(); }

            rslt = callback.doWithDatabase(db);

            if (tx) { db.commit(); }

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

}
