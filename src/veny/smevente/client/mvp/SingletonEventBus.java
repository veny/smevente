package veny.smevente.client.mvp;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * This class provides singleton instance of the GWT {@link EventHandler} manager
 * called <code>Event Bus</code>.
 *
 * Rather than being attached to a single object, an EventBus provides a central
 * pathway to send events across the whole application.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public final class SingletonEventBus {

    /** Singleton instance. */
    private static EventBus instance = null;

    /**
     * Constructor to avoid an other instance than the singleton.
     */
    private SingletonEventBus() { }

    /**
     * Gets the singleton instance of the event bus.
     * @return the GWT event bus implementation
     */
    public static synchronized EventBus get() {
        if (null == instance) {
            instance = new SimpleEventBus();
        }
        return instance;
    }

}
