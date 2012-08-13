package veny.smevente.service.gae;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Aspect for caching on GAE platform.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 3.3.2011
 * {@link http://thoughts.inphina.com/2010/10/04/google-app-engine-understanding-non-invasive-caching/}
 * {@link http://static.springsource.org/spring/docs/3.0.x/reference/aop.html}
 */
@Aspect
public class CacheAspect {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GaeCache.class.getName());

    /** Dependency. */
    @Autowired
    private GaeCache gaeCache;

    /** Pointcut for Unit/User getters. */
    @SuppressWarnings("unused")
    @Pointcut("execution(public * veny.smevente.service.gae.U*ServiceImpl.get*(..))")
    private void uGet() { }

    /** Pointcut for Unit/User finders. */
    @SuppressWarnings("unused")
    @Pointcut("execution(public * veny.smevente.service.gae.U*ServiceImpl.find*(..))")
    private void uFind() { }

    /** Pointcut for Unit/User getters or finders. */
    @SuppressWarnings("unused")
    @Pointcut("uGet() || uFind()")
    private void fromCache() { }


    /** Pointcut for Unit/User creators. */
    @SuppressWarnings("unused")
    @Pointcut("execution(public * veny.smevente.service.gae.U*ServiceImpl.create*(..))")
    private void uCreate() { }

    /** Pointcut for Unit/User updators. */
    @SuppressWarnings("unused")
    @Pointcut("execution(public * veny.smevente.service.gae.U*ServiceImpl.update*(..))")
    private void uUpdate() { }

    /** Pointcut for Unit/User deletors. */
    @SuppressWarnings("unused")
    @Pointcut("execution(public * veny.smevente.service.gae.U*ServiceImpl.delete*(..))")
    private void uDelete() { }

    /** Pointcut for Unit/User creators or updators or deletors. */
    @SuppressWarnings("unused")
    @Pointcut("uCreate() || uUpdate() || uDelete()")
    private void clearCache() { }


    /**
     * Advice to get a value from cache before method invocation or
     * get the return value from business method and store it in cache
     * if not found in cache.
     *
     * @param pjp joint point
     * @return value found in cache or returned from business method
     * @throws Throwable if something goes wrong
     */
    @Around("fromCache()")
    public Object get(final ProceedingJoinPoint pjp) throws Throwable {
        final String className = pjp.getTarget().getClass().getSimpleName();
        final String methodName = pjp.getSignature().getName();
        final String cacheKey = getCacheKey(className, methodName, pjp.getArgs());
        Object returnedValue = gaeCache.get(cacheKey);
        if (null == returnedValue) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("CACHE MISS, key=" + cacheKey);
            }
            returnedValue = pjp.proceed();
            gaeCache.put(cacheKey, returnedValue);
        } else {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("CACHE HIT!, key=" + cacheKey);
            }
        }
        return returnedValue;
    }

    /**
     * Clears the whole cache.
     * @param jp joint point
     */
    @Before("clearCache()")
    public void clear(final JoinPoint jp) {
        gaeCache.clear();
        if (LOG.isLoggable(Level.FINER)) {
            final String className = jp.getTarget().getClass().getSimpleName();
            final String methodName = jp.getSignature().getName();
            LOG.finer("CACHE cleared, operation=" + className + "_" + methodName);
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Gets a cache key that contains following segments separated by '_':<ul>
     * <li>class name
     * <li>method name
     * <li>all arguments
     * </ul>.
     * @param className class name
     * @param methodName method name
     * @param args arguments
     * @return the cache key
     */
    private String getCacheKey(final String className, final String methodName, final Object[] args) {
        final StringBuilder sb = new StringBuilder(className);
        sb.append('_').append(methodName);
        for (Object arg : args) {
            sb.append('_').append(null == arg ? "null" : arg.toString());
        }
        return sb.toString();
    }

}
