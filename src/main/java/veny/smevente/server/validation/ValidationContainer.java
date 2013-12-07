package veny.smevente.server.validation;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.maydu.gwt.validation.client.InvalidValueSerializable;
import eu.maydu.gwt.validation.client.ValidationException;

/**
 * Container of validation chains.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class ValidationContainer {

    /**
     * The validator cache.
     */
    private Map<String, ValidationChain> validatorChains;

    /**
     * Validates object using the validationChainName.
     *
     * @param validationChainName the validationChain to validate
     * @param propertyName the name of the validated property
     * @param target the object to be validated
     */
    public void validate(final String validationChainName, final String propertyName, final Object target) {
        if (!validatorChains.containsKey(validationChainName)) {
            throw new IllegalArgumentException("validation chain not found, name=" + validationChainName);
        }

        validatorChains.get(validationChainName).validate(target, propertyName);
    }

    /**
     * Sets the validator chains.
     * @param validatorChains validator chains to set
     */
    public void setValidatorChains(final Map<String, ValidationChain> validatorChains) {
        this.validatorChains = validatorChains;
    }

    /**
     * @return the validatorChains
     */
    public Map<String, ValidationChain> getValidatorChains() {
        return validatorChains;
    }


    /**
     * For testing purposes.
     * @param args CLI arguments
     */
    public static void main(final String[] args) {
        final ApplicationContext actx = new ClassPathXmlApplicationContext(
                new String[] { "appctx-validation.xml" });

        final ValidationContainer vc = (ValidationContainer) actx.getBean("validationContainer");
        try {
            vc.validate("birthNumber", "PN", "010101203x");
        } catch (ValidationException e) {
            final InvalidValueSerializable iv = e.getInvalidValues().get(0);
            System.out.println("validation failed, msgKey=" + iv.getMessage() //CSOFF
                    + ", propertyName=" + iv.getPropertyName());
        }
    }


}
