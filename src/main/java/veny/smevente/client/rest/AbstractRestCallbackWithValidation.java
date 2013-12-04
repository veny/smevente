package veny.smevente.client.rest;

import veny.smevente.client.App;
import veny.smevente.shared.ExceptionJsonWrapper;
import eu.maydu.gwt.validation.client.ValidationException;


/**
 * Convenience callback class that:<ul>
 * <li>by validation exception invokes a callback method
 * <li> other problems sends to the default <code>ErrorHandler</code>.
 * </ul>
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 28.8.2010
 * @see ErrorHandler
 */
public abstract class AbstractRestCallbackWithValidation implements RestCallback {

    /** {@inheritDoc} */
    @Override
    public abstract void onSuccess(String jsonText);

    /** {@inheritDoc} */
    @Override
    public final void onFailure(final ExceptionJsonWrapper exWrapper) {
        if (exWrapper.isValidation()) {
            if (!(exWrapper.getCause() instanceof ValidationException)) {
                throw new IllegalStateException("cause is not a ValidationException");
            }
            onValidationFailure((ValidationException) exWrapper.getCause());
        } else {
            App.get().getFailureHandler().handleServerError(exWrapper);
        }
    }

    /**
     * Validation failure callback method.
     * @param ve validation exception
     */
    public abstract void onValidationFailure(ValidationException ve);

}
