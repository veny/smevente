package veny.smevente.dao;

import veny.smevente.shared.SmeventeException;

/**
 * Object Not Found Exception to be used across all service layers.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 01.09.2012
 */
@SuppressWarnings("serial")
public class ObjectNotFoundException extends SmeventeException {

    /**
     * Exception constructor.
     * @param message message
     */
    public ObjectNotFoundException(final String message) {
        super(message);
    }

    /**
     * Exception constructor.
     * @param message message
     * @param cause cause
     */
    public ObjectNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
