package veny.smevente.dao;

import veny.smevente.model.User;

/**
 * Interface for persistence operation withs <code>User</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
public interface UserDao extends GenericDao<User> {

    /**
     * Checks whether given username and password represents a user.
     *
     * @param username user name
     * @param password password
     * @return <i>true</i> if the combination is valid
     */
    boolean login(String username, String password);

    /**
     * Finds user by given user name and password.
     *
     * @param username user name
     * @param password password
     * @return found user or <i>null</i> if not found
     */
    User findByUsernameAndPassword(String username, String password);

}

