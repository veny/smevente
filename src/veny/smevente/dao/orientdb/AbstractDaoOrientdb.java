package veny.smevente.dao.orientdb;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;

import veny.smevente.dao.DeletedObjectException;
import veny.smevente.dao.GenericDao;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.misc.SoftDelete;
import veny.smevente.model.AbstractEntity;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * Abstract class for most common DAO operations based on <code>OrientDB</code> engine.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 17.8.2012
 *
 * @param <T> the entity class
 */
public abstract class AbstractDaoOrientdb< T > implements GenericDao< T > {

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

    /** {@inheritDoc} */
    @Override
    public void remove(final T entity) {
        final String id = ((AbstractEntity) entity).getId();
        remove(id);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final String entityId) {
        if (Strings.isNullOrEmpty(entityId)) {
            throw new IllegalArgumentException("entity ID cannot be blank");
        }

        databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final ODatabaseDocument db) {
                final ODocument doc = db.getRecord(new ORecordId(entityId));

                if (null != softDeleteAnnotation) {
                    doc.field(softDeleteAnnotation.attribute(), Boolean.TRUE);
                    doc.save();
                } else {
                    doc.delete();
                }
                return null;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T getById(final String id) throws ObjectNotFoundException {
        final T rslt;

        try {
            ODocument doc = databaseWrapper.get().getRecord(new ORecordId(id));
            rslt = (T) databaseWrapper.createValueObject(doc, getPersistentClass());
        } catch (IllegalArgumentException e) { // from 'new ORecordId'
            throw new ObjectNotFoundException("failed to find entity'"
                     + getPersistentClass().getSimpleName() + "' not found, id=" + id, e);
        }

        if (null == rslt) {
            throw new ObjectNotFoundException(
                    "entity '" + getPersistentClass().getSimpleName() + "' not found, id=" + id);
        }
        // check if not deleted
        assertNotSoftDeleted(rslt);

        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    public List< T > getAll() {
        return getAll(false);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List< T > getAll(final boolean withDeleted) {
        return databaseWrapper.execute(new ODatabaseCallback<List< T >>() {
            @Override
            public List< T > doWithDatabase(final ODatabaseDocument db) {
                final StringBuilder sql =
                        new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName());
                List<ODocument> result;
                if (withDeleted) {
                    final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(sql.toString());
                    result = db.command(query).execute();
                } else {
                    result = executeWithSoftDelete(db, sql.toString(), null);
                }
                final List<T> rslt = new ArrayList<T>();
                for (ODocument doc : result) {
                    rslt.add((T) databaseWrapper.createValueObject(doc, getPersistentClass()));
                }
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List< T > findBy(final String paramName, final Object value, final String orderBy) {
        return databaseWrapper.execute(new ODatabaseCallback<List< T >>() {
            @Override
            public List< T > doWithDatabase(final ODatabaseDocument db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName())
                    .append(" WHERE ").append(paramName).append(" = :").append(paramName);

                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put(paramName, value);

                final List<T> rslt = new ArrayList<T>();
                final List<ODocument> result = executeWithSoftDelete(db, sql.toString(), params);
                for (ODocument doc : result) {
                    rslt.add((T) databaseWrapper.createValueObject(doc, getPersistentClass()));
                }
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List< T > findBy(
            final String paramName1, final Object value1,
            final String paramName2, final Object value2,
            final String orderBy) {

        return databaseWrapper.execute(new ODatabaseCallback<List< T >>() {
            @Override
            public List< T > doWithDatabase(final ODatabaseDocument db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName())
                        .append(" WHERE ").append(paramName1).append(" = :").append(paramName1)
                        .append(" AND ").append(paramName2).append(" = :").append(paramName2);

                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put(paramName1, value1);
                params.put(paramName2, value2);

                final List<T> rslt = new ArrayList<T>();
                final List<ODocument> result = executeWithSoftDelete(db, sql.toString(), params);
                for (ODocument doc : result) {
                    rslt.add((T) databaseWrapper.createValueObject(doc, getPersistentClass()));
                }
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public T getBy(final String paramName, final Object value) {
        return databaseWrapper.execute(new ODatabaseCallback< T >() {
            @Override
            public T doWithDatabase(final ODatabaseDocument db) {
//                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
//                    .append(" e WHERE e.").append(paramName).append("=:").append(paramName);
//                final Query q = em.createQuery(sql.toString());
//                q.setParameter(paramName, value);
//                final T rslt = (T) q.getSingleResult();
//                // check if not deleted
//                assertNotSoftDeleted(rslt);
//
//                return rslt;
                return null;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public void persist(final T entity) {
        databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final ODatabaseDocument db) {
                final ODocument doc = databaseWrapper.createDocument((AbstractEntity) entity);
                doc.save();

                if (null == ((AbstractEntity) entity).getId()) {
                    db.commit(); // to obtain RID, TODO [veny,A] other solution?
                    ((AbstractEntity) entity).setId(doc.getIdentity().toString());
                }
                return null;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @Override
    public T merge(final T entity) {
        return databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final ODatabaseDocument db) {
//                return em.merge(entity);
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


    // ------------------------------------------------------ Soft Delete Stuff


    /**
     * Decorates a SQL query with a extension of WHERE clause to eliminate soft deleted entities.
     * @param db database
     * @param origSql original SQL
     * @param origParams original query paramaters
     * @return result set from database
     */
    protected List<ODocument> executeWithSoftDelete(
            final ODatabaseDocument db, final String origSql, final Map<String, Object> origParams) {

        StringBuffer sql = new StringBuffer(origSql);
        Map<String, Object> params = origParams;

        if (null != softDeleteAnnotation) {
            if (sql.indexOf(" WHERE ") > 0 || sql.indexOf(" where ") > 0) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE "); // TODO [veny,A] ORDER BY problem
            }
            sql.append(softDeleteAnnotation.attribute()).append(" = :softDelete");
            if (null == params) { params = new HashMap<String, Object>(); }
            params.put("softDelete", Boolean.FALSE);
        }

        final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(sql.toString());
        if (null == params) {
            return db.command(query).execute();
        } else {
            return db.command(query).execute(params);
        }
    }

//    /**
//     * Appends a SQL suffix that filters deleted entities.
//     * @param query query to be extended
//     */
//    protected void appendSoftDeleteFilter(final StringBuilder query) {
//        if (null != softDeleteAnnotation) {
//            query.append(" AND e.").append(softDeleteAnnotation.attribute()).append("=:sd");
//        }
//    }
//    /**
//     * Sets a SQL suffix that filters deleted entities. Should be used
//     * in cases where no other select criteria is used, so the WHERE
//     * clause is completely missing.
//     * @param query query to be extended
//     */
//    protected void setSoftDeleteFilter(final StringBuilder query) {
//        if (null != softDeleteAnnotation) {
//            query.append(" WHERE ").append(softDeleteAnnotation.attribute()).append(" = :sd");
//        }
//    }
//    /**
//     * Set a query parameter to filter the deleted entities.
//     * @param params query parameters to be extended
//     */
//    protected void setSoftDeleteFilter(final Map<String, Object> params) {
//        if (null != softDeleteAnnotation) {
//            params.put("sd", Boolean.FALSE);
//        }
//    }

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
                throw new DeletedObjectException("found deleted entity, entity=" + entity);
            }
        }
    }

    // ----------------------------------------------------------- Helper Stuff

}
