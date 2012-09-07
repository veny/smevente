package veny.smevente.dao.orientdb;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.dao.UserDao;
import veny.smevente.dao.orientdb.DatabaseWrapper.ODatabaseCallback;
import veny.smevente.model.User;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * OrientDB DAO implementation for <code>User</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
public class UserDaoImpl extends AbstractDaoOrientdb<User> implements UserDao {

//    /** {@inheritDoc} */
//    public boolean login(final String username, final String password) {
//        final ODatabaseCallback<Integer> callback = new ODatabaseCallback<Integer>() {
//            @Override
//            public Integer doWithDatabase(final OObjectDatabaseTx db) {
//                final StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ")
//                        .append(getPersistentClass().getName())
//                        .append(" WHERE username = :username AND password = :password");
//
//                final Map<String, Object> params = new HashMap<String, Object>();
//                params.put("username", username);
//                params.put("password", password);
//
//                //setSoftDeleteFilter(query);
//                final List<AbstractEntity> result = executeWithSoftDelete(db, sql.toString(), params);
////                return (Integer) result.get(0).field("count");
//                return 0;
//            }
//        };
//
//        return getDatabaseWrapper().execute(callback).intValue() > 0;
//    }

    /** {@inheritDoc} */
    public User findByUsernameAndPassword(final String username, final String password) {
        return getDatabaseWrapper().execute(new ODatabaseCallback<User>() {
            @Override
            public User doWithDatabase(final OObjectDatabaseTx db) {
                final StringBuilder sql = new StringBuilder("SELECT FROM ")
                        .append(getPersistentClass().getSimpleName())
                        .append(" WHERE username = :username AND password = :password");

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("username", username);
                params.put("password", password);

                final List<User> users = executeWithSoftDelete(db, sql.toString(), params, true);
                if (users.size() > 1) {
                    throw new IllegalStateException("expected max 1 user, but found " + users.size());
                }

                return users.isEmpty() ? null : (User) db.detach(users.get(0));
            }
        });
    }

    // ---------------------------------------------------------- Special Stuff

}
