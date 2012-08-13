package veny.smevente.client.utils;

import veny.smevente.client.SmsWidget;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event which will be fired
 * if a double click occurs on <code>SmsWidget</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 9.9.2010
 */
public class SmsWidgetEvent extends GwtEvent<SmsWidgetEvent.SmsWidgetHandler> {

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface SmsWidgetHandler extends EventHandler {
        /**
         * Invoked as callback if click has been occurred on widget.
         * @param event the event
         */
        void showMenu(SmsWidgetEvent event);
    }

    /**
     * For each new event, a new event type must also be specified, with which the
     * event can be registered.
     */
    public static final Type<SmsWidgetHandler> TYPE = new Type<SmsWidgetHandler>();

    /** Wrapped SMS. */
    private final SmsWidget smsWidget;


    /**
     * Constructor.
     * @param smsWidget the clicked on widget
     */
    public SmsWidgetEvent(final SmsWidget smsWidget) {
        this.smsWidget = smsWidget;
    }

    /** {@inheritDoc} */
    @Override
    public Type<SmsWidgetHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final SmsWidgetHandler handler) {
        handler.showMenu(this);
    }

    /**
     * Gets the wrapped SMS triple.
     * @return wrapped SMS triple
     */
    public SmsWidget getSmsWidget() {
        return smsWidget;
    }

}
