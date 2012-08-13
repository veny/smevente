package veny.smevente.server.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * String Regular Expression Validator.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class RegularExpressionValidator implements Validator {

    /** Validation message key. */
    public static final String MSG_KEY = "regexpValidator";

    /**
     * Regular Expression Validator parameter.
     */
    private String regularExpression;

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
        if (!s.matches(getRegularExpression())) {
            errors.reject(MSG_KEY, new Object[] { regularExpression },
                    "The input does not match the given regular expression");
        }
    }

    /**
     * Gets the regular expression.
     * @param regularExpression the regular expression
     */
    public void setRegularExpression(final String regularExpression) {
        this.regularExpression = regularExpression;
    }

    /**
     * Sets the regular expression.
     * @return the regular expression
     */
    public String getRegularExpression() {
        return regularExpression;
    }

}
