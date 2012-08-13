package veny.smevente.security;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * Represents dummy voter - all access are granted.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
public class DummyPermissionVoter extends AbstractPermissionVoter {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(DummyPermissionVoter.class.getName());

    /** {@inheritDoc} */
    @Override
    public int vote(final Authentication authentication, final Object object,
            final Collection<ConfigAttribute> attributes) {

        LOG.warning("DUMMY permission should not be used.");
        return ACCESS_GRANTED;
    }

    /** @return symbolic name of this permission */
    @Override
    public String getTargetPermission() {
        return "V_DUMMY";
    }

}
