package veny.smevente.client.rest;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event which will be fired if an AJAX request/response
 * will be sent/received.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class AjaxEvent extends GwtEvent<AjaxEvent.AjaxEventHandler> {

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface AjaxEventHandler extends EventHandler {
        /**
         * Invoked as callback if the request has been sent.
         * @param event the event
         */
        void requestSent(AjaxEvent event);
        /**
         * Invoked as callback if the response has been received.
         * @param event the event
         */
        void responseReceived(AjaxEvent event);
    }

    /**
     * For each new event, a new event type must also be specified, with which the
     * event can be registered.
     */
    public static final Type<AjaxEventHandler> TYPE = new Type<AjaxEventHandler>();

    /**
     * Sorts of the <code>AjaxEvent</code>.
     * @author Vaclav Sykora
     * @since 0.1
     */
    public enum Sort {
        /**
         * Request sent.
         */
        REQUEST_SENT,
        /**
         * Response received.
         */
        RESPONSE_RECEIVED
    }

    /** Sort of the event. */
    private Sort sort;

    /**
     * Constructor.
     * @param sort the sort of the event
     */
    public AjaxEvent(final Sort sort) {
        this.sort = sort;
    }

    /** {@inheritDoc} */
    @Override
    public Type<AjaxEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final AjaxEventHandler handler) {
        switch (sort) {
            case REQUEST_SENT:
                handler.requestSent(this);
                break;
            case RESPONSE_RECEIVED:
                handler.responseReceived(this);
                break;
            default:
                throw new IllegalStateException("bad AjaxEvent sort, sort=" + sort);
        }
    }

    /**
     * Gets sort of the event.
     * @return the event sort
     */
    public Sort getSort() {
        return sort;
    }

}
