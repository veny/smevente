package veny.smevente.client.rest;

import veny.smevente.shared.ExceptionJsonWrapper;


/**
 * Interface for <code>ClientRestHandler</code> callback.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public interface RestCallback {

    /**
     * Success callback method.
     * @param jsonText JSON text value from the server
     */
    void onSuccess(String jsonText);

    /**
     * Failure callback method.
     * @param exceptionJsonWrapper wrapper of exception thrown on the server
     */
    void onFailure(ExceptionJsonWrapper exceptionJsonWrapper);

}
