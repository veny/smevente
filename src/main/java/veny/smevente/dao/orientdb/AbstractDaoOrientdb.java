package veny.smevente.dao.orientdb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;

import veny.smevente.dao.DeletedObjectException;
import veny.smevente.dao.GenericDao;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.misc.SoftDelete;
import veny.smevente.model.AbstractEntity;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * Abstract class for most common DAO operations based on <code>OrientDB</code> engine.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 17.8.2012
 *
 * @param <T> the entity class
 */
public abstract class AbstractDaoOrientdb< T extends AbstractEntity > implements GenericDao< T > {

    /** Class of target entity. */
    private final Class< T > persistentClass;
    /** Life Cycle Annotation. */
    private final SoftDelete softDeleteAnnotation;
    /** Singleton of Apache Commons util class. */
    private final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    /** Dependency. */
    @Autowired
    private DatabaseWrapper databaseWrapper;

    /**
     * Constructor. Resolves actual type of persistent class.
     */
    @SuppressWarnings("unchecked")
    public AbstractDaoOrientdb() {
        this.persistentClass =
            (Class< T >) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.softDeleteAnnotation = persistentClass.getAnnotation(SoftDelete.class);
    }

    /**
     * Getter for actual class.
     *
     * @return actual class for this instance
     */
    public Class< T > getPersistentClass() {
        return persistentClass;
    }

    /**
     * Getter for the database wrapper.
     *
     * @return the database wrapper
     * @see DatabaseWrapper
     */
    public DatabaseWrapper getDatabaseWrapper() {
        return databaseWrapper;
    }


    // ----------------------------------------------------- GenericDao Methods

