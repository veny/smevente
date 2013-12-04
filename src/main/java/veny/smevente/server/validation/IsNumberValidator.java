package veny.smevente.server.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for check whether the given object represents a integer number.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class IsNumberValidator implements Validator {

    /** Validation message key. */
    public static final String MSG_KEY = "notNumber";

    /** {@inheritDoc} */
    @Override
    public boolean supports(final Class< ? > clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    /** {@inheritDoc} */
    @Override
    public void validate(final Object target, final Errors errors) {
        if (null == target) { throw new NullPointerException("validation target cannot be null"); }

        final String s = (String) target;
        try {
            Long.parseLong(s);
        } catch (NumberFormatException e) {
            errors.reject(MSG_KEY, new Object[] { }, "Not a whole number");
        }
    }

}
