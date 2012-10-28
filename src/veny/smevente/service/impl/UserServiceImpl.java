package veny.smevente.service.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.dao.MembershipDao;
import veny.smevente.dao.UnitDao;
import veny.smevente.dao.UserDao;
import veny.smevente.model.Membership;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.service.UserService;

import com.google.common.base.Strings;

import eu.maydu.gwt.validation.client.server.ServerValidation;


/**
 * Implementation of User service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 12.7.2012
 */
public class UserServiceImpl implements UserService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UserDao userDao;
    /** Dependency. */
    @Autowired
    private MembershipDao membershipDao;
    /** Dependency. */
    @Autowired
    private UnitDao unitDao;

    /** Message Digest to generate password hash. */
    private final MessageDigest md;

    /** Constructor. */
    public UserServiceImpl() {
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("failed to initialize message digest", e);
        }
    }

    /** {@inheritDoc} */
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    @Override
    public User createUser(
            final String username, final String password,
            final String fullname, final boolean root) {
        final User user = new User();
        user.setUsername(username);
        user.setPassword(encodePassword(password));
        user.setFullname(fullname);
        user.setRoot(root);
        return createUser(user);
    }

    /** {@inheritDoc} */
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    @Override
    public User createUser(final User user) {
        validateUser(user, true);

        final List<User> check = userDao.findBy("username", user.getUsername(), null);
        if (!check.isEmpty()) {
            ServerValidation.exception("duplicateValue", "username", (Object[]) null);
        }

        final User rslt = userDao.persist(user);

        LOG.info("created new user, " + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @PreAuthorize("hasPermission(#unitId, 'V_UNIT_ADMIN')")
    @Override
    public User createUser(
            final User user, final Object unitId, final Membership.Role role, final Integer significance) {

        if (significance < 0) {
            throw new IllegalArgumentException("invalid value of significance: " + significance);
        }

        // find unit
        final Unit unit = unitDao.getById(unitId);

        // create user
        final User rslt = createUser(user);

        // create membership
        final Membership memb = new Membership();
        memb.setRole(role.toString());
        memb.setSignificance(significance);
        memb.setUser(rslt);
        memb.setUnit(unit);
        membershipDao.persist(memb);

        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    @Override
    public User getUser(final Object id) {
        final User rslt = userDao.getById(id);
        LOG.info("found user by id=" + id);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    // TODO [veny,B] think of authorization
    @Override
    public void updateUser(final User user) {
        validateUser(user, false);

        // unique user name
        final List<User> check = userDao.findBy("username", user.getUsername(), null);
        if (null != check && !check.isEmpty() && !check.get(0).getId().equals(user.getId())) {
            ServerValidation.exception("duplicateValue", "username", (Object[]) null);
        } else {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("duplicite user name check OK, userName=" + user.getUsername());
            }
        }

        userDao.persist(user);
        LOG.info("user updated, id=" + user.getId()
                + ", username=" + user.getUsername() + ", fullname=" + user.getFullname());
    }

//    /** {@inheritDoc} */
//    @Transactional
//    @PreAuthorize("hasPermission(#unitId, 'V_UNIT_ADMIN')")
//    @Override
//    public void updateUser(final User user,
//            final Long unitId,
//            final MembershipDto.Type type,
//            final Integer significance) {
//        if (significance < 0) {
//            throw new IllegalArgumentException("invalid value of significance: " + significance);
//        }
//        updateUser(user);
//        updateMembershipWithReorg(unitId, user.getId(), type, significance);
//    }

    /** {@inheritDoc} */
    @Transactional
    // TODO [veny,B] think of authorization
    @Override
    public void deleteUser(final Object id) {
        final List<Membership> memberships = getMembershipsByUser(id);
        if (memberships != null) {
            for (Membership membership: memberships) {
                membershipDao.remove(membership.getId());
                LOG.info("membership (for user with id=" + id + ") deleted, id=" + membership.getId());
            }
        }

        userDao.remove(id);
        LOG.info("user deleted, id=" + id + ", memberships=" + memberships.size());
    }

    // no authorization - used in AuthenticationProvider
    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    public User findUserByUsername(final String username) {
        if (Strings.isNullOrEmpty(username)) { throw new NullPointerException("username cannot be blank"); }

        final List<User> found = userDao.findBy("username", username, null);
        if (found.size() > 1) {
            throw new IllegalStateException("duplicate username found (has to be unique!), username=" + username);
        }

        final User rslt;
        if (0 == found.size()) {
            rslt = null;
        } else {
            rslt = found.get(0);
        }

        LOG.info("found user by username, " + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    @Override
    public List<Membership> getUsersInUnit(final Object unitId) {
        if (null == unitId) { throw new IllegalArgumentException("unit id cannot be null"); }

        LOG.info("getUsersInUnit, unitId=" + unitId);

        final List<Membership> rslt = membershipDao.findBy("unit", unitId, null);
        for (Membership memb : rslt) {
            memb.setUnit(null);
        }

        LOG.info("returned users, size=" + rslt.size());
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @PreAuthorize("hasPermission(#userId, 'V_MY_USER')")
    @Override
    public void updateUserPassword(final Object userId, final String oldPassword, final String newPassword) {
        if (null == userId) { throw new NullPointerException("user ID cannot be null"); }
        if (Strings.isNullOrEmpty(oldPassword)) { throw new NullPointerException("old password cannot be blank"); }
        if (Strings.isNullOrEmpty(newPassword)) { throw new NullPointerException("new password cannot be blank"); }

        // check the old password
        final User user = userDao.getById(userId);
        if (!user.getPassword().equals(encodePassword(oldPassword))) {
            LOG.warning("trying to change password, the old one is bad, userId=" + userId);
            ServerValidation.exception("validationOldPasswordBad", "old", (Object[]) null);
        }

        user.setPassword(encodePassword(newPassword));
        userDao.persist(user);
        LOG.info("changed password, userId=" + userId);
    }

    // no authorization - used in AuthenticationProvider
    /** {@inheritDoc} */
    @Transactional
    @Override
    public User performLogin(final String username, final String password) {
        if (Strings.isNullOrEmpty(username)) { throw new NullPointerException("username cannot be blank"); }
        if (Strings.isNullOrEmpty(password)) { throw new NullPointerException("password cannot be blank"); }

        final User user = userDao.findByUsernameAndPassword(username, encodePassword(password));

        if (null == user) {
            LOG.info("user not found by username & password, username=" + username);
            return null;
        }

        // update last logged in timestamp
        user.setLastLoggedIn(new Date());
        userDao.persist(user);

        LOG.info("found user by username & password, " + user);
        return user;
    }

    // no authorization
    /** {@inheritDoc} */
    @Override
    public String encodePassword(final String password) {
        try {
            md.update(password.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.severe("failed to convert String to byte[]");
            throw new IllegalStateException("failed to convert String to byte[]", e);
        }
        final byte[] hash = md.digest();

        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }

        final StringBuffer rslt = new StringBuffer(md.getAlgorithm()).append(':').append(formatter.toString());
        return rslt.toString();
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    @Override
    public List<User> getAllUsers() {
        final List<User> found = userDao.getAll();
        LOG.info("found all users, size=" + found.size());
        return found;
    }

    /** {@inheritDoc} */
    // TODO [veny,B] think of authorization
    @Transactional(readOnly = true)
    @Override
    public List<User> getOtherUsersInUnit(final Object unitId, final Object userId) {
        final List<User> rslt = new ArrayList<User>();
        // load the user again <- 'userId' is probably from session
        // and insert it into result list
        final User mainUser = userDao.getById(userId);
        rslt.add(mainUser);

        // load user's Membership
        final List<Membership> membs = membershipDao.findBy("unit", unitId, "user", userId, null);
        if (1 != membs.size()) {
            throw new IllegalStateException("invalid amount of membership in unit, userId="
                    + userId + ", unitId=" + unitId + ", amount=" + membs.size());
        }
        final Membership memb = membs.get(0);

        // no Admin -> return only the user
        if (Membership.Role.ADMIN.equals(memb.enumRole())) {
            // find other users
            final List<Membership> other = membershipDao.findBy("unit", unitId, null);
            for (Membership m : other) {
                if (!mainUser.getId().equals(m.getUser().getId())) { // don't include 'me' again
                    rslt.add(m.getUser());
                }
            }
        }

        LOG.info("found " + rslt.size() + " member(s), unitId=" + unitId + ", role=" + memb.getRole());
        return rslt;
    }

//    /** {@inheritDoc} */
//    @Transactional(readOnly = true)
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    @Override
//    public List<Unit> getUnitsOfUser(final Object userId) {
//        final List<Unit> rslt = unitDao.getUnitsByUser(userId);
//        LOG.info("found " + rslt.size() + " units(s), userId=" + userId);
//        return rslt;
//    }

    // ------------------------------------------------------- Membership Stuff

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Membership storeMembership(
            final Object unitId, final Object userId,
            final Membership.Role role, final int significance) {

        if (null == userId) { throw new NullPointerException("user ID cannot be null"); }
        if (null == unitId) { throw new NullPointerException("unit ID cannot be null"); }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("trying to store membership, userId=" + userId + ", unitId=" + unitId + ", role=" + role);
        }

        Membership toStore = membershipDao.findByUserAndUnit(userId, unitId);

        if (null != toStore) {
            // update existing
            toStore.setRole(role.toString());
            toStore.setSignificance(significance);
        } else {
            // create new one
            final Unit unit = unitDao.getById(unitId);
            toStore = new Membership();
            toStore.setUser(userDao.getById(userId));
            toStore.setUnit(unit);
            toStore.setRole(role.toString());
            toStore.setSignificance(significance);
        }
        final Membership rslt = membershipDao.persist(toStore);

        LOG.info("stored membership, userId=" + userId + ", unitId=" + unitId + ", role=" + role);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public List<Membership> getMembershipsByUser(final Object userId) {
        final List<Membership> memberships = membershipDao.findBy("user", userId, "significance");
        LOG.info("membership(s) found, size=" + memberships.size());
        return memberships;
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Validation of a user before persistence.
     * @param user user to be validated
     * @param forCreate whether the object has to be created as new entry in DB
     */
    private void validateUser(final User user, final boolean forCreate) {
        if (null == user) { throw new NullPointerException("user cannot be null"); }
        if (Strings.isNullOrEmpty(user.getUsername())) {
            throw new IllegalArgumentException("username cannot be blank");
        }
        if (Strings.isNullOrEmpty(user.getPassword())) {
            throw new IllegalArgumentException("password cannot be blank");
        }
        if (Strings.isNullOrEmpty(user.getFullname())) {
            throw new IllegalArgumentException("fullname cannot be blank");
        }
        if (forCreate) {
            if (null != user.getId()) {
                throw new IllegalArgumentException("expected object with empty ID");
            }
        } else {
            if (null == user.getId()) { throw new NullPointerException("object ID cannot be null"); }
        }
    }

//    /**
//     *
//     * @param unitId the id of unit to which the membership is related
//     * @param userId the id of user to which the membership is related
//     * @param type the type of membership
//     * @param significance the significance of membership
//     */
//    private void createMembershipWithReorg(
//            final Long unitId, final Long userId,
//            final MembershipDto.Type type, final int significance) {
//
//        List<Membership> memberships = membershipDao.findBy("userId", userId, "significance");
//
//        final Membership newMembership = new Membership();
//        newMembership.setType(type);
//        newMembership.setSignificance(significance);
//        newMembership.setUserId(userId);
//        newMembership.setUnitId(unitId);
//
//        List<Membership> newMemberships = new ArrayList<Membership>(memberships);
//        if (significance > newMemberships.size()) {
//            newMemberships.add(newMembership);
//        } else {
//            newMemberships.add(significance, newMembership);
//        }
//        for (int i = 0; i < newMemberships.size(); i++) {
//            Membership membership = newMemberships.get(i);
//            membership.setSignificance(i);
//            membershipDao.persist(membership);
//            LOG.info("set new value " + i + " as significance of membership with ID " + membership.getId());
//        }
//        LOG.info("created membership " + newMembership);
//    }
//
//    /**
//    *
//    * @param unitId the id of unit to which the membership is related
//    * @param userId the id of user to which the membership is related
//    * @param type the type of membership
//    * @param significance the significance of membership
//    */
//    private void updateMembershipWithReorg(
//            final Long unitId, final Long userId,
//            final MembershipDto.Type type, final int significance) {
//
//        List<Membership> memberships = membershipDao.findBy("userId", userId, "significance");
//
//        Membership newMembership = null;
//        for (Membership membership: memberships) {
//            if (membership.getUnitId() == unitId) {
//                newMembership = membership;
//                break;
//            }
//        }
//
//        if (newMembership == null) {
//            throw new IllegalStateException("no membership for unit id="
//                    + unitId + " and user id=" + userId + " found, but it should exist");
//        }
//
//        newMembership.setType(type);
//        List<Membership> newMemberships = new ArrayList<Membership>(memberships);
//        newMemberships.remove(newMembership);
//        if (significance > newMemberships.size()) {
//            newMemberships.add(newMembership);
//        } else {
//            newMemberships.add(significance, newMembership);
//        }
//
//        for (int i = 0; i < newMemberships.size(); i++) {
//            Membership membership = newMemberships.get(i);
//            membership.setSignificance(i);
//            membershipDao.persist(membership);
//            LOG.info("set new value " + i + " as significance of membership with ID " + membership.getId());
//        }
//        LOG.info("updated membership " + newMembership);
//    }

}
