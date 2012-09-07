package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.orientechnologies.orient.core.id.ORecordId;

import veny.smevente.AbstractBaseTest;
import veny.smevente.dao.ObjectNotFoundException;
import veny.smevente.model.Membership;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import eu.maydu.gwt.validation.client.ValidationException;

/**
 * Test of <code>UserService</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.11.2010
 */
public class UserServiceTest extends AbstractBaseTest {

    /** UserService.createUser. */
    @SuppressWarnings("deprecation")
    @Test
    public void testCreateUser() {
        userService.createUser(USERNAME, PASSWORD, FULLNAME, false /* root */);
        final List<User> found = userService.getAllUsers();
        assertEquals(1, found.size());
        assertDefaultUser(found.get(0));

        final User a = userService.createUser("a", "a", "a a", false);
        userService.createUser("b", "b", "b b", false);
        assertEquals(3, userService.getAllUsers().size());

        try { // existing username
            userService.createUser("a", "a", "a a", false);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }

        // SOFT DELETE
        // deleted user doesn't block a unique name
        userService.deleteUser(a.getId());
        assertEquals(2, userService.getAllUsers().size());
        userService.createUser("a", "a", "a a", false);
        assertEquals(3, userService.getAllUsers().size());
    }

    /** UserService.createUser. */
    @SuppressWarnings("deprecation")
    @Test
    public void testCreateUserAndMembership() {
        final Unit unit1 = createDefaultUnit();
        User userA = new User();
        userA.setUsername("a");
        userA.setFullname("a a");
        userA.setPassword("a");
        userA = userService.createUser(userA, unit1.getId(), Membership.Role.MEMBER, 0);
        assertNotNull(userA);

        User userB = new User();
        userB.setUsername("b");
        userB.setFullname("b b");
        userB.setPassword("b");
        userB = userService.createUser(userB, unit1.getId(), Membership.Role.ADMIN, 6);
        assertNotNull(userB);

        final List<User> found = userService.getAllUsers();
        assertNotNull(found);
        assertEquals(2, found.size());

        User badUser = new User();
        badUser.setUsername("a");
        badUser.setFullname("a a");
        badUser.setPassword("a");

        try { // existing username
            userService.createUser(badUser, unit1.getId(), Membership.Role.MEMBER, 0);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }

        try { // invalid significance
            badUser.setUsername("badUser");
            userService.createUser(badUser, unit1.getId(), Membership.Role.MEMBER, -1);
            assertEquals("expected IllegalArgumentException", true, false);
        } catch (IllegalArgumentException e) { assertEquals(true, true); }

//XXX        List<Membership> memberships = userService.findMembershipsByUser(userA.getId());
//        assertNotNull(memberships);
//        assertEquals(1, memberships.size());
//        assertEquals(0, memberships.get(0).getSignificance());
//
//        memberships = userService.findMembershipsByUser(userB.getId());
//        assertNotNull(memberships);
//        assertEquals(1, memberships.size());
//        assertEquals(0, memberships.get(0).getSignificance());
//
//        // SOFT DELETE
//        // deleting of user force also deleting of membership
//        userService.deleteUser(userA.getId());
//        assertEquals(1, userService.getAllUsers().size());
//        assertEquals(0, userService.findMembershipsByUser(userA.getId()).size());
    }

