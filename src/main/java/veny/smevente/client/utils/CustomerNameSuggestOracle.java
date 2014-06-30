package veny.smevente.client.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.model.Customer;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Class to provide customer suggestion by name.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.6.2010
 */
public class CustomerNameSuggestOracle extends SuggestOracle {

    /** Hash of diacritics conversion which is performance consuming. */
    private static Map<String, String> replacedDiacriticsCache = new HashMap<String, String>();

    /** List of available customers. */
    private List<Customer> customers = null;

    /** {@inheritDoc} */
    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        final Response response = new Response(matching(request.getQuery(), request.getLimit()));
        callback.onSuggestionsReady(request, response);
    }

    /**
     * Creates a list of matching customers.
     * @param query client query
     * @param limit maximal count of suggestions
     * @return list of suggestions
     */
    private Collection<CustomerSuggestion> matching(final String query, final int limit) {
        final List<CustomerSuggestion> matching = new ArrayList<CustomerSuggestion>(limit);

        // only begin to search after the user has type one character
        if (query.length() >= 1) {
            String prefixToMatch = query.toLowerCase();

            for (int i = 0; i < customers.size() && matching.size() < limit; i++) {
                if (matches(customers.get(i), prefixToMatch)) {
                    matching.add(new CustomerSuggestion(customers.get(i),
                            convertToFormattedSuggestions(customers.get(i), prefixToMatch)));
                }
            }
        }

        return matching;
    }

    /**
     * Sets list of customers.
     * @param customers list of customers
     */
    public void setCustomers(final List<Customer> customers) {
        this.customers = customers;
    }

    /**
     * Removes all of the suggestions from the oracle.
     */
    public void clear() {
        if (null != customers) {
            customers.clear();
        }
    }

    /**
     * Returns real suggestions with the given query in <code>strong</code> HTML font.
     * @param customer customer
     * @param toMatch current entered prefix
     * @return real suggestions
     */
    private String convertToFormattedSuggestions(final Customer customer, final String toMatch) {
        final StringBuilder rslt = new StringBuilder();
        final String toMatchUpper = replaceDiacritics(toMatch, 1);
        final int pos = customer.getAsciiFullname().indexOf(toMatchUpper);
        final int len = toMatch.length();

        if (pos >= 0) {
            rslt.append(customer.fullname().substring(0, pos))
                .append("<strong>")
                .append(customer.fullname().substring(pos, pos + len))
                .append("</strong>")
                .append(customer.fullname().substring(pos + len));
        } else { // never should be
            rslt.append(customer.fullname());
        }

        // birth number
        rslt.append(" [")
            .append(customer.formattedBirthNumber())
            .append(']');

        return rslt.toString();
    }

    /**
     * Whether a customer matches an entered text.
     * @param customer customer
     * @param prefixToMatch text to test
     * @return <i>true</i> if matches
     */
    private boolean matches(final Customer customer, final String prefixToMatch) {
        String toMatch;
        // next 'if' block is only performance optimalization, function 'replaceDiacritics' is expensive
        if (replacedDiacriticsCache.containsKey(prefixToMatch)) {
            toMatch = replacedDiacriticsCache.get(prefixToMatch);
        } else {
            toMatch = replaceDiacritics(prefixToMatch, 1);
            replacedDiacriticsCache.put(prefixToMatch, toMatch);
        }

        return (null != customer.getAsciiFullname() && customer.getAsciiFullname().contains(toMatch));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    // ------------------------------------------------------ Native JavaScript

    /**
     * Link to original JavaScript method to remove diacritics characters from a string.
     * @param str string to be converted
     * @param mode -1 to lower case, 1 to upper case, otherwise no change
     * @return converted string
     */
    public static native String replaceDiacritics(final String str, final int mode) /*-{
        return $wnd.replaceDiacritics(str, mode);
    }-*/;

}
