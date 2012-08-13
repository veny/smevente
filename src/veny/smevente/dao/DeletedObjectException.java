package veny.smevente.dao;

import veny.smevente.shared.SmeventeException;

/**
 * Exception to be thrown if an object with life cycle with state 'DELETED'
 * is found by ID.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.4.2011
 */
@SuppressWarnings("serial")
public class DeletedObjectException extends SmeventeException {

    /**
     * Exception constructor.
     * @param message message
     */
    public DeletedObjectException(final String message) {
        super(message);
    }

}
