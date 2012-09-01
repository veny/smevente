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
import veny.smevente.dao.jpa.gae.MembershipDaoGae;
import veny.smevente.dao.jpa.gae.UnitDaoGae;
import veny.smevente.dao.jpa.gae.UserDaoGae;
import veny.smevente.model.MembershipDto.Type;
import veny.smevente.model.UnitDto;
import veny.smevente.model.User;
import veny.smevente.model.gae.Membership;
import veny.smevente.model.gae.Unit;
import veny.smevente.model.gae.User;
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
    protected UnitDto unit1;
    protected UnitDto unit2;
    // CHECKSTYLE:ON

    /** User DAO. */
    @Autowired
    private UserDaoGae userDao;
    /** Unit DAO. */
    @Autowired
    private UnitDaoGae unitDao;
    /** Membership DAO. */
    @Autowired
    private MembershipDaoGae membershipDao;

    /**
     * Prepare users/units and corresponding memberships.
     */
    @Before
    public void setUp() {
        User user = new User();
        user.setUsername("aXrt34");
        user.setPassword(userService.encodePassword("a"));
        user.setFullname("V aXrt34");
        userDao.persist(user);
        user1 = user.mapToDto();

        Unit unit = new Unit();
        unit.setName("aKhfU89");
        unit.setMetadata("a=a");
        unitDao.persist(unit);
        unit1 = unit.mapToDto();
        // --
        unit = new Unit();
        unit.setName("bOpDe88");
        unit.setMetadata("b=b");
        unitDao.persist(unit);
        unit2 = unit.mapToDto();

        Membership m = new Membership();
        m.setUserId(user1.getId());
        m.setUnitId(unit1.getId());
        m.setType(Type.ADMIN);
        membershipDao.persist(m);
        // --
        m = new Membership();
        m.setUserId(user1.getId());
        m.setUnitId(unit2.getId());
        m.setType(Type.MEMBER);
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
        final List<Long> adminUnits = Arrays.asList(new Long[] { unit1.getId() });
        auth.setDetails(new Pair<Long, List<Long>>(user.getId(), adminUnits));
        sc.setAuthentication(auth);
    }

    /**
     * Logout user.
     */
    protected final void logout() {
        SecurityContextHolder.clearContext();
    }

}
