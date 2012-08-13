package veny.smevente.dao.jpa;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.orm.jpa.support.JpaDaoSupport;

import veny.smevente.dao.GenericDao;
import veny.smevente.dao.ObjectNotFoundException;

/**
 * Abstract class for most common DAO operations based on Spring Persistence Context.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 *
 * @param <T> the entity class
 *
 * {@link http://www.ctroller.com/contentRoller/view/16663/spring_300m3_on_google_appengine_with_jpa.html}
 * and
 * {@link http://www.dot4pro.com/google-app-engine/jpa-generic-dao-for-google-app-engine.html}
 * and
 * {@link https://jira.springsource.org/browse/SPR-6679}
 * +
 * {@link http://code.google.com/p/zhikebao-ecom/source/browse/trunk/zhikebao-service/src/
 *        main/java/com/xyz/framework/fix/PersistenceAnnotationBeanPostProcessor.java?spec=svn167&r=167}
 *
 * @deprecated use AbstractDaoJpaGae instead for GAE version
 */
public class AbstractDaoJpaSpring< T > implements GenericDao< T >, InitializingBean {

    /** The Spring <code>HibernateDaoSupport</code>. */
    private JpaDaoSupport jpaDaoSupport;

    /** EntityManager to be used by this DAO. */
    private EntityManager entityManager;

    /** Class of target entity. */
    private Class< T > persistentClass;

    /**
     * Constructor. Resolves actual type of persistent class.
     */
    @SuppressWarnings("unchecked")
    public AbstractDaoJpaSpring() {
        this.persistentClass =
            (Class< T >) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Set the JPA EntityManager to be used by this DAO.
     * @param entityManager EntityManager to be used
     */
    @PersistenceContext
    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
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
        jpaDaoSupport.getJpaTemplate().remove(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final Long entityId) {
        jpaDaoSupport.getJpaTemplate().remove(getById(entityId));
    }

    /** {@inheritDoc} */
    @Override
    public T getById(final long id) throws ObjectNotFoundException {
        T result = (T) jpaDaoSupport.getJpaTemplate().find(getPersistentClass(), id);

        if (result == null) {
            throw new ObjectNotFoundException(
                    "entity '" + getPersistentClass().getSimpleName() + "' not found, id=" + id);
        }

        return result;
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
        return jpaDaoSupport.getJpaTemplate().find("SELECT e FROM " + getPersistentClass().getName() + " e");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List< T > findBy(final String paramName, final Object value, final String orderBy) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(paramName, value);
        String sql = "SELECT e FROM " + getPersistentClass().getName() + " e WHERE e." + paramName + "=:" + paramName;
        if (null != orderBy) {
            sql += " ORDER BY " + orderBy;
        }
        return jpaDaoSupport.getJpaTemplate().findByNamedParams(sql, params);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List< T > findBy(
            final String paramName1, final Object value1,
            final String paramName2, final Object value2,
            final String orderBy) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(paramName1, value1);
        params.put(paramName2, value2);
        String sql = "SELECT e FROM " + getPersistentClass().getName() + " e WHERE e." + paramName1
                + "=:" + paramName1 + " AND e." + paramName2 + "=:" + paramName2;
        if (null != orderBy) {
            sql += " ORDER BY " + orderBy;
        }
        return jpaDaoSupport.getJpaTemplate().findByNamedParams(sql, params);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T getBy(final String paramName, final Object value) {
        JpaCallback<T> callback = new JpaCallback<T>() {
            @Override
            public T doInJpa(final EntityManager em) throws PersistenceException {
                Query query = em.createQuery(
                    "SELECT e FROM " + getPersistentClass().getName() + " e WHERE e." + paramName
                    + "=:" + paramName);
                query.setParameter(paramName, value);
                return (T) query.getSingleResult();
            }
        };
        return getJpaTemplate().execute(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void persist(final T entity) {
        jpaDaoSupport.getJpaTemplate().persist(entity);
    }

    /** {@inheritDoc} */
    @Override
    public T merge(final T entity) {
        jpaDaoSupport.getJpaTemplate().merge(entity);
        return entity;
    }

//    /** {@inheritDoc} */
//    @Override
//    public void flush() {
//        jpaDaoSupport.getJpaTemplate().flush();
//    }

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

    // ------------------------------------------------- InitializingBean Stuff

    /** {@inheritDoc} */
    @Override
    public void afterPropertiesSet() throws Exception {
        jpaDaoSupport = new HelperJpaDaoSupport();
        jpaDaoSupport.setEntityManager(entityManager);
    }


    // ----------------------------------------------------------- Helper Stuff

    /**
     * Gets a JPA template.
     * @return JPA template
     */
    protected JpaTemplate getJpaTemplate() {
        return jpaDaoSupport.getJpaTemplate();
    }

    /**
     * Just to be able to construct an instance of <code>JpaDaoSupport</code> which is abstract.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 0.1
     */
    public static class HelperJpaDaoSupport extends JpaDaoSupport {
    }

}
