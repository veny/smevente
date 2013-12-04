package veny.smevente.client.rest;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event which will be fired if the unauthorized response (401)
 * will arrive from server.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public class UnauthorizedEvent extends GwtEvent<UnauthorizedEvent.UnauthorizedEventHandler> {

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface UnauthorizedEventHandler extends EventHandler {
        /**
         * What to do when the unauthorized response (401) has been arrived.
         * @param event the event
         */
        void unauthorized(UnauthorizedEvent event);
    }

    /**
     *    For each new event, a new event type must also be specified,
     *    with which the event can be registered.
     */
    public static final Type<UnauthorizedEventHandler> TYPE = new Type<UnauthorizedEventHandler>();


    /**
     * Constructor.
     */
    public UnauthorizedEvent() {
    }

    /** {@inheritDoc} */
    @Override
    public Type<UnauthorizedEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final UnauthorizedEventHandler handler) {
        handler.unauthorized(this);
    }

}
