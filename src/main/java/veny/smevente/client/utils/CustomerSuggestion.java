package veny.smevente.client.utils;

import veny.smevente.model.Customer;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;

/**
 * Suggestion wrapper for <code>Customer</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.6.2010
 */
public class CustomerSuggestion extends MultiWordSuggestion {

    /** Wrapped customer. */
    private Customer customer;

    /**
     * Constructor.
     * @param customer customer to be wrapped
     * @param formattedSuggestions formatted suggestion
     */
    public CustomerSuggestion(final Customer customer, final String formattedSuggestions) {
        super(customer.fullname(), formattedSuggestions);
        this.customer = customer;
    }

    /**
     * Gets wrapped customer.
     * @return customer
     */
    public Customer getCustomer() {
        return customer;
    }

}
