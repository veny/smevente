package veny.smevente.server.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Minimal String Length.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class StringLengthValidator implements Validator {

    /** Validation message key. */
    public static final String MSG_KEY = "textLength";

    /** Minimal length validator parameter. */
    private int minimalLength = -1;
    /** Maximal length validator parameter. */
    private int maximalLength = -1;

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
        final int len = s.length();
        if (minimalLength >= 0 && len < getMinimalLength()) {
            errors.reject(MSG_KEY + "Small", new Object[] { minimalLength, len },
                    "Needs a length lesser than " + minimalLength + " but is " + len);
        }
        if (maximalLength >= 0 && len > getMaximalLength()) {
            errors.reject(MSG_KEY + "Big", new Object[] { maximalLength, len },
                    "Needs a length bigger than " + maximalLength + " but is " + len);
        }
    }

    /**
     * Sets the minimal length.
     * @param minimalLength minimal length
     */
    public void setMinimalLength(final int minimalLength) {
        this.minimalLength = minimalLength;
    }
    /**
     * Gets the minimal length.
     * @return minimal length
     */
    public int getMinimalLength() {
        return minimalLength;
    }

    /**
     * Sets the maximal length.
     * @param maximalLength maximal length
     */
    public void setMaximalLength(final int maximalLength) {
        this.maximalLength = maximalLength;
    }
    /**
     * Gets the maximal length.
     * @return maximal length
     */
    public int getMaximalLength() {
        return maximalLength;
    }

}