    /** {@inheritDoc} */
    @Override
    public T getById(final Object id) throws ObjectNotFoundException {
        return databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override public T doWithDatabase(final OObjectDatabaseTx db) {
                if (null == id) { throw new NullPointerException("ID cannot be null"); }
                final ORID rid;
                if ((id instanceof ORID)) { rid = (ORID) id; } else {
                    try { rid = new ORecordId(id.toString()); } catch (Exception e) {
                        throw new ObjectNotFoundException("ID has to be OrientDB RID, id=" + id.toString());
                    }
                }
                final T rslt;
                try { rslt = db.load(rid); } catch (final Exception e) {
                    throw new ObjectNotFoundException(
                            "entity '" + getPersistentClass().getSimpleName() + "' not found, id=" + id, e);
                }

                assertNotSoftDeleted(rslt);
                detachWithFirstLevelAssociations(rslt, db);
                return (T) rslt;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @Override
    public List< T > getAll() {
        return getAll(false);
    }

    /** {@inheritDoc} */
    @Override
    public List< T > getAll(final boolean withDeleted) {
        return databaseWrapper.execute(new ODatabaseCallback<List< T >>() {
            @Override
            public List< T > doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql =
                        new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName());
                final List<T> rslt = executeWithSoftDelete(db, sql.toString(), null, !withDeleted);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public List< T > findBy(final String paramName, final Object value, final String orderBy) {
        return databaseWrapper.execute(new ODatabaseCallback<List< T >>() {
            @Override
            public List< T > doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName())
                    .append(" WHERE ").append(paramName).append(" = :").append(paramName);

                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put(paramName, value);

                final List<T> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public List< T > findBy(
            final String paramName1, final Object value1,
            final String paramName2, final Object value2,
            final String orderBy) {

        return databaseWrapper.execute(new ODatabaseCallback<List< T >>() {
            @Override
            public List< T > doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName())
                        .append(" WHERE ").append(paramName1).append(" = :").append(paramName1)
                        .append(" AND ").append(paramName2).append(" = :").append(paramName2);

                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put(paramName1, value1);
                params.put(paramName2, value2);

                final List<T> rslt = executeWithSoftDelete(db, sql.toString(), params, true);
                detachWithFirstLevelAssociations(rslt, db);
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public T getBy(final String paramName, final Object value) {
        return databaseWrapper.execute(new ODatabaseCallback< T >() {
            @Override
            public T doWithDatabase(final OObjectDatabaseTx db) {
                throw new IllegalStateException("not implemented yet");
//                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
//                    .append(" e WHERE e.").append(paramName).append("=:").append(paramName);
//                final Query q = em.createQuery(sql.toString());
//                q.setParameter(paramName, value);
//                final T rslt = (T) q.getSingleResult();
//                // check if not deleted
//                assertNotSoftDeleted(rslt);
//
//                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public T persist(final T entity) {
        final T r = databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final OObjectDatabaseTx db) {
                db.attach(entity); // has to be attached, it's maybe detached by previous operation
                final T rslt = db.save(entity);
                return rslt;
            }
        }, true);
        // detach:
        // 1) has to be after commit, otherwise ID is temporary like '#9:-2'
        // 2) otherwise are all properties 'null'
        databaseWrapper.get().detach(r);
        return r;
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final Object id) {
        databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final OObjectDatabaseTx db) {
                if (null == id) { throw new IllegalArgumentException("entity ID cannot be null"); }
                final ORID rid;
                if ((id instanceof ORID)) { rid = (ORID) id; } else {
                    try { rid = new ORecordId(id.toString()); } catch (Exception e) {
                        throw new IllegalArgumentException("ID has to be OrientDB RID");
                    }
                }
                final T entity = db.load(rid);
                if (null != softDeleteAnnotation) {
                    db.detach(entity); // entity has to be detached, otherwise are all properties 'null'
                    entity.setDeleted(true);
                    db.attach(entity); // all data contained in the object will be copied in the associated document
                    db.save(entity);
                } else { db.delete(entity); }
                return null;
            }
        }, true);
    }

//    /** {@inheritDoc} */
//    @SuppressWarnings("unchecked")
//    public Integer count() {
//        HibernateCallback< Integer > callback = new HibernateCallback< Integer >() {
//            @Override
//            public Integer doInHibernate(final Session session) throws HibernateException, SQLException {
//                final Criteria crit = session.createCriteria(getPersistentClass());
//                crit.setProjection(Projections.rowCount());
//                List result = crit.list();
//                return (Integer) result.get(0);
//            }
//        };
//
//        return getHibernateTemplate().execute(callback);
//    }


    // --------------------------------------------------------- OrientDB Stuff


    /**
     * Detach an entity and first level associations labeled with JPA annotations.
     * @param entity entity to detach
     * @param db object database
     */
    protected void detachWithFirstLevelAssociations(final T entity, final OObjectDatabaseTx db) {
        if (null == entity) { throw new NullPointerException("entity is null"); }
        final List<Method> assocMethods = getAssociationGetters();

        db.detach(entity);
        // detach aggregated fields too
        for (Method m : assocMethods) {
            try {
                final Object toBeDetached = m.invoke(entity);
                if (null != toBeDetached) { // BF#15
                    db.detach(toBeDetached);
                }
            } catch (Exception e) {
                throw new IllegalStateException("failed to detach associated field, method="
                        + m.getName() + ", entity=" + entity, e);
            }
        }
    }

    /**
     * Detaches list of given objects and on each object detach aggregated entity too.
     * @param list objects to detach
     * @param db object database
     */
    protected void detachWithFirstLevelAssociations(final List<T> list, final OObjectDatabaseTx db) {
        if (null == list || list.isEmpty()) { return; }
        final List<Method> assocMethods = getAssociationGetters();

        // detach entries in list
        for (T entity : list) {
            db.detach(entity);
            // detach aggregated fields too
            for (Method m : assocMethods) {
                try {
                    final Object toBeDetached = m.invoke(entity);
                    if (null != toBeDetached) { // BF#15
                        db.detach(toBeDetached);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("failed to detach associated field", e);
                }
            }
        }
    }

    /**
     * Gets list of 'getters' of fields labeled with JPA annotation representing an association.
     * @return list of getters
     */
    private List<Method> getAssociationGetters() {
        final Field[] fields = persistentClass.getDeclaredFields();
        final List<Method> rslt = new ArrayList<Method>();

        for (Field f : fields) {
            if (null != f.getAnnotation(OneToOne.class)
                    || null != f.getAnnotation(OneToMany.class)
                    || null != f.getAnnotation(ManyToOne.class)) {
                final String methodName = "get" + WordUtils.capitalize(f.getName());
                try {
                    rslt.add(persistentClass.getMethod(methodName));
                } catch (Exception e) {
                    throw new IllegalStateException("failed to detach associated field", e);
                }
            }
        }

        return rslt;
    }

    // ------------------------------------------------------ Soft Delete Stuff


    /**
     * Decorates a SQL query with a extension of WHERE clause to eliminate soft deleted entities.
     * @param db database
     * @param origSql original SQL
     * @param origParams original query parameters
     * @param apply flag whether the 'soft delete' filter should be applied
     * @return result set from database
     */
    protected List<T> executeWithSoftDelete(
            final OObjectDatabaseTx db, final String origSql,
            final Map<String, Object> origParams, final boolean apply) {

        final StringBuffer sql = new StringBuffer(origSql);
        Map<String, Object> params = origParams;

        if (null != softDeleteAnnotation && apply) {
            final StringBuffer add = new StringBuffer();
            if (sql.indexOf(" WHERE ") > 0 || sql.indexOf(" where ") > 0) {
                add.append(" AND ");
            } else {
                add.append(" WHERE ");
            }
            add.append('(').append(softDeleteAnnotation.attribute()).append(" = :softDelete")
                .append(" OR ").append(softDeleteAnnotation.attribute()).append(" IS NULL)");
            int big = sql.indexOf(" ORDER BY ");
            int small = sql.indexOf(" order by ");

            if (big > 0 || small > 0) {
                sql.insert(big > 0 ? big : small, add);
            } else {
                sql.append(add);
            }

            if (null == params) { params = new HashMap<String, Object>(); }
            params.put("softDelete", Boolean.FALSE);
        }

        final OSQLSynchQuery<T> query = new OSQLSynchQuery<T>(sql.toString());
        if (null == params) {
            return db.command(query).execute();
        } else {
            return db.command(query).execute(params);
        }
    }

    /**
     * Asserts that a given entity is not deleted
     * by reading the attribute that controls a live cycle of the entity.
     * @param entity entity to read the life cycle control attribute
     */
    private void assertNotSoftDeleted(final T entity) {
        if (null != softDeleteAnnotation) {
            final Boolean softDeleteValue;
            try {
                softDeleteValue =
                    (Boolean) propertyUtilsBean.getProperty(entity, softDeleteAnnotation.attribute());
            } catch (Exception e) {
                throw new IllegalStateException("failed to read a 'softDelete' attribute, entity=" + entity, e);
            }
            if (null != softDeleteValue && softDeleteValue.booleanValue()) {
                throw new DeletedObjectException("found deleted entity, entity=" + entity.getId());
            }
        }
    }

    // ----------------------------------------------------------- Helper Stuff

}
