package veny.smevente.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 * The custom permission evaluator for Smevente.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.5.2011
 */
public class PermissionEvaluatorImpl implements PermissionEvaluator {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(PermissionEvaluatorImpl.class.getName());

    /** All project voters injected by the class. */
    @Autowired
    private Set<AbstractPermissionVoter> voters;

    /** Map of voters where the key is symbolic name of target permission. */
    private Map<String, AbstractPermissionVoter> votersMap;

    /**
     * Creates a map of voters.
     * @see #votersMap
     */
    @PostConstruct
    public void init() {
        votersMap = new HashMap<String, AbstractPermissionVoter>(voters.size());
        for (AbstractPermissionVoter voter : voters) {
            final String targetPermission = voter.getTargetPermission();
            if (votersMap.containsKey(targetPermission)) {
                throw new IllegalStateException("there are duplicate permissions with name=" + targetPermission);
            }
            votersMap.put(targetPermission, voter);
        }

        LOG.info("PermissionEvaluator initialized ok, voters=" + voters.size() + ", names=" + votersMap.keySet());
    }

    // ---------------------------------------------- PermissionEvaluator Stuff

    /** {@inheritDoc} */
    @Override
    public boolean hasPermission(final Authentication authentication,
            final Object targetDomainObject, final Object permission) {

        final AbstractPermissionVoter voter = votersMap.get(permission.toString());
        if (null == voter) {
            throw new IllegalStateException("voter not found, targetPermission=" + permission.toString());
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("checking permission, targetPermission=" + permission.toString());
        }
        return (voter.vote(authentication, targetDomainObject, null) == AccessDecisionVoter.ACCESS_GRANTED);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasPermission(
            final Authentication authentication, final Serializable targetId,
            final String targetType, final Object permission) {

        throw new IllegalStateException("not implemented yet");
    }

}
