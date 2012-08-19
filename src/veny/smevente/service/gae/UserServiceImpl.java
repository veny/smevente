package veny.smevente.service.gae;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import veny.smevente.dao.UserDao;
import veny.smevente.dao.jpa.gae.MembershipDaoGae;
import veny.smevente.dao.jpa.gae.UnitDaoGae;
import veny.smevente.model.MembershipDto;
import veny.smevente.model.UnitDto;
import veny.smevente.model.User;
import veny.smevente.model.gae.Membership;
import veny.smevente.model.gae.Unit;
import veny.smevente.service.UserService;
import eu.maydu.gwt.validation.client.server.ServerValidation;


/**
 * Implementation of User service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class UserServiceImpl implements UserService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UserDao userDao;
    /** Dependency. */
//    @Autowired
//    private MembershipDaoGae membershipDao;
//    /** Dependency. */
//    @Autowired
//    private UnitDaoGae unitDao;

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
        validateUser(user);
        List<User> check = userDao.findBy("username", user.getUsername(), null);
        if (!check.isEmpty()) {
            ServerValidation.exception("duplicateValue", "username", (Object[]) null);
        }

        userDao.persist(user);

        LOG.info("created new user, " + user);
        return user;
    }

//    /** {@inheritDoc} */
//    @Transactional
//    @PreAuthorize("hasPermission(#unitId, 'V_UNIT_ADMIN')")
//    @Override
//    public User createUser(final User user,
//            final Long unitId,
//            final MembershipDto.Type type,
//            final Integer significance) {
//
//        if (significance < 0) {
//            throw new IllegalArgumentException("invalid value of significance: " + significance);
//        }
//        User rslt = createUser(user);
//        createMembershipWithReorg(unitId, rslt.getId(), type, significance);
//        return rslt;
//    }
//
//    /** {@inheritDoc} */
//    @Transactional(readOnly = true)
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    @Override
//    public User getUser(final Long id) {
//        final User rslt = userDao.getById(id).mapToDto();
//        LOG.info("found user by id=" + id);
//        return rslt;
//    }
//
//    /** {@inheritDoc} */
//    @Transactional
//    // TODO [veny,B] think of authorization
//    @Override
//    public void updateUser(final User user) {
//        if (null == user.getId()) { throw new NullPointerException("ID cannot be null"); }
//        validateUser(user);
//
//        // unique user name
//        List<User> check = userDao.findBy("username", user.getUsername(), null);
//        if (null != check && !check.isEmpty() && !check.get(0).getId().equals(user.getId())) {
//            ServerValidation.exception("duplicateValue", "username", (Object[]) null);
//        } else {
//            if (LOG.isLoggable(Level.FINER)) {
//                LOG.finer("duplicite user name check OK, un=" + user.getUsername());
//            }
//        }
//
//        final User userGae = userDao.getById(user.getId());
//        User.mapFromDto(user, userGae);
//        userDao.persist(userGae);
//        LOG.info("user updated, id=" + user.getId()
//                + ", username=" + user.getUsername() + ", fullname=" + user.getFullname());
//    }
//
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
//
//    /** {@inheritDoc} */
//    @Transactional
//    // TODO [veny,B] think of authorization
//    @Override
//    public void deleteUser(final Long id) {
//        final List<MembershipDto> memberships = findMembershipsByUser(id);
//        if (memberships != null) {
//            for (MembershipDto membership: memberships) {
//                membershipDao.remove(membership.getId());
//                LOG.info("membership (for user with id=" + id + ") deleted, id=" + membership.getId());
//            }
//        }
//        userDao.remove(id);
//        LOG.info("user deleted, id=" + id);
//    }
//
//    // no authorization - used in AuthenticationProvider
//    /** {@inheritDoc} */
//    @Override
//    public User findUserByUsername(final String username) {
//        if (null == username) { throw new NullPointerException("username cannot be null"); }
//
//        final List<User> found = userDao.findBy("username", username, null);
//        if (found.size() > 1) {
//            throw new IllegalStateException("duplicate username found (has to be unique!), username=" + username);
//        }
//
//        final User rslt;
//        if (0 == found.size()) {
//            rslt = null;
//        } else {
//            rslt = found.get(0).mapToDto();
//        }
//
//        LOG.info("found user by username, " + rslt);
//        return rslt;
//    }
//
//    /** {@inheritDoc} */
//    @SuppressWarnings("unchecked")
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    @Override
//    public List<User> findUsers(final Long unitId, final String userName, final String fullName) {
//        if (null == unitId) { throw new IllegalArgumentException("unit id cannot be null"); }
//
//        LOG.info("findUsers, unit id=" + unitId + ", user name=" + userName + ", full name=" + fullName);
//
//        List<User> users = null;
//        if (null == userName && null == fullName) {
//            users = getAllUsers();
//        } else {
//            List<User> collectedRslt = null;
//
//            // user name
//            if (null != userName) {
//                collectedRslt = userDao.findBy("username", userName, null);
//                if (LOG.isLoggable(Level.FINER)) {
//                    LOG.finer("user(s) found by user name, size=" + collectedRslt.size());
//                }
//            }
//
//            // full name
//            if (null != fullName) {
//                List<User> found = userDao.findBy("fullname", fullName, null);
//                if (LOG.isLoggable(Level.FINER)) {
//                    LOG.finer("user(s) found by fullname, size=" + found.size());
//                }
//                if (null == collectedRslt) {
//                    collectedRslt = found;
//                } else {
//                    collectedRslt = (List<User>) CollectionUtils.intersection(collectedRslt, found);
//                }
//            }
//
//            users = new ArrayList<User>();
//            for (User u : collectedRslt) {
//                final User user = u.mapToDto();
//                users.add(user);
//            }
//        }
//
//        // exist the found users in specified unit?
//        final List<User> rslt = new ArrayList<User>();
//        if (!users.isEmpty()) {
//            for (User user: users) {
//                final List<Membership> memberships = membershipDao.findBy("userId", user.getId(),
//                        "unitId", unitId, null);
//                if (!memberships.isEmpty()) {
//                    rslt.add(user);
//                }
//            }
//        }
//        LOG.info("returned users, size=" + rslt.size());
//        return rslt;
//    }
//
//    /** {@inheritDoc} */
//    @PreAuthorize("hasPermission(#userId, 'V_MY_USER')")
//    @Override
//    public void updateUserPassword(final Long userId, final String oldPassword, final String newPassword) {
//        if (null == userId) { throw new NullPointerException("user ID cannot be null"); }
//        if (null == oldPassword) { throw new NullPointerException("old password cannot be null"); }
//        if (null == newPassword) { throw new NullPointerException("new password cannot be null"); }
//        if (0 == newPassword.trim().length()) { throw new IllegalArgumentException("new password cannot be blank"); }
//
//        // check the old password
//        final User userGae = userDao.getById(userId);
//        if (!userGae.getPassword().equals(encodePassword(oldPassword))) {
//            LOG.warning("trying to change password, the old one is bad, userId=" + userId);
//            ServerValidation.exception("validationOldPasswordBad", "old", (Object[]) null);
//        }
//
//        userGae.setPassword(encodePassword(newPassword));
//        userDao.persist(userGae);
//        LOG.info("changed password, userId=" + userId);
//    }
//
//    /*
//     * Here is not used a TX because of GAE Entity Group limit for transaction.
//     * I avoided to use TX (not needed here).
//     */
//    // no authorization - used in AuthenticationProvider
//    /** {@inheritDoc} */
//    @Override
//    public User performLogin(final String username, final String password) {
//        if (null == username) { throw new NullPointerException("username cannot be null"); }
//        if (null == password) { throw new NullPointerException("password cannot be null"); }
//
//        final User userGae = userDao.findByUsernameAndPassword(username, encodePassword(password));
//
//        if (null == userGae) {
//            LOG.info("user not found by username & password, username=" + username);
//            return null;
//        }
//
//        // update last logged in timestamp
//        userGae.setLastLoggedIn(new Date());
//        userDao.persist(userGae);
//
//        final User rslt = userGae.mapToDto();
//        LOG.info("found user by username & password, " + rslt);
//        return rslt;
//    }
//
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
        return formatter.toString();
    }
