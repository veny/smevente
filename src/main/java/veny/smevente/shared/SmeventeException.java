package veny.smevente.shared;

/**
 * Root of application exceptions.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
@SuppressWarnings("serial")
public class SmeventeException extends RuntimeException {

    /**
     * Exception constructor.
     */
    public SmeventeException() {
        super();
    }

    /**
     * Exception constructor.
     * @param message message
     */
    public SmeventeException(final String message) {
        super(message);
    }

    /**
     * Exception constructor.
     * @param message message
     * @param cause cause
     */
    public SmeventeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception constructor.
     * @param cause cause
     */
    public SmeventeException(final Throwable cause) {
        super(cause);
    }

}
