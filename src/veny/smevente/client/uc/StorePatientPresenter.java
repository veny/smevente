package veny.smevente.client.uc;

import java.util.HashMap;
import java.util.Map;

import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithValidation;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.model.Patient;
import veny.smevente.shared.EntityTypeEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.TextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;
import eu.maydu.gwt.validation.client.validators.strings.StringLengthValidator;

/**
 * Add Patient presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class StorePatientPresenter
    extends AbstractPresenter<StorePatientPresenter.StorePatientView>
    implements HeaderHandler {

    /**
     * View interface for the Add Patient.
     *
     * @author Vaclav Sykora
     * @since 22.8.2010
     */
    public interface StorePatientView extends View {
        /**
         * Getter for the first name text field.
         * @return the input field for the first name
         */
        TextBox getFirstname();
        /**
         * Getter for the surname text field.
         * @return the input field for the surname
         */
        TextBox getSurname();
        /**
         * Getter for the phone number text field.
         * @return the input field for the phone number
         */
        TextBox getPhoneNumber();
        /**
         * Getter for the birth number text field.
         * @return the input field for the birth number
         */
        TextBox getBirthNumber();
        /**
         * Getter for the degree text field.
         * @return the input field for the degree
         */
        TextBox getDegree();
        /**
         * Getter for the street text field.
         * @return the input field for the street
         */
        TextBox getStreet();
        /**
         * Getter for the city text field.
         * @return the input field for the city
         */
        TextBox getCity();
        /**
         * Getter for the zip code text field.
         * @return the input field for the zip code
         */
        TextBox getZipCode();
        /**
         * Getter for the employer text field.
         * @return the input field for the employer
         */
        TextBox getEmployer();
        /**
         * Getter for the careers text field.
         * @return the input field for the careers
         */
        TextBox getCareers();
        /**
         * Getter for the button to submit.
         * @return the submit element
         */
        HasClickHandlers getSubmit();
        /**
         * Getter for the button to cancel.
         * @return the submit element
         */
        HasClickHandlers getCancel();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
        /**
         * @return the hidden field wit patient ID
         */
        Hidden getPatientId();
    }

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    // -------------------------------------------------- HeaderHandler Methods

    /** {@inheritDoc} */
    @Override
    public void unitChanged(final HeaderEvent event) {
        clean();
    }
    /** {@inheritDoc} */
    @Override
    public void unitMemberChanged(final HeaderEvent event) { /* I don't care */ }
    /** {@inheritDoc} */
    @Override
    public void dateChanged(final HeaderEvent event) { /* I don't care */ }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        // register this to display/hide the loading progress bar
        ebusUnitSelection = eventBus.addHandler(HeaderEvent.TYPE, this);

        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                // validation
                if (!validator.validate()) {
                    // One (or more) validations failed. The actions will have been
                    // already invoked by the ...validate() call.
                    return;
                }
                storePatient();
            }
        });

        view.getCancel().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().switchToPresenterByType(PresenterEnum.FIND_PATIENT, null);
            }
        });

        // VALIDATION
        setupValidation();
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        ebusUnitSelection.removeHandler();
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object parameter) {
        view.getFirstname().setFocus(true);

        if (null != parameter && parameter instanceof Patient) {
            final Patient p = (Patient) parameter;
            view.getPatientId().setValue(p.getId().toString());
            view.getFirstname().setText(p.getFirstname());
            view.getSurname().setText(p.getSurname());
            view.getPhoneNumber().setText(p.getPhoneNumber());
            view.getBirthNumber().setText(p.getBirthNumber());
            view.getDegree().setText(p.getDegree());
            view.getStreet().setText(p.getStreet());
            view.getCity().setText(p.getCity());
            view.getZipCode().setText(p.getZipCode());
            view.getEmployer().setText(p.getEmployer());
            view.getCareers().setText(p.getCareers());
        } else {
            // Using a null as argument on IE7 will lead to the setting of
            // string "null" as value, therefore the empty string is used instead.
            view.getPatientId().setValue("");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // Using a null as argument on IE7 will lead to the setting of
        // string "null" as value, therefore the empty string is used instead.
        view.getPatientId().setValue("");
        view.getFirstname().setText("");
        view.getSurname().setText("");
        view.getPhoneNumber().setText("");
        view.getBirthNumber().setText("");
        view.getDegree().setText("");
        view.getStreet().setText("");
        view.getCity().setText("");
        view.getZipCode().setText("");
        view.getEmployer().setText("");
        view.getCareers().setText("");

        // validation
        validator.reset((String[]) null);
        view.getFirstname().removeStyleName("validationFailedBorder");
        view.getSurname().removeStyleName("validationFailedBorder");
        view.getPhoneNumber().removeStyleName("validationFailedBorder");
        view.getBirthNumber().removeStyleName("validationFailedBorder");
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Sets up validation for the presenter.
     */
    private void setupValidation() {
        // validation messages
        ValidationMessages vmess = new ValidationMessages() {
            @Override
            public String getCustomMessage(final String key, final Object... parameters) {
                return getValidationMessage(key, parameters);
            }
            @Override
            public String getPropertyName(final String propertyName) {
                String result;
                if ("firstname".equals(propertyName)) {
                    result = CONSTANTS.firstname();
                } else if ("surname".equals(propertyName)) {
                    result = CONSTANTS.surname();
                } else if ("phoneNumber".equals(propertyName)) {
                    result = CONSTANTS.phoneNumber();
                } else if ("birthNumber".equals(propertyName)) {
                    result = CONSTANTS.birthNumber();
                } else { result = super.getPropertyName(propertyName); }
                return result;
            }
        };
        validator = new DefaultValidationProcessor(vmess);
        // add DisclosurePanel for validation messages
        validator.addGlobalAction(new DisclosureTextAction(view.getValidationErrors(), "redText"));

        final FocusAction focusAction = new FocusAction();

        validator.addValidators("firstname",
                new EmptyValidator(view.getFirstname())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("surname",
                new EmptyValidator(view.getSurname())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        // phone number
        validator.addValidators("phoneNumber",
                new StringLengthValidator(view.getPhoneNumber(), 9, 20, false, "textLength")
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        // birth number
        validator.addValidators("birthNumber",
                new StringLengthValidator(view.getBirthNumber(), 0, 10, false, "textLength")
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

    /**
     * Creates a new patient.
     */
    private void storePatient() {
        final Patient p = new Patient();
        if (null == view.getPatientId().getValue() || view.getPatientId().getValue().trim().isEmpty()) {
            p.setId(null);
        } else {
            p.setId(Long.parseLong(view.getPatientId().getValue()));
        }
        p.setUnit(App.get().getSelectedUnit());
        p.setFirstname(view.getFirstname().getText());
        p.setSurname(view.getSurname().getText());
        p.setPhoneNumber(view.getPhoneNumber().getText());
        p.setBirthNumber(view.getBirthNumber().getText());
        p.setDegree(view.getDegree().getText());
        p.setStreet(view.getStreet().getText());
        p.setCity(view.getCity().getText());
        p.setZipCode(view.getZipCode().getText());
        p.setEmployer(view.getEmployer().getText());
        p.setCareers(view.getCareers().getText());

        final Map<String, String> params = new HashMap<String, String>();
        params.put("unitId", p.getUnit().getId().toString());
        params.put("firstname", p.getFirstname());
        params.put("surname", p.getSurname());
        params.put("phoneNumber", p.getPhoneNumber());
        params.put("birthNumber", p.getBirthNumber());
        params.put("degree", p.getDegree());
        params.put("street", p.getStreet());
        params.put("city", p.getCity());
        params.put("zipCode", p.getZipCode());
        params.put("employer", p.getEmployer());
        params.put("careers", p.getCareers());
        if (null != p.getId()) { params.put("id", p.getId().toString()); }

        final RestHandler rest = new RestHandler("/rest/unit/patient/");
        rest.setCallback(new AbstractRestCallbackWithValidation() {
            @Override
            public void onSuccess(final String jsonText) {
                if (null == p.getId()) {
                    final Patient patient = App.get().getJsonDeserializer().deserialize(
                            Patient.class, "patient", jsonText);
                    eventBus.fireEvent(new CrudEvent(EntityTypeEnum.PATIENT, OperationType.CREATE, patient));
                    Window.alert(CONSTANTS.patientAdded()[App.get().getSelectedUnitTextVariant()]);
                } else {
                    eventBus.fireEvent(new CrudEvent(EntityTypeEnum.PATIENT, OperationType.UPDATE, p));
                    Window.alert(CONSTANTS.patientUpdated()[App.get().getSelectedUnitTextVariant()]);
                }
            }
            @Override
            public void onValidationFailure(final ValidationException ve) {
                validator.processServerErrors(ve);
            }
        });

        if (null == p.getId()) {
            rest.post(params);
        } else {
            rest.put(params);
        }
    }

}
