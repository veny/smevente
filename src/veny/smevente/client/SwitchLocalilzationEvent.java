package veny.smevente.client;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event which will be fired when must be the localization changed.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public class SwitchLocalilzationEvent extends GwtEvent<SwitchLocalilzationEvent.SwitchLocalilzationEventHandler> {

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface SwitchLocalilzationEventHandler extends EventHandler {
        /**
         * What to do when the switch localization response has been arrived.
         * @param event the event
         */
        void switchLocalization(SwitchLocalilzationEvent event);
    }

    /**
     * For each new event, a new event type must also be specified,
     * with which the event can be registered.
     */
    public static final Type<SwitchLocalilzationEventHandler> TYPE = new Type<SwitchLocalilzationEventHandler>();

    /** Localization. */
    private String localization;


    /**
     * Constructor.
     * @param newLocalization localization
     */
    public SwitchLocalilzationEvent(final String newLocalization) {
        this.localization = newLocalization;
    }

    /**
     * Getter for the localization.
     * @return localization
     */
    public String getLocalization() {
        return localization;
    }

    /** {@inheritDoc} */
    @Override
    public Type<SwitchLocalilzationEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final SwitchLocalilzationEventHandler handler) {
        handler.switchLocalization(this);
    }

}
