package veny.smevente.client.utils;

import com.google.gwt.user.client.ui.TextBoxBase;

import eu.maydu.gwt.validation.client.ValidationAction;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * Empty Validator validates nothing.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 2.7.2010
 */
public class EmptyValidator extends Validator<EmptyValidator> {

    /**
     * Validated object, no validation performed.
     */
    private TextBoxBase text;

    /**
     * Constructor with a validated object.
     * @param validatedObject validated object, no validation performed
     */
    public EmptyValidator(final TextBoxBase validatedObject) {
        this.text = validatedObject;
    }

    /**
     * Invokes actions with the validation object.
     * @param result validation result to be passed
     */
    @Override
    public void invokeActions(final ValidationResult result) {
        for (ValidationAction<TextBoxBase> action : getFailureActions()) {
            action.invoke(result, text);
        }
    }

    /**
     * No validation performed.
     * @param messages not used
     * @param <V> the class of the ValidationMessages
     * @return null
     */
    @Override
    public <V extends ValidationMessages> ValidationResult validate(final V messages) {
        String str;
        if (text != null) {
            str = text.getText();
        } else {
            str = "";
        }

        if (str.isEmpty()) {
            String customMessage = messages.getCustomMessage("empty");
            String errorMessage = getErrorMessage(messages, customMessage);
            return new ValidationResult(errorMessage);
        }

        return null;
    }

}
