package veny.smevente.client;

import veny.smevente.shared.ExceptionJsonWrapper;

/**
 * This interface defines functionality of a client side error handler.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public interface FailureHandler {

    /**
     * Handles a exception which has been occurred on the client side.
     *
     * @param th the exception
     * @param detail the detailed description
     */
    void handleClientError(Throwable th, String detail);

    /**
     * Handles a exception which has been occurred on the client side
     * with some more detailed action description.
     *
     * @param th the exception
     * @param action action description
     * @param detail the detailed description
     */
    void handleClientErrorWithAction(Throwable th, String action, String detail);

    /**
     * Handles a exception which has been occurred on the server side.
     *
     * @param th the exception
     */
    void handleServerError(ExceptionJsonWrapper th);

}
