package veny.smevente.client.utils;

import java.util.Date;

import veny.smevente.model.UnitDto;
import veny.smevente.model.UserDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event which will be fire
 * if the unit is changed in header drop down.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 6.7.2010
 */
public class HeaderEvent extends GwtEvent<HeaderEvent.HeaderHandler> {

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface HeaderHandler extends EventHandler {
        /**
         * Invoked as callback if the unit has been changed.
         * @param event the event
         */
        void unitChanged(HeaderEvent event);
        /**
         * Invoked as callback if the unit member has been changed.
         * @param event the event
         */
        void unitMemberChanged(HeaderEvent event);
        /**
         * Invoked as callback if the date has been changed.
         * @param event the event
         */
        void dateChanged(HeaderEvent event);
    }

    /**
     * For each new event, a new event type must also be specified, with which the
     * event can be registered.
     */
    public static final Type<HeaderHandler> TYPE = new Type<HeaderHandler>();

    /** The selected unit. */
    private UnitDto unit;
    /** The selected unit member. */
    private UserDto unitMember;
    /** The selected date. */
    private Date date;

    /**
     * Constructor.
     * @param unit selected unit
     */
    public HeaderEvent(final UnitDto unit) {
        this.unit = unit;
    }
    /**
     * Constructor.
     * @param unitMember selected unit member
     */
    public HeaderEvent(final UserDto unitMember) {
        this.unitMember = unitMember;
    }
    /**
     * Constructor.
     * @param date selected date
     */
    public HeaderEvent(final Date date) {
        this.date = date;
    }

    /** {@inheritDoc} */
    @Override
    public Type<HeaderHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final HeaderHandler handler) {
        if (null != unit) {
            handler.unitChanged(this);
        } else if (null != unitMember) {
            handler.unitMemberChanged(this);
        } else if (null != date) {
            handler.dateChanged(this);
        } else {
            throw new IllegalStateException("unit, unitMember, selectedDate are null");
        }
    }

    /**
     * Gets selected unit.
     * @return selected unit
     */
    public UnitDto getUnit() {
        return unit;
    }
    /**
     * Gets selected unit member.
     * @return selected unit member
     */
    public UserDto getUnitMember() {
        return unitMember;
    }
    /**
     * Gets selected date.
     * @return selected date
     */
    public Date getDate() {
        return date;
    }

}