    /** UserService.getUser. */
    @Test
    public void testGetUser() {
        final User created = createDefaultUser();
        final User found = userService.getUser(created.getId());
        assertDefaultUser(found);

        try { // invalid ID class
            userService.getUser("xx");
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }

        try { // invalid ID
            userService.getUser(new ORecordId("#1001:123456789"));
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
    }

//    /** UserService.updateUser. */
//    @SuppressWarnings("deprecation")
//    @Test
//    public void testUpdateUser() {
//        final UnitDto unit1 = createDefaultUnit();
//        final User user1 = userService.createUser(USERNAME, PASSWORD, FULLNAME, false);
//        userService.createMembership(unit1.getId(), user1.getId(), MembershipDto.Type.MEMBER, 0);
//        final List<User> found = userService.findUsers(unit1.getId(), USERNAME, FULLNAME);
//        assertNotNull(found);
//        assertEquals(1, found.size());
//        assertDefaultUser(found.get(0));
//
//        User userA = new User();
//        userA.setUsername("a");
//        userA.setPassword("a");
//        userA.setFullname("a a");
//        userA = userService.createUser(userA, unit1.getId(), MembershipDto.Type.MEMBER, 0);
//        assertNotNull(userA);
//
//        User userB = new User();
//        userB.setUsername("b");
//        userB.setPassword("b");
//        userB.setFullname("b b");
//        userB = userService.createUser(userB, unit1.getId(), MembershipDto.Type.MEMBER, 0);
//        assertNotNull(userB);
//
//        assertEquals(3, userService.getAllUsers().size());
//
//        try { // existing username
//            userB.setUsername(USERNAME);
//            userService.updateUser(userB);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//        try { // existing username
//            userB.setUsername("a");
//            userService.updateUser(userB);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//        try { // invalid username
//            userB.setUsername(null);
//            userService.updateUser(userB);
//            assertEquals("expected NullPointerException", true, false);
//        } catch (NullPointerException e) { assertEquals(true, true); }
//        try { // invalid password
//            userB.setUsername("B");
//            userB.setPassword(null);
//            userService.updateUser(userB);
//            assertEquals("expected NullPointerException", true, false);
//        } catch (NullPointerException e) { assertEquals(true, true); }
//        try { // existing username
//            userB.setPassword("B");
//            userB.setFullname(null);
//            userService.updateUser(userB);
//            assertEquals("expected NullPointerException", true, false);
//        } catch (NullPointerException e) { assertEquals(true, true); }
//        try { // invalid fullname
//            userB.setFullname("B B");
//            userService.updateUser(userB);
//            assertEquals(true, true);
//        } catch (Exception e) { e.printStackTrace(); assertEquals(" Not expected any Exception", true, false); }
//
//        assertEquals(3, userService.getAllUsers().size());
//        assertNotNull("User should exist", userService.findUserByUsername("B"));
//        assertEquals(1, userService.findUsers(unit1.getId(), "B", "B B").size());
//    }
//
//    /** UserService.createUser. */
//    @SuppressWarnings("deprecation")
//    @Test
//    public void testUpdateUserAndMembetship() {
//        final UnitDto unit1 = createDefaultUnit();
//        final UnitDto unit2 = createUnit("unit2", getDefaultUnitMetadata(), LIMITED_SMSS);
//        User userA = new User();
//        userA.setUsername("a");
//        userA.setFullname("a a");
//        userA.setPassword("a");
//        userA = userService.createUser(userA, unit1.getId(), MembershipDto.Type.MEMBER, 0);
//        assertNotNull(userA);
//
//        User userB = new User();
//        userB.setUsername("b");
//        userB.setFullname("b b");
//        userB.setPassword("b");
//        userB = userService.createUser(userB, unit1.getId(), MembershipDto.Type.ADMIN, 6);
//        assertNotNull(userB);
//
//        final List<User> found = userService.getAllUsers();
//        assertNotNull(found);
//        assertEquals(2, found.size());
//
//        try { // existing username
//            userA.setUsername(userB.getUsername());
//            userService.updateUser(userA, unit1.getId(), MembershipDto.Type.MEMBER, 0);
//            assertEquals("expected ValidationException", true, false);
//        } catch (ValidationException e) { assertEquals(true, true); }
//
//        try { // invalid significance
//            userService.updateUser(userB, unit1.getId(), MembershipDto.Type.MEMBER, -1);
//            assertEquals("expected IllegalArgumentException", true, false);
//        } catch (IllegalArgumentException e) { assertEquals(true, true); }
//
//        // create membership for second unit
//        userService.createMembership(unit2.getId(), userA.getId(), MembershipDto.Type.MEMBER, 0);
//
//        // update user name
//        userA.setUsername("aA");
//        userService.updateUser(userA, unit1.getId(), MembershipDto.Type.ADMIN, 0);
//        userService.updateUser(userA, unit2.getId(), MembershipDto.Type.MEMBER, 0);
//
//        // test membership values
//        List<MembershipDto> memberships = userService.findMembershipsByUser(userA.getId());
//        assertNotNull(memberships);
//        assertEquals(2, memberships.size());
//        assertEquals(0, memberships.get(0).getSignificance());
//        assertEquals(MembershipDto.Type.MEMBER, memberships.get(0).getType());
//        assertEquals(1, memberships.get(1).getSignificance());
//        assertEquals(MembershipDto.Type.ADMIN, memberships.get(1).getType());
//
//        // significance will not be changed
//        userService.updateUser(userA, unit1.getId(), MembershipDto.Type.ADMIN, 9);
//        memberships = userService.findMembershipsByUser(userA.getId());
//        assertNotNull(memberships);
//        assertEquals(2, memberships.size());
//        assertEquals(0, memberships.get(0).getSignificance());
//        assertEquals(MembershipDto.Type.MEMBER, memberships.get(0).getType());
//        assertEquals(1, memberships.get(1).getSignificance());
//        assertEquals(MembershipDto.Type.ADMIN, memberships.get(1).getType());
//
//        // significance will be changed
//        userService.updateUser(userA, unit1.getId(), MembershipDto.Type.ADMIN, 0);
//        memberships = userService.findMembershipsByUser(userA.getId());
//        assertNotNull(memberships);
//        assertEquals(2, memberships.size());
//        assertEquals(0, memberships.get(0).getSignificance());
//        assertEquals(MembershipDto.Type.ADMIN, memberships.get(0).getType());
//        assertEquals(1, memberships.get(1).getSignificance());
//        assertEquals(MembershipDto.Type.MEMBER, memberships.get(1).getType());
//
//        // type will be changed
//        userService.updateUser(userA, unit1.getId(), MembershipDto.Type.MEMBER, 0);
//        memberships = userService.findMembershipsByUser(userA.getId());
//        assertNotNull(memberships);
//        assertEquals(2, memberships.size());
//        assertEquals(0, memberships.get(0).getSignificance());
//        assertEquals(MembershipDto.Type.MEMBER, memberships.get(0).getType());
//        assertEquals(1, memberships.get(1).getSignificance());
//        assertEquals(MembershipDto.Type.MEMBER, memberships.get(1).getType());
//    }

    /** UserService.deleteUser. */
    @SuppressWarnings("deprecation")
    @Test
    public void testDeleteUser() {
        final User firstCreated = createDefaultUser();
        final User secondCreated = createUser("a", "a", "a a", false);
        assertEquals(2, userService.getAllUsers().size());

        // delete first
        userService.deleteUser(firstCreated.getId());
        List<User> found = userService.getAllUsers();
        assertNotNull(found);
        assertEquals(1, found.size());
        assertEquals(secondCreated.getId(), found.get(0).getId());

        final User thirdCreated = createUser("b", "b", "b b", false);
        found = userService.getAllUsers();
        assertNotNull(found);
        assertEquals(2, found.size());
        userService.deleteUser(firstCreated.getId()); // DO NOTHING
        // delete second
        userService.deleteUser(secondCreated.getId());
        found = userService.getAllUsers();
        assertNotNull(found);
        assertEquals(1, found.size());
        assertEquals(thirdCreated.getId(), found.get(0).getId());

        // add membership
        final Unit unit = createDefaultUnit();
        userService.storeMembership(unit.getId(), thirdCreated.getId(), Membership.Role.MEMBER, 1);
        final List<Membership> membs = userService.findMembershipsByUser(thirdCreated.getId());
        assertEquals(1, membs.size());
        assertEquals(Membership.Role.MEMBER, membs.get(0).enumRole());
        assertEquals(1, membs.get(0).getSignificance());
        assertDefaultUnit(membs.get(0).getUnit());

        // delete third
        userService.deleteUser(thirdCreated.getId());

        userService.deleteUser(thirdCreated.getId()); // DO NOTHING
        found = userService.getAllUsers();
        assertNotNull(found);
        assertEquals(0, found.size());

        // SOFT DELETE
        // deleting of user force also deleting of membership
        assertTrue(userService.findMembershipsByUser(firstCreated.getId()).isEmpty());
        assertTrue(userService.findMembershipsByUser(secondCreated.getId()).isEmpty());
        assertTrue(userService.findMembershipsByUser(thirdCreated.getId()).isEmpty());
    }

    /** UserService.findUserByUsername. */
    @Test
    public void testFindUserByUsername() {
        createDefaultUser();
        final User found = userService.findUserByUsername(USERNAME);
        assertDefaultUser(found);

        assertNull(userService.findUserByUsername(USERNAME.toUpperCase()));
        assertNull(userService.findUserByUsername(USERNAME + "x"));

        // SOFT DELETE
        userService.deleteUser(found.getId());
        assertNull(userService.findUserByUsername(USERNAME));
    }

//    /** UserService.findUsers. */
//    @Test
//    public void testFindUsers() {
//        final UnitDto unit1 = createDefaultUnit();
//        final User user1 = createDefaultUser();
//        userService.createMembership(unit1.getId(), user1.getId(), MembershipDto.Type.MEMBER, 0);
//        final List<User> found = userService.findUsers(unit1.getId(), USERNAME, FULLNAME);
//        assertEquals(1, found.size());
//
//        assertEquals(0, userService.findUsers(unit1.getId(), USERNAME.toUpperCase(), null).size());
//        assertEquals(0, userService.findUsers(unit1.getId(), USERNAME + "x", null).size());
//        assertEquals(1, userService.findUsers(unit1.getId(), USERNAME, null).size());
//        assertEquals(0, userService.findUsers(unit1.getId(), null, FULLNAME.toUpperCase()).size());
//        assertEquals(0, userService.findUsers(unit1.getId(), null, FULLNAME + "x").size());
//        assertEquals(1, userService.findUsers(unit1.getId(), null, FULLNAME).size());
//
//        // SOFT DELETE
//        userService.deleteUser(found.get(0).getId());
//        assertEquals(0, userService.findUsers(unit1.getId(), USERNAME, FULLNAME).size());
//    }

    /** UserService.updateUserPassword. */
    @Test
    @SuppressWarnings("deprecation")
    public void testUpdateUserPassword() {
        final User created = createDefaultUser();

        final String newPassword = PASSWORD + "x";
        assertEquals(1, userService.getAllUsers().size());
        userService.updateUserPassword(created.getId(), PASSWORD, newPassword);
        assertEquals(1, userService.getAllUsers().size()); // stored existing user, no duplicate caused by Dao#persist

        assertNotNull(userService.performLogin(USERNAME, newPassword));
        assertEquals(userService.encodePassword(newPassword), userService.findUserByUsername(USERNAME).getPassword());

        try { // bad user ID class
            userService.updateUserPassword("xx", PASSWORD, newPassword);
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
        try { // bad user ID
            userService.updateUserPassword(new ORecordId("#1001:123456789"), PASSWORD, newPassword);
            assertEquals("expected ObjectNotFoundException", true, false);
        } catch (ObjectNotFoundException e) { assertEquals(true, true); }
        try { // bad old password
            userService.updateUserPassword(created.getId(), PASSWORD, newPassword);
            assertEquals("expected ValidationException", true, false);
        } catch (ValidationException e) { assertEquals(true, true); }
    }

    /** UserService.performLogin. */
    @Test
    public void testPerformLogin() {
        User created = createDefaultUser();
        assertNull(created.getLastLoggedIn());
        final Date now = new Date();
        final User found = userService.performLogin(USERNAME, PASSWORD);
        assertDefaultUser(found);
        assertNotNull(found.getLastLoggedIn());
        assertTrue(found.getLastLoggedIn().getTime() > now.getTime());

        assertNull(userService.performLogin(USERNAME, PASSWORD + "x"));
        assertNull(userService.performLogin(USERNAME + "x", PASSWORD));

        // SOFT DELETE
        // can't login with deleted user
        userService.deleteUser(found.getId());
        assertNull(userService.performLogin(USERNAME, PASSWORD));
    }

//    /** UserService.getUnitsOfUser. */
//    @Test
//    public void testGetUnitsOfUser() {
//        // unit1 - user1 as MEMBER
//        final UnitDto unit1 = createDefaultUnit();
//        final User user1 = createDefaultUser();
//        userService.createMembership(unit1.getId(), user1.getId(), Type.MEMBER, 100);
//
//        List<UnitDto> found = userService.getUnitsOfUser(user1);
//        assertNotNull(found);
//        assertEquals(1, found.size());
//        assertEquals(1, found.get(0).getMembers().size());
//        assertEquals(Type.MEMBER, found.get(0).getMembers().get(0).getType());
//        assertDefaultUser(found.get(0).getMembers().get(0).getUser());
//
//        // unit1 - user1 as MEMBER
//        // unit1 - user2 as MEMBER
//        final User user2 = createUser("a", "a", "a a", false);
//        userService.createMembership(unit1.getId(), user2.getId(), Type.MEMBER, 100);
//
//        found = userService.getUnitsOfUser(user1);
//        assertEquals(1, found.size());
//        assertEquals(1, found.get(0).getMembers().size());
//        assertEquals(Type.MEMBER, found.get(0).getMembers().get(0).getType());
//        assertEquals(USERNAME, found.get(0).getMembers().get(0).getUser().getUsername());
//
//        found = userService.getUnitsOfUser(user2);
//        assertEquals(1, found.size());
//        assertEquals(1, found.get(0).getMembers().size());
//        assertEquals(Type.MEMBER, found.get(0).getMembers().get(0).getType());
//        assertEquals("a", found.get(0).getMembers().get(0).getUser().getUsername());
//
//        // unit1 - user1 as MEMBER
//        // unit1 - user2 as MEMBER
//        // unit1 - user3 as ADMIN
//        // unit2 - user1 as ADMIN
//        // unit2 - user3 as ADMIN
//        final User user3 = createUser("admin", "admin", "admin admin", false);
//        userService.createMembership(unit1.getId(), user3.getId(), Type.ADMIN, 100);
//        final UnitDto unit2 = createUnit("U", getDefaultUnitMetadata(), LIMITED_SMSS);
//        userService.createMembership(unit2.getId(), user1.getId(), Type.ADMIN, 200);
//        userService.createMembership(unit2.getId(), user3.getId(), Type.ADMIN, 50);
//
//        found = userService.getUnitsOfUser(user2);
//        assertEquals(1, found.size());
//
//        found = userService.getUnitsOfUser(user1);
//        assertEquals(2, found.size());
//        assertEquals(1, found.get(0).getMembers().size()); // first is unit1 (significance=100)
//        assertEquals(Type.MEMBER, found.get(0).getMembers().get(0).getType());
//        assertEquals(2, found.get(1).getMembers().size()); // second is unit2 (significance=200)
//        assertEquals(USERNAME, found.get(1).getMembers().get(0).getUser().getUsername());
//        assertEquals(Type.ADMIN, found.get(1).getMembers().get(0).getType());
//
//        found = userService.getUnitsOfUser(user3);
//        assertEquals(2, found.size());
//        assertEquals(2, found.get(0).getMembers().size()); // first is unit2 (significance=50)
//        assertEquals("admin", found.get(0).getMembers().get(0).getUser().getUsername());
//        assertEquals(Type.ADMIN, found.get(0).getMembers().get(0).getType());
//        assertEquals(3, found.get(1).getMembers().size()); // second is unit1 (significance=100)
//        assertEquals("admin", found.get(1).getMembers().get(0).getUser().getUsername());
//        assertEquals(Type.ADMIN, found.get(1).getMembers().get(0).getType());
//    }

}
