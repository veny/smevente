package veny.smevente.client.utils;

import veny.smevente.model.AbstractEntity;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * This class represents an event to be fired if some data has been manipulated
 * (CREATE, READ, UPDATE, DELETE) and an other presenter can be interested on such info.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 21.12.2010
 */
public class CrudEvent extends GwtEvent<CrudEvent.CrudEventHandler> {

    /**
     * Enumeration defining the operations proceeded on the date before this event was fired.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 21.12.2010
     */
    public enum OperationType {
        /** Create. */
        CREATE,
        /** Read. */
        READ,
        /** Update. */
        UPDATE,
        /** Delete. */
        DELETE,
    }

    /**
     * Associated <code>EventHandler</code> interface.
     */
    public interface CrudEventHandler extends EventHandler {
        /**
         * Callback to handle event that the data was created.
         * @param event the event wrapped data
         */
        void create(CrudEvent event);
        /**
         * Callback to handle event that the data was read.
         * @param event the event wrapped data
         */
        void read(CrudEvent event);
        /**
         * Callback to handle event that the data was updated.
         * @param event the event wrapped data
         */
        void update(CrudEvent event);
        /**
         * Callback to handle event that the data was deleted.
         * @param event the event wrapped data
         */
        void delete(CrudEvent event);
    }

    /**
     * For each new event, a new event type must also be specified, with which the event can be registered.
     */
    public static final Type<CrudEventHandler> TYPE = new Type<CrudEventHandler>();


    /** Type of operation. */
    private final OperationType operationType;
    /** Data of the operation. */
    private final AbstractEntity data;


    /**
     * Constructor.
     * @param operationType type of operation
     * @param data data of the operation
     */
    public CrudEvent(final OperationType operationType, final AbstractEntity data) {
        if (null == operationType) { throw new NullPointerException("operation type cannot be null"); }
        if (null == data) { throw new NullPointerException("data cannot be null"); }

        this.operationType = operationType;
        this.data = data;
    }

    /** {@inheritDoc} */
    @Override
    public Type<CrudEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final CrudEventHandler handler) {
        switch (operationType) {
            case CREATE:
                handler.create(this);
                break;
            case READ:
                handler.read(this);
                break;
            case UPDATE:
                handler.update(this);
                break;
            case DELETE:
                handler.delete(this);
                break;
            default:
                throw new IllegalArgumentException("unknown operation type: " + operationType);
        }
    }

    /**
     * Gets data of the operation.
     * @return data of the operation
     */
    public AbstractEntity getData() {
        return data;
    }

}
