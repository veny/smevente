package veny.smevente.client.utils;

import veny.smevente.model.Patient;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;

/**
 * Suggestion wrapper for <code>Patient</code>.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 18.6.2010
 */
public class PatientSuggestion extends MultiWordSuggestion {

    /** Wrapped patient. */
    private Patient patient;

    /**
     * Constructor.
     * @param patient patient to be wrapped
     * @param formattedSuggestions formatted suggestion
     */
    public PatientSuggestion(final Patient patient, final String formattedSuggestions) {
        super(patient.getFirstname() + " " + patient.getSurname(), formattedSuggestions);
        this.patient = patient;
    }

    /**
     * Gets wrapped patient.
     * @return patient
     */
    public Patient getPatient() {
        return patient;
    }

}
