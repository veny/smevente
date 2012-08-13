package veny.smevente.dao.jpa.gae;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import veny.smevente.model.gae.User;
import veny.smevente.server.JpaGaeUtils;
import veny.smevente.server.JpaGaeUtils.JpaCallback;

/**
 * GAE JPA DAO implementation for <code>User</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.4.2010
 */
public class UserDaoGae extends AbstractDaoGae<User> {

    /**
     * Checks whether given username and password represents a user.
     * @param username user name
     * @param password password
     * @return <i>true</i> if the combination is valid
     */
    public boolean login(final String username, final String password) {
        final JpaCallback<Integer> callback = new JpaCallback<Integer>() {
            @Override
            public Integer doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT COUNT(e) FROM "
                        + getPersistentClass().getName()
                        + " e WHERE e.username=:username AND e.password=:password");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("username", username);
                query.setParameter("password", password);
                setSoftDeleteFilter(query);
                return (Integer) query.getSingleResult();
            }
        };

        return JpaGaeUtils.execute(callback).intValue() > 0;
    }

    /**
     * Finds user by given user name and password.
     * @param username user name
     * @param password password
     * @return found user or <i>null</i> if not found
     */
    public User findByUsernameAndPassword(final String username, final String password) {
        return JpaGaeUtils.execute(new JpaCallback<User>() {
            @SuppressWarnings("unchecked")
            @Override
            public User doWithEntityManager(final EntityManager em) {
                final StringBuilder sql = new StringBuilder("SELECT DISTINCT e FROM "
                        + getPersistentClass().getName()
                        + " e WHERE e.username=:username AND e.password=:password");
                appendSoftDeleteFilter(sql);
                final Query query = em.createQuery(sql.toString());
                query.setParameter("username", username);
                query.setParameter("password", password);
                setSoftDeleteFilter(query);
                List<User> users = (List<User>) query.getResultList();
                if (users.size() > 1) {
                    throw new IllegalStateException("expected max 1 user, but found " + users.size());
                }

                return users.isEmpty() ? null : (User) users.get(0);
            }
        });
    }

//    /**
//     * Marks given user as deleted (STATUS_DELETED).
//     * @param userId user ID
//     */
//    public void remove(final Long userId) {
//        JpaGaeUtils.execute(new JpaCallback<Object>() {
//            @Override
//            public Object doWithEntityManager(final EntityManager em) throws PersistenceException {
//                final User user = em.find(getPersistentClass(), userId);
//                user.setStatus(user.getStatus() | UserDto.STATUS_DELETED);
//                em.persist(user);
//                return null;
//            }
//        }, true);
//    }

    // ---------------------------------------------------------- Special Stuff
}
