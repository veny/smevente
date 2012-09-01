package veny.smevente.dao.orientdb;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.persistence.Column;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import veny.smevente.model.AbstractEntity;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

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
    private boolean init = false;
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

    public void setInit(final boolean init) {
        this.init = init;
        if (init) {
            final ODatabaseDocument db = get();


            // delete classes
            if (db.getMetadata().getSchema().existsClass("User")) {
                db.getMetadata().getSchema().dropClass("User");
            }
            if (db.getMetadata().getSchema().existsClass("AbstractEntity")) {
                db.getMetadata().getSchema().dropClass("AbstractEntity");
            }

            // AbstractEntity
            OClass entity = db.getMetadata().getSchema().createClass("AbstractEntity");
            entity.createProperty("deleted", OType.BOOLEAN).setMandatory(true);
            entity.createProperty("revision", OType.STRING);
            // User
            OClass user = db.getMetadata().getSchema().createClass("User", entity);
        }
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

    // --------------------------------------------------- DisposableBean Stuff

    /** {@inheritDoc} */
    @Override
    public void destroy() throws Exception {
        ODatabaseDocumentPool.global().close();
        LOG.info("Connection pool properly closed");
    }

    // ---------------------------------------- Document<->Entity Mapping Stuff

    public ODocument createDocument(Object entity) {
        final ODocument doc = new ODocument(entity.getClass().getSimpleName());

        final PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(entity);
        for (PropertyDescriptor pd : pds) {
            final Annotation col = pd.getReadMethod().getAnnotation(Column.class);
            if (null != col) {
                final String propName = pd.getName();
                try {
                    doc.field(propName, PropertyUtils.getProperty(entity, propName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return doc;
    }

    public AbstractEntity createValueObject(final ODocument doc, final Class clazz) {
        AbstractEntity rslt = null;
        try {
            rslt = (AbstractEntity) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // RID -> ID
        rslt.setId(doc.getIdentity().toString());
        // document version
        rslt.setVersion(Integer.toString(doc.getVersion()));

        final String[] fieldNames = doc.fieldNames();
        for (String fieldName : fieldNames) {
            try {
                PropertyUtils.setProperty(rslt, fieldName, doc.field(fieldName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rslt;
    }

}
