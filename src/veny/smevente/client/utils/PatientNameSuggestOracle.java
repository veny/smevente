package veny.smevente.client.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import veny.smevente.model.Patient;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Class to provide patient suggestion by name.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.6.2010
 */
public class PatientNameSuggestOracle extends SuggestOracle {

    /** List of available patients. */
    private List<Patient> patients = null;

    /** {@inheritDoc} */
    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        final Response response = new Response(matching(request.getQuery(), request.getLimit()));
        callback.onSuggestionsReady(request, response);
    }

    /**
     * Creates a list of matching patients.
     * @param query client query
     * @param limit maximal count of suggestions
     * @return list of suggestions
     */
    private Collection<PatientSuggestion> matching(final String query, final int limit) {
        final List<PatientSuggestion> matching = new ArrayList<PatientSuggestion>(limit);

        // only begin to search after the user has type one character
        if (query.length() >= 1) {
            String prefixToMatch = query.toLowerCase();

            for (int i = 0; i < patients.size() && matching.size() < limit; i++) {
                if (matches(patients.get(i), prefixToMatch)) {
                    matching.add(new PatientSuggestion(patients.get(i),
                            convertToFormattedSuggestions(patients.get(i), prefixToMatch)));
                }
            }
        }

        return matching;
    }

    /**
     * Sets list of patients.
     * @param patients list of patients
     */
    public void setPatients(final List<Patient> patients) {
        this.patients = patients;
    }

    /**
     * Removes all of the suggestions from the oracle.
     */
    public void clear() {
        if (null != patients) {
            patients.clear();
        }
    }

    /**
     * Returns real suggestions with the given query in <code>strong</code> HTML font.
     * @param patient patient
     * @param prefixToMatch current entered prefix
     * @return real suggestions
     */
    private String convertToFormattedSuggestions(final Patient patient, final String prefixToMatch) {
        final StringBuilder rslt = new StringBuilder();

        // search in form "firstname surname"
        if (prefixToMatch.contains(" ")) {
            final int len = prefixToMatch.substring(prefixToMatch.lastIndexOf(' ') + 1).length();
            rslt.append("<strong>");
            rslt.append(patient.getFirstname());
            rslt.append(' ');
            rslt.append(patient.getSurname().substring(0, len));
            rslt.append("</strong>");
            rslt.append(patient.getSurname().substring(len));

        } else {

            // search without space -> check firstname or surname
            final int len = prefixToMatch.length();
            if (patient.getFirstname().toLowerCase().startsWith(prefixToMatch)) {
                rslt.append("<strong>");
                rslt.append(patient.getFirstname().substring(0, len));
                rslt.append("</strong>");
                rslt.append(patient.getFirstname().substring(len));
            } else {
                rslt.append(patient.getFirstname());
            }
            rslt.append(' ');
            if (patient.getSurname().toLowerCase().startsWith(prefixToMatch)) {
                rslt.append("<strong>");
                rslt.append(patient.getSurname().substring(0, len));
                rslt.append("</strong>");
                rslt.append(patient.getSurname().substring(len));
            } else {
                rslt.append(patient.getSurname());
            }
        }

        // birth number
        rslt.append(" [")
            .append(patient.formattedBirthNumber())
            .append(']');

        return rslt.toString();
    }

    /**
     * Whether a patient matches an entered text.
     * @param patient patient
     * @param prefixToMatch text to test
     * @return <i>true</i> if matches
     */
    private boolean matches(final Patient patient, final String prefixToMatch) {
        // search in form "firstname surname"
        if (prefixToMatch.contains(" ")) {
            final String firstname = prefixToMatch.substring(0, prefixToMatch.indexOf(' '));
            final String surname = prefixToMatch.substring(prefixToMatch.lastIndexOf(' ') + 1);
            return patient.getFirstname().toLowerCase().equals(firstname)
                && patient.getSurname().toLowerCase().startsWith(surname);
        }

        // search without space -> check firstname or surname
        return patient.getFirstname().toLowerCase().startsWith(prefixToMatch)
                || patient.getSurname().toLowerCase().startsWith(prefixToMatch);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

}
