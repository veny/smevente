package veny.smevente.server.validation;

import java.util.List;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.server.ServerValidation;

/**
 * The validation chain implementation.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class ValidationChain {

    /**
     * The chain to be used for the validation.
     */
    private List<Validator> chain;

    /**
     * Sets validators to the validation chain.
     *
     * @param validatorList the validators to be added to the chain
     */
    public void setValidators(final List<Validator> validatorList) {
        chain = validatorList;
    }

    /**
     * Validates to validatedObject.
     * @param target the object to be validated
     * @param propertyName the name of the validated property
     * @throws ValidationException if validation failed
     */
    public void validate(final Object target, final  String propertyName) throws ValidationException {
        final Errors errors = new BeanPropertyBindingResult(target, "VALIDATED_OBJECT");

        try {
            for (Validator v : chain) {
                v.validate(target, errors);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (errors.hasErrors()) {
            ObjectError objectError = errors.getAllErrors().get(0);
            String errorCode = objectError.getCode();
            ServerValidation.exception(errorCode, propertyName, objectError.getArguments());
        }
    }

}
