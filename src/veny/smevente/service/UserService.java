package veny.smevente.service;

import java.util.List;

import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Membership;
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
     * Stores a given user and also the related membership into DB.
     *
     * @param user user to be created
     * @param unitId ID of unit the user belongs to
     * @param role role of membership
     * @param significance significance of membership
     * @return created user
     */
    User createUser(User user, Object unitId, final Membership.Role role, final Integer significance);

    /**
     * Gets user by given ID.
     *
     * @param id user ID to be returned
     * @return the found user
     * @throws ObjectNotFoundException if the ID doesn't exist
     */
    User getUser(Object id) throws ObjectNotFoundException;

//    /**
//     * Updates a given user into DB.
//     *
//     * @param user the user to be updated
//     */
//    void updateUser(final User user);
//
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
     * @param id user ID to be deleted
     */
    void deleteUser(Object id);

    /**
     * Finds user by given user name.
     * @param username unique user name to find
     * @return found user
     */
    User findUserByUsername(String username);

//    /**
//     * Finds users according to given user name and/or full name for given unit.
//     * @param unitId the ID of unit the searched users must belong into
//     * @param userName the user name
//     * @param fullName full name
//     * @return list of found users
//     */
//    List<User> findUsers(Long unitId, String userName, String fullName);

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

////    /**
////     * Gets all users in given unit.
////     *
////     * Business rules:<br>
////     * A user can see other users only if he has an ADMIN membership in the unit.<br>
////     *
////     * @param unitId unit ID
////     * @param me currently logged in user will be on the first place in list
////     * @return list of users in unit, the logged in user is always on index 0
////     * AAA?
////     */
////    List<UserDto> getUsersByUnit(Long unitId, UserDto me);
//
//    /**
//     * Gets units and corresponding users in an unit for a given user.
//     *
//     * Business rules:<br>
//     * A user can see other users only if he has an ADMIN membership in the unit.<br>
//     * The currently logged in user is on the first place of a unit members.<br>
//     *
//     * @param user user
//     * @return list of memberships with units for given user
//     * @see #getUsersByUnit(Long, User)
//     */
//    List<UnitDto> getUnitsOfUser(User user);

    // ------------------------------------------------------- Membership Stuff

    /**
     * Adds user to unit.
     *
     * @param unitId ID of unit
     * @param userId ID of user
     * @param role membership role
     * @param significance significance of the membership to other memberships of a user
     */
    void createMembership(Object unitId, Object userId, Membership.Role role, int significance);

//    /**
//     * Finds memberships for given user.
//     * @param userId ID of user
//     * @return the list of memberships for given user
//     */
//    List<MembershipDto> findMembershipsByUser(Long userId);

}
