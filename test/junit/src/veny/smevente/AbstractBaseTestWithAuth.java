package veny.smevente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import veny.smevente.client.utils.Pair;
import veny.smevente.dao.MembershipDao;
import veny.smevente.dao.UserDao;
import veny.smevente.dao.orientdb.UnitDaoImpl;
import veny.smevente.model.Membership;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.security.SmeventeRole;

/**
 * Base class for the <i>Wave</i> tests with authentication and authorization.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
@ContextConfiguration({ "/appctx-common.xml", "/appctx-validation.xml", "/appctx-security.xml" })
public abstract class AbstractBaseTestWithAuth extends AbstractBaseTest {

    // CHECKSTYLE:OFF
    protected User user1;
    protected Unit unit1;
    protected Unit unit2;
    // CHECKSTYLE:ON

    /** User DAO. */
    @Autowired
    private UserDao userDao;
    /** Unit DAO. */
    @Autowired
    private UnitDaoImpl unitDao;
    /** Membership DAO. */
    @Autowired
    private MembershipDao membershipDao;

    /**
     * Prepare users/units and corresponding memberships.
     */
    @Before
    public void setUp() {
        User user = new User();
        user.setUsername("aXrt34");
        user.setPassword(userService.encodePassword("a"));
        user.setFullname("V aXrt34");
        user1 = userDao.persist(user);

        Unit unit = new Unit();
        unit.setName("aKhfU89");
//XXX        unit.setMetadata("a=a");
        unit1 = unitDao.persist(unit);
        // --
        unit = new Unit();
        unit.setName("bOpDe88");
//XXX        unit.setMetadata("b=b");
        unit2 = unitDao.persist(unit);

        Membership m = new Membership();
        m.setUser(user1);
        m.setUnit(unit1);
        m.setRole(Membership.Role.ADMIN.toString());
        membershipDao.persist(m);
        // --
        m = new Membership();
        m.setUser(user1);
        m.setUnit(unit2);
        m.setRole(Membership.Role.MEMBER.toString());
        membershipDao.persist(m);

        login(user1);
    }

    /** Tears down test. */
    @After
    public void tearDown() {
        logout();
    }

    /**
     * Login user.
     * @param user the user to log in
     */
    protected final void login(final User user) {
        final SecurityContext sc = SecurityContextHolder.getContext();
        final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl(SmeventeRole.ROLE_AUTHENTICATED.name()));
        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword(), authorities);
        final List<Object> adminUnits = Arrays.asList(new Object[] { unit1.getId() });
        auth.setDetails(new Pair<Object, List<Object>>(user.getId(), adminUnits));
        sc.setAuthentication(auth);
    }

    /**
     * Logout user.
     */
    protected final void logout() {
        SecurityContextHolder.clearContext();
    }

}
