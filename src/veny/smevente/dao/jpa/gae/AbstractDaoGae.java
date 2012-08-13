package veny.smevente.dao.jpa.gae;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

import veny.smevente.dao.DeletedObjectException;
import veny.smevente.dao.GenericDao;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.misc.SoftDelete;
import veny.smevente.server.JpaGaeUtils;
import veny.smevente.server.JpaGaeUtils.JpaCallback;

/**
 * Abstract class for most common DAO operations based on
 * <code>EMF</code> factory from GAE JPA tutorial.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 24.7.2010
 *
 * @param <T> the entity class
 *
 * {@link http://www.dot4pro.com/google-app-engine/jpa-generic-dao-for-google-app-engine.html}
 */
public class AbstractDaoGae< T > implements GenericDao< T > {

    /** Class of target entity. */
    private final Class< T > persistentClass;
    /** Life Cycle Annotation. */
    private final SoftDelete softDeleteAnnotation;
    /** Singleton of Apache Commons util class. */
    private final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    /**
     * Constructor. Resolves actual type of persistent class.
     */
    @SuppressWarnings("unchecked")
    public AbstractDaoGae() {
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

    /** {@inheritDoc} */
    @Override
    public void remove(final T entity) {
        JpaGaeUtils.execute(new JpaCallback<T>() {
            @Override
            public T doWithEntityManager(final EntityManager em) throws PersistenceException {
                removeHardOrSoft(entity, em);
                return null;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final Long entityId) {
        JpaGaeUtils.execute(new JpaCallback<T>() {
            @Override
            public T doWithEntityManager(final EntityManager em) throws PersistenceException {
                final T entity = em.find(getPersistentClass(), entityId);
                removeHardOrSoft(entity, em);
                return null;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @Override
    public T getById(final long id) throws ObjectNotFoundException {
        final T rslt = JpaGaeUtils.execute(new JpaCallback<T>() {
            @Override
            public T doWithEntityManager(final EntityManager em) {
                return em.find(getPersistentClass(), id);
            }
        }, false);

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
        return JpaGaeUtils.execute(new JpaCallback<List< T >>() {
            @Override
            public List< T > doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM " + getPersistentClass().getName() + " e");
                if (!withDeleted) { setSoftDeleteFilter(sql); }

                final Query q = em.createQuery(sql.toString());
                if (!withDeleted) { setSoftDeleteFilter(q); }
                final List<T> rslt = q.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List< T > findBy(final String paramName, final Object value, final String orderBy) {
        return JpaGaeUtils.execute(new JpaCallback<List< T >>() {
            @Override
            public List< T > doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.").append(paramName).append("=:").append(paramName);

                appendSoftDeleteFilter(sql);
                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Query q = em.createQuery(sql.toString());
                q.setParameter(paramName, value);
                setSoftDeleteFilter(q);
                final List< T > rslt = q.getResultList();

                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();

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

        return JpaGaeUtils.execute(new JpaCallback<List< T >>() {
            @Override
            public List< T > doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.").append(paramName1).append("=:").append(paramName1)
                    .append(" AND e.").append(paramName2).append("=:").append(paramName2);

                appendSoftDeleteFilter(sql);
                if (null != orderBy) { sql.append(" ORDER BY ").append(orderBy); }

                final Query q = em.createQuery(sql.toString());
                q.setParameter(paramName1, value1);
                q.setParameter(paramName2, value2);
                setSoftDeleteFilter(q);
                final List< T > rslt = q.getResultList();
                // load entities to eliminate 'Object Manager has been closed' exception
                rslt.size();
                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T getBy(final String paramName, final Object value) {
        return JpaGaeUtils.execute(new JpaCallback< T >() {
            @Override
            public T doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT e FROM ").append(getPersistentClass().getName())
                    .append(" e WHERE e.").append(paramName).append("=:").append(paramName);
                final Query q = em.createQuery(sql.toString());
                q.setParameter(paramName, value);
                final T rslt = (T) q.getSingleResult();
                // check if not deleted
                assertNotSoftDeleted(rslt);

                return rslt;
            }
        }, false);
    }

    /** {@inheritDoc} */
    @Override
    public void persist(final T entity) {
        JpaGaeUtils.execute(new JpaCallback<T>() {
            @Override
            public T doWithEntityManager(final EntityManager em) throws PersistenceException {
                em.persist(entity);
                return null;
            }
        }, true);
    }

    /** {@inheritDoc} */
    @Override
    public T merge(final T entity) {
        return JpaGaeUtils.execute(new JpaCallback<T>() {
            @Override
            public T doWithEntityManager(final EntityManager em) throws PersistenceException {
                return em.merge(entity);
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
     * Appends a SQL suffix that filters deleted entities.
     * @param query query to be extended
     */
    protected void appendSoftDeleteFilter(final StringBuilder query) {
        if (null != softDeleteAnnotation) {
            query.append(" AND e.").append(softDeleteAnnotation.attribute()).append("=:sd");
        }
    }
    /**
     * Sets a SQL suffix that filters deleted entities. Should be used
     * in cases where no other select criteria is used, so the WHERE
     * clause is completely missing.
     * @param query query to be extended
     */
    protected void setSoftDeleteFilter(final StringBuilder query) {
        if (null != softDeleteAnnotation) {
            query.append(" WHERE e.").append(softDeleteAnnotation.attribute()).append("=:sd");
        }
    }
    /**
     * Set a query parameter to filter the deleted entities.
     * @param query query to set the value
     */
    protected void setSoftDeleteFilter(final Query query) {
        if (null != softDeleteAnnotation) {
            query.setParameter("sd", Boolean.FALSE);
        }
    }

    /**
     * Removes a given entity from storage hard (physical remove)
     * or soft (set flag).
     * @param entity the entity to remove
     * @param em the controlling entity manager
     * @return <i>true</i> by hard delete
     */
    private boolean removeHardOrSoft(final T entity, final EntityManager em) {
        if (null == softDeleteAnnotation) {
            em.remove(entity);
            return Boolean.TRUE;
        } else {
            try {
                BeanUtils.setProperty(entity, softDeleteAnnotation.attribute(), Boolean.TRUE);
            } catch (Exception e) {
                throw new IllegalStateException("failed to soft remove an entity: " + entity, e);
            }
            em.persist(entity);
            return Boolean.FALSE;
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
                throw new IllegalStateException("failed to read a 'sof delete' attribute, entity=" + entity, e);
            }
            if (null != softDeleteValue && softDeleteValue.booleanValue()) {
                throw new DeletedObjectException("found deleted entity, entity=" + entity);
            }
        }
    }

    // ----------------------------------------------------------- Helper Stuff

}
