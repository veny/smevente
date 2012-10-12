package veny.smevente.client.utils;

import veny.smevente.client.EventWidget;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event which will be fired
 * if a double click occurs on <code>EventWidget</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 9.9.2010
 */
public class EventWidgetEvent extends GwtEvent<EventWidgetEvent.EventWidgetHandler> {

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface EventWidgetHandler extends EventHandler {
        /**
         * Invoked as callback if click has been occurred on widget.
         * @param event the event
         */
        void showMenu(EventWidgetEvent event);
    }

    /**
     * For each new event, a new event type must also be specified, with which the
     * event can be registered.
     */
    public static final Type<EventWidgetHandler> TYPE = new Type<EventWidgetHandler>();

    /** Wrapped event widget. */
    private final EventWidget eventWidget;


    /**
     * Constructor.
     * @param eventWidget the clicked on widget
     */
    public EventWidgetEvent(final EventWidget eventWidget) {
        this.eventWidget = eventWidget;
    }

    /** {@inheritDoc} */
    @Override
    public Type<EventWidgetHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final EventWidgetHandler handler) {
        handler.showMenu(this);
    }

    /**
     * Gets the wrapped event widget.
     * @return wrapped event widget
     */
    public EventWidget getEventWidget() {
        return eventWidget;
    }

}
