package veny.smevente.service.gae;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

/**
 * This class represents an entry point to the GAE cache.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 3.3.2011
 */
public final class GaeCache {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GaeCache.class.getName());

    /** GAE cache interface. */
    private final Cache cache;

    /** Constructor. */
    private GaeCache() {
        try {
            final CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
// http://code.google.com/p/googleappengine/issues/detail?id=5039
//            final Map<String, Object> props = new HashMap<String, Object>();
//            props.put(GCacheFactory.EXPIRATION_DELTA, 3600);
            final Map<String, Object> props = Collections.emptyMap();

            cache = cacheFactory.createCache(props);
            LOG.info("GAE cache initialized ok, props=" + props);
        } catch (CacheException e) {
            LOG.log(Level.SEVERE, "failed to initialize GAE cache", e);
            throw new IllegalStateException("failed to initialize GAE cache", e);
        }
    }

    /**
     * Gets GAE cache interface.
     * @return GAE cache interface
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Gets a cached value.
     * @param <T> return type of a found value
     * @param key cache key
     * @return found cached value or <i>null</i> if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String key) {
        try {
            return (T) cache.get(key);
        } catch (Throwable t) {
            clear();
            LOG.log(Level.WARNING, "problem reading cache, cache cleaned", t);
        }
        return null;
    }

    /**
     * Puts a value into cache.
     * @param key cache key
     * @param value value to be cached
     */
    @SuppressWarnings("unchecked")
    public void put(final String key, final Object value) {
        cache.put(key, value);
    }

    /**
     * Clears the whole cache.
     */
    public void clear() {
        cache.clear();
    }

}
