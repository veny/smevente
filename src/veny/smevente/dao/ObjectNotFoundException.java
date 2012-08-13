package veny.smevente.dao;

import veny.smevente.shared.SmeventeException;

/**
 * Object Not Found Exception to be used across all service layers.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
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

}
