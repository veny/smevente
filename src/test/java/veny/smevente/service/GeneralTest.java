package veny.smevente.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.orientechnologies.orient.core.id.ORecordId;

import veny.smevente.AbstractBaseTest;
import veny.smevente.client.utils.Pair;
import veny.smevente.dao.DeletedObjectException;
import veny.smevente.dao.UserDao;
import veny.smevente.model.User;

/**
 * Test of general stuff.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2014
 */
public class GeneralTest extends AbstractBaseTest {

    // CHECKSTYLE:OFF
    @Autowired
    protected UserDao userDao;
    // CHECKSTYLE:ON

    /** Clear security contect to perform logout. */
    @After
    public void logout() {
        SecurityContextHolder.clearContext();
    }


    /** Test of updatedBy/updatedAt flags if no logged-in user. */
    @Test
    public void testUpdatedFlagsNoAuthentication() {
        User user  = createDefaultUser();
        assertNull(user.getUpdatedBy());
        assertNull(user.getUpdatedAt());

        user.setUsername("alfa");
        userService.updateUser(user);
        user = userService.getUser(user.getId());
        assertEquals("alfa", user.getUsername());
        assertNull(user.getUpdatedBy()); // no logged-in user -> no value
        assertNotNull(user.getUpdatedAt()); // date is set always, even if not logged-in user
    }

    /** Test of updatedBy/updatedAt flags. */
    @Test
    public void testUpdatedFlags() {
        login();

        User user  = createDefaultUser();
        assertNull(user.getUpdatedBy());
        assertNull(user.getUpdatedAt());

        user.setUsername("alfa");
        userService.updateUser(user);
        user = userService.getUser(user.getId());
        assertEquals("alfa", user.getUsername());
        assertNotNull(user.getUpdatedBy());
        assertEquals("#100:101", user.getUpdatedBy());
        assertNotNull(user.getUpdatedAt());

        // check timestamp aktualization
        Date last = user.getUpdatedAt();
        userService.updateUser(user);
        user = userService.getUser(user.getId());
        assertNotNull(user.getUpdatedBy());
        assertEquals("#100:101", user.getUpdatedBy());
        assertNotNull(user.getUpdatedAt());
        assertTrue(last.before(user.getUpdatedAt()));
    }

    /** Test of deletedBy/deletedAt flags if no logged-in user. */
    @Test
    public void testDeletedFlagsNoAuthentication() {
        User user  = createDefaultUser();
        assertNull(user.getDeletedBy());
        assertNull(user.getDeletedAt());

        userService.deleteUser(user.getId());
        try {
            userService.getUser(user.getId());
            assertEquals("expected DeletedObjectException", true, false);
        } catch (DeletedObjectException e) { assertEquals(true, true); }

        List<User> users = userDao.getAll(true);
        assertEquals(1, users.size());
        assertNull(users.get(0).getDeletedBy()); // no logged-in user -> no value
        assertNotNull(users.get(0).getDeletedAt()); // date is set always, even if not logged-in user
    }

    /** Test of deletedBy/deletedAt flags. */
    @Test
    public void testDeletedFlags() {
        login();

        User user  = createDefaultUser();
        assertNull(user.getDeletedBy());
        assertNull(user.getDeletedAt());

        userService.deleteUser(user.getId());
        List<User> users = userDao.getAll(true);
        assertEquals(1, users.size());
        assertEquals("#100:101", users.get(0).getDeletedBy());
        assertNotNull(users.get(0).getDeletedAt());
        assertTrue(users.get(0).isDeleted());
    }

    // -------------------------------------------------------- Assistant Stuff

    /** Simulate a login of user. */
    public void login() {
        final User forLogin = new User();
        forLogin.setId(new ORecordId("#100:101"));
        forLogin.setUsername("aXrt34");
        forLogin.setPassword(userService.encodePassword("a"));
        final SecurityContext sc = SecurityContextHolder.getContext();
        final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                forLogin.getUsername(), forLogin.getPassword(), authorities);
        auth.setDetails(new Pair<User, List<Object>>(forLogin, new ArrayList<Object>()));
        sc.setAuthentication(auth);
    }

}
