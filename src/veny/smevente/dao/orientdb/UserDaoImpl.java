package veny.smevente.dao.orientdb;


import java.util.List;

import com.orientechnologies.orient.core.db.ODatabase;

import veny.smevente.dao.UserDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.User;

/**
 * OrientDB DAO implementation for <code>User</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
public class UserDaoImpl extends AbstractDaoOrientdb<User> implements UserDao {

    /**
     * Checks whether given username and password represents a user.
     * @param username user name
     * @param password password
     * @return <i>true</i> if the combination is valid
     */
    public boolean login(final String username, final String password) {
        final ODatabaseCallback<Integer> callback = new ODatabaseCallback<Integer>() {
            @Override
            public Integer doWithDatabase(final ODatabase db) {
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