//
//    /** {@inheritDoc} */
//    @Transactional(readOnly = true)
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    @Override
//    public List<User> getAllUsers() {
//        final List<User> found = userDao.getAll();
//        LOG.info("found all users, size=" + found.size());
//
//        final List<User> rslt = new ArrayList<User>();
//        for (User userGae : found) {
//            rslt.add(userGae.mapToDto());
//        }
//        LOG.info("returned users, size=" + rslt.size());
//        return rslt;
//    }
//
////    /*
////     * Here is not used a TX because of GAE Entity Group limit for transaction.
////     * I avoided to use TX (not needed here).
////     */
////    /** {@inheritDoc} */
////    @Override
////    public List<UserDto> getUsersByUnit(final Long unitId, final UserDto me) {
////        final List<UserDto> rslt = new ArrayList<UserDto>();
////        // load the user again <- 'me' is probably from session with memberships and units
////        // and insert it into result list
////        rslt.add(beanMapper.map(userDao.getById(me.getId()), UserDto.class));
////
////        // load my Membership
////        final List<MembershipGae> myMemberships = membershipDao.findBy("userId", me.getId(), "unitId", unitId, null);
////        if (1 != myMemberships.size()) {
////            throw new IllegalStateException("invalid amount of membership in unit, userId="
////                    + me.getId() + ", unitId=" + unitId + ", amount=" + myMemberships.size());
////        }
////        final MembershipGae myMembership = myMemberships.get(0);
////
////        // no Admin -> return only the user
////        if (MembershipDto.Type.ADMIN.equals(myMembership.getType())) {
////            // find other users
////            final List<MembershipGae> other = membershipDao.findBy("unitId", unitId, null);
////            for (MembershipGae m : other) {
////                if (!me.getId().equals(m.getUserId())) { // don't include 'me' again
////                    rslt.add(beanMapper.map(userDao.getById(m.getUserId()), UserDto.class));
////                }
////            }
////        }
////
////        LOG.info("found " + rslt.size() + " member(s), unitId=" + unitId);
////        return rslt;
////    }
//
//    /*
//     * Here is not used a TX because of GAE Entity Group limit for transaction.
//     * I avoided to use TX (not needed here).
//     */
//    /** {@inheritDoc} */
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    @Override
//    public List<UnitDto> getUnitsOfUser(final User user) {
//        final List<UnitDto> rslt = new ArrayList<UnitDto>();
//
//        // get memberships of given user
//        final List<Membership> memberships = membershipDao.findBy("userId", user.getId(), "significance");
//
//        for (Membership userMembGae : memberships) {
//            final Unit unitGae = unitDao.getById(userMembGae.getUnitId());
//            final UnitDto unit = unitGae.mapToDto();
//            rslt.add(unit);
//
//            // add the given user at first place
//            final MembershipDto memb = userMembGae.mapToDto();
//            final User uDto = userDao.getById(userMembGae.getUserId()).mapToDto();
//            memb.setUser(uDto);
//            unit.addMember(memb);
//
//            // Admin -> return other members
//            if (MembershipDto.Type.ADMIN.equals(userMembGae.getType())) {
//                // find other users
//                final List<Membership> other = membershipDao.findBy("unitId", userMembGae.getUnitId(), null);
//                for (Membership mGae : other) {
//                    if (!user.getId().equals(mGae.getUserId())) { // don't include 'me' again
//                        final MembershipDto membershipDto = mGae.mapToDto();
//                        final User userDto = userDao.getById(mGae.getUserId()).mapToDto();
//                        membershipDto.setUser(userDto);
//                        unit.addMember(membershipDto);
//                    }
//                }
//            }
//        }
//
//        LOG.info("found " + rslt.size() + " membership(s), userId=" + user.getId());
//        return rslt;
//    }
//
//    // ------------------------------------------------------- Membership Stuff
//
//    /*
//     * Here is not used a TX because of GAE Entity Group limit for transaction.
//     * I avoided to use TX (not needed here).
//     */
//    /** {@inheritDoc} */
//    @Override
//    public void createMembership(
//            final Long unitId, final Long userId,
//            final MembershipDto.Type type, final int significance) {
//
//        if (LOG.isLoggable(Level.FINER)) {
//            LOG.finer("trying to create membership, userId=" + userId + ", unitId=" + unitId + ", type=" + type);
//        }
//        // test existence of such combination
//        List<Membership> memberships = membershipDao.findBy("userId", userId, "unitId", unitId, null);
//        if (memberships.size() > 0) {
//            throw new IllegalStateException("membership already exists, userId=" + userId + ", unitId=" + unitId);
//        }
//
//        // test existence of unit
//        unitDao.getById(unitId);
//        // test existence of user
//        userDao.getById(userId);
//
//        final Membership m = new Membership();
//        m.setType(type);
//        m.setSignificance(significance);
//        m.setUserId(userId);
//        m.setUnitId(unitId);
//        membershipDao.persist(m);
//
//        LOG.info("created membership, userId=" + userId + ", unitId=" + unitId + ", type=" + type);
//    }
//
//    /*
//     * Here is not used a TX because of GAE Entity Group limit for transaction.
//     * I avoided to use TX (not needed here).
//     */
//    /** {@inheritDoc} */
//    @Override
//    public List<MembershipDto> findMembershipsByUser(final Long userId) {
//        final List<MembershipDto> rslt = new ArrayList<MembershipDto>();
//        final List<Membership> memberships = membershipDao.findBy("userId", userId, "significance");
//        LOG.info("membership(s) found, size=" + memberships.size());
//
//        if (!memberships.isEmpty()) {
//            final User userGae = userDao.getById(userId);
//            if (userGae == null) {
//                throw new IllegalStateException("no user found, userId=" + userId);
//            }
//            for (Membership m : memberships) {
//                final MembershipDto membership = m.mapToDto();
//                membership.setUser(userGae.mapToDto());
//                rslt.add(membership);
//            }
//        }
//
//        return rslt;
//    }
    // -------------------------------------------------------- Assistant Stuff

    /**
     * Validation of a user before persistence.
     * @param user the user to be validated
     */
    private void validateUser(final User user) {
        if (null == user.getUsername()) { throw new NullPointerException("username cannot be null"); }
        if (null == user.getPassword()) { throw new NullPointerException("password cannot be null"); }
        if (null == user.getFullname()) { throw new NullPointerException("fullname cannot be null"); }
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
