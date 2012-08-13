package veny.smevente.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * A collection of GAE JPA oriented utilities.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 24.7.2010
 */
public final class JpaGaeUtils {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(JpaGaeUtils.class.getName());

    /**
     * Because an <code>EntityManagerFactory</code> instance takes time to
     * initialize, it's a good idea to reuse a single instance.
     */
    private static final EntityManagerFactory EMF_INSTANCE =
        Persistence.createEntityManagerFactory("transactions-optional");

    /**
     * Callback interface to define an action that uses the JPA interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 24.7.2010
     * @param <T> data type handled by the action
     */
    public interface JpaCallback<T> {
        /**
         * Calls a defined action with an active JPA <code>EntityManager</code>.
         * Provider of the action does not need to care about activating or
         * closing the <code>EntityManager</code> or about the transaction
         * management.
         *
         * @param em JPA entity manager
         * @return a result object, or <code>null</code> if none
         */
        T doWithEntityManager(EntityManager em);
    }

    /** Suppresses default constructor, ensuring non-instantiability. */
    private JpaGaeUtils() {
    }

    /**
     * Gets cached singleton instance of <code>EntityManagerFactory</code>.
     *
     * @return cached singleton instance of <code>EntityManagerFactory</code>
     */
    public static EntityManagerFactory get() {
        return EMF_INSTANCE;
    }

    /**
     * Execute an action specified by the given callback object without transaction.
     *
     * @param callback callback object that specifies the action
     * @return an object returned by the action, or <code>null</code>
     * @param <T> data type handled by the action
     */
    public static <T> T execute(final JpaCallback<T> callback) {
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
    public static <T> T execute(final JpaCallback<T> callback, final boolean tx) {
        EntityManager em = null;
        EntityTransaction trx = null;
        T rslt = null;

        try {
            em = EMF_INSTANCE.createEntityManager();
            if (tx) {
                trx = em.getTransaction();
                trx.begin();
            }

            rslt  = callback.doWithEntityManager(em);

            if (tx) {
                trx.commit();
            }

        } catch (final Exception e) {
            LOG.log(Level.SEVERE, "failed to execute callback: " + e.getMessage(), e);
            if (null != trx) {
                trx.rollback();
            }
            throw new IllegalStateException("failed to execute callback: " + e.getMessage(), e);
        } finally {
            if (null != em) {
                em.close();
            }
        }
        return rslt;
    }

}
