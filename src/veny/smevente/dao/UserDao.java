package veny.smevente.dao;

import java.util.List;

import veny.smevente.model.User;

/**
 * Interface for persistence operation with <code>User</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.8.2012
 */
public interface UserDao extends GenericDao<User> {

    /**
     * Finds user by given user name and password.
     *
     * @param username user name
     * @param password password
     * @return found user or <i>null</i> if not found
     */
    User findByUsernameAndPassword(String username, String password);

    /**
     * Gets all users in given unit.
     *
     * @param unitId ID of unit the users must belong into
     * @return list of users
     */
    List<User> getUsersInUnit(Object unitId);

}

