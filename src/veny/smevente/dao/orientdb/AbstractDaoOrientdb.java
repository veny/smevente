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

import com.orientechnologies.orient.core.id.ORID;
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
    @SuppressWarnings("unchecked")
    @Override
    public T getById(final Object id) throws ObjectNotFoundException {
        return databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final OObjectDatabaseTx db) {
                if (!(id instanceof ORID)) {
                    throw new IllegalArgumentException("ID has to be OrientDB RID");
                }
                try {
                    final AbstractEntity rslt = db.load((ORID) id);
                    // check if not deleted
                    assertNotSoftDeleted((T) rslt);
                    return (T) rslt;
                } catch (final Exception e) {
                    throw new ObjectNotFoundException(
                            "entity '" + getPersistentClass().getSimpleName() + "' not found, id=" + id, e);
                }
            }
        }, true);
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
            public List< T > doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql =
                        new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName());
                List<AbstractEntity> result;
                if (withDeleted) {
                    final OSQLSynchQuery<AbstractEntity> query = new OSQLSynchQuery<AbstractEntity>(sql.toString());
                    result = db.command(query).execute();
                } else {
                    result = executeWithSoftDelete(db, sql.toString(), null);
                }
                final List<T> rslt = new ArrayList<T>();
                for (AbstractEntity e : result) {
                    rslt.add((T) db.detach(e));
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
            public List< T > doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName())
                    .append(" WHERE ").append(paramName).append(" = :").append(paramName);

                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put(paramName, value);

                final List<T> rslt = new ArrayList<T>();
                final List<AbstractEntity> result = executeWithSoftDelete(db, sql.toString(), params);
                for (AbstractEntity e : result) {
                    rslt.add((T) db.detach(e));
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
            public List< T > doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ").append(getPersistentClass().getSimpleName())
                        .append(" WHERE ").append(paramName1).append(" = :").append(paramName1)
                        .append(" AND ").append(paramName2).append(" = :").append(paramName2);

                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put(paramName1, value1);
                params.put(paramName2, value2);

                final List<T> rslt = new ArrayList<T>();
                final List<AbstractEntity> result = executeWithSoftDelete(db, sql.toString(), params);
                for (AbstractEntity e : result) { rslt.add((T) db.detach(e)); }
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
    public T persist(final T entity) {
        return databaseWrapper.execute(new ODatabaseCallback<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T doWithDatabase(final OObjectDatabaseTx db) {
                final AbstractEntity rslt = db.save(entity);
                db.detach(rslt);

                if (null == ((AbstractEntity) entity).getId()) {
                    db.commit(); // to obtain RID, TODO [veny,A] other solution?
                }
                return (T) rslt;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final Object id) {
        databaseWrapper.execute(new ODatabaseCallback<T>() {
            @Override
            public T doWithDatabase(final OObjectDatabaseTx db) {
                if (null == id) {
                    throw new IllegalArgumentException("entity ID cannot be null");
                }

                final AbstractEntity entity = db.load((ORID) id);

                if (null != softDeleteAnnotation) {
                    entity.setDeleted(true);
                    db.save(entity);
                } else {
                    db.delete(entity);
                }
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
    protected List<AbstractEntity> executeWithSoftDelete(
            final OObjectDatabaseTx db, final String origSql, final Map<String, Object> origParams) {

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

        final OSQLSynchQuery<AbstractEntity> query = new OSQLSynchQuery<AbstractEntity>(sql.toString());
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
                throw new DeletedObjectException("found deleted entity, entity=" + entity);
            }
        }
    }

    // ----------------------------------------------------------- Helper Stuff

}
