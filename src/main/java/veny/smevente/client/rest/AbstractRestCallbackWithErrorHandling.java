package veny.smevente.client.rest;

import veny.smevente.client.App;
import veny.smevente.shared.ExceptionJsonWrapper;


/**
 * Convenience callback class that sends all failures to the default
 * <code>ErrorHandler</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 * @see ErrorHandler
 */
public abstract class AbstractRestCallbackWithErrorHandling implements RestCallback {

    /** {@inheritDoc} */
    @Override
    public abstract void onSuccess(String jsonText);

    /** {@inheritDoc} */
    @Override
    public void onFailure(final ExceptionJsonWrapper exWrapper) {
        App.get().getFailureHandler().handleServerError(exWrapper);
    }

}
