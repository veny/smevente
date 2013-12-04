package veny.smevente.service;

import java.util.List;

import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Membership;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import eu.maydu.gwt.validation.client.ValidationException;

/**
 * User service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 12.7.2010
 */
public interface UserService {

    /**
     * Stores a given user into DB.
     *
     * @param username the user name
     * @param password the password
     * @param fullname full name
     * @param root flag of root user
     * @return created user
     * @deprecated
     */
    User createUser(String username, String password, String fullname, boolean root);

    /**
     * Stores a given user into DB.
     *
     * @param user the user to be created
     * @return created user
     */
    User createUser(User user);

    /**
     * Stores (creates or updates) a given user and also the related membership into DB.<p/>
     * The criterion to decide if create or update is entity's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param user user to be created
     * @param unitId ID of unit the user belongs to
     * @param role role of membership
     * @param significance significance of membership
     * @return created user
     */
    User storeUser(User user, Object unitId, final Membership.Role role, final Integer significance);

    /**
     * Gets user by given ID.
     *
     * @param id user ID to be returned
     * @return the found user
     * @throws ObjectNotFoundException if the ID doesn't exist
     */
    User getUser(Object id) throws ObjectNotFoundException;

    /**
     * Updates given user into DB.
     *
     * @param user user to be updated
     * @return updated user
     */
    User updateUser(User user);

//    /**
//     * Updates a given user into DB.
//     *
//     * @param user the user to be updated
//     * @param unitId the id of unit the user belongs into
//     * @param type the type of membership
//     * @param significance the significance of membership
//     */
//    void updateUser(final User user, Long unitId,
//            final Type type,
//            final Integer significance);

    /**
     * Deletes user.
     *
     * @param id user ID to be deleted
     */
    void deleteUser(Object id);

    /**
     * Finds user by given user name.
     * (used in authentication)
     *
     * @param username unique user name to find
     * @return found user
     */
    User findUserByUsername(String username);

    /**
     * Gets all memberships/users in given unit.
     *
     * @param unitId ID of unit the users must belong into
     * @return list of memberships with associated user (unit is <i>null</i>).
     */
    List<Membership> getUsersInUnit(Object unitId);

    /**
     * Changes password of given user.
     *
     * @param userId user ID
     * @param oldPassword the old password
     * @param newPassword the new password
     * @throws ValidationException if the old password doesn't match
     */
    void updateUserPassword(Object userId, String oldPassword, String newPassword) throws ValidationException;

    /**
     * Loads the user by user name and password.
     *
     * @param username user name of the user
     * @param password password of the user
     * @return user by user name and password or <i>null</i> if not found
     */
    User performLogin(String username, String password);

    /**
     * Encode the user password.
     * @param password user password
     * @return encoded user password
     */
    String encodePassword(final String password);

    /**
     * Loads all users.
     *
     * @return list of all users
     */
    @Deprecated // only for unit testing purposes
    List<User> getAllUsers();

    /**
     * Gets other users in given unit. Returns<ul>
     * <li>all users in unit where the specified user is ADMIN in, the given user is always on index 0
     * <li>only given user if not ADMIN
     * </ul>
     *
     * @param userId currently logged in user will be on the first place in list
     * @param unitId unit ID
     * @return list of users in unit
     */
    List<User> getOtherUsersInUnit(Object unitId, Object userId);

//    /**
//     * Gets units for given user.
//     *
//     * Business rules:<br>
//     * A user can see other users only if he has an ADMIN membership in the unit.<br>
//     * The currently logged in user is on the first place of a unit members.<br>
//     *
//     * @param userId user ID
//     * @return list of units where given user is member in
//     */
//    List<Unit> getUnitsOfUser(Object userId);

    // ------------------------------------------------------- Membership Stuff

    /**
     * Stores (creates or updates) a membership.<p/>
     *
     * @param unit the unit
     * @param user the user
     * @param role membership role
     * @param significance significance of the membership to other memberships of a user
     * @return found or created membership object
     */
    Membership storeMembership(Unit unit, User user, Membership.Role role, int significance);

    /**
     * Gets memberships for given user.
     *
     * @param userId ID of user
     * @return list of memberships for given user
     */
    List<Membership> getMembershipsByUser(Object userId);

}
