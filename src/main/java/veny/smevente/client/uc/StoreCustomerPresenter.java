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
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Unit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;
import eu.maydu.gwt.validation.client.validators.strings.StringLengthValidator;

/**
 * Store customer presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class StoreCustomerPresenter
    extends AbstractPresenter<StoreCustomerPresenter.StoreCustomerView>
    implements HeaderHandler {

    /**
     * View interface for the storing customer.
     *
     * @author Vaclav Sykora
     * @since 22.8.2010
     */
    public interface StoreCustomerView extends View {
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
         * Getter for the email text field.
         * @return the input field for email
         */
        TextBox getEmail();
        /**
         * Getter for the email channel check box.
         * @return email channel check box
         */
        CheckBox getEmailChannel();
        /**
         * Getter for the phone number text field.
         * @return the input field for the phone number
         */
        TextBox getPhoneNumber();
        /**
         * Getter for the SMS channel check box.
         * @return SMS channel check box
         */
        CheckBox getSmsChannel();
        /**
         * Getter for the SMS channel help image.
         * @return SMS channel help image
         */
        Image getSmsChannelHelp();
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
         * @return the hidden field wit customer ID
         */
        Hidden getCustomerId();
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
                if (!view.getSmsChannel().getValue() && !view.getEmailChannel().getValue()) {
                    if (!Window.confirm(CONSTANTS.customerWithoutChannel()[App.get().getSelectedUnitTextVariant()])) {
                        return;
                    }
                }
                storeCustomer();
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
        final Unit currentUnit = App.get().getSelectedUnit();

        view.getSmsChannel().setEnabled(currentUnit.isSmsEnabled());
        if (!currentUnit.isSmsEnabled()) {
            view.getSmsChannelHelp().setTitle(CONSTANTS.smsChannel() + "\n(" + CONSTANTS.premiumService() + ")");
        }

        if (null != parameter && parameter instanceof Customer) {
            final Customer p = (Customer) parameter;
            view.getCustomerId().setValue(p.getId().toString());
            view.getFirstname().setText(p.getFirstname());
            view.getSurname().setText(p.getSurname());
            view.getPhoneNumber().setText(p.getPhoneNumber());
            view.getSmsChannel().setValue((p.getSendingChannel() & veny.smevente.model.Event.CHANNEL_SMS) > 0);
            view.getEmail().setText(p.getEmail());
            view.getEmailChannel().setValue((p.getSendingChannel() & veny.smevente.model.Event.CHANNEL_EMAIL) > 0);
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
            view.getCustomerId().setValue("");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // Using a null as argument on IE7 will lead to the setting of
        // string "null" as value, therefore the empty string is used instead.
        view.getCustomerId().setValue("");
        view.getFirstname().setText("");
        view.getSurname().setText("");
        view.getPhoneNumber().setText("");
        view.getSmsChannel().setValue(false);
        view.getSmsChannelHelp().setTitle(CONSTANTS.smsChannel());
        view.getEmail().setText("");
        view.getEmailChannel().setValue(false);
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
            @Override public String getCustomMessage(final String key, final Object... parameters) {
                return getValidationMessage(key, parameters);
            }
            @Override public String getPropertyName(final String propertyName) {
                String result;
                if ("firstname".equals(propertyName)) {
                    result = CONSTANTS.firstname();
                } else if ("surname".equals(propertyName)) {
                    result = CONSTANTS.surname();
                } else if ("email".equals(propertyName)) {
                    result = CONSTANTS.email();
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
        // email
        validator.addValidators("email", new Validator<Object>() {
            @Override
            public void invokeActions(final ValidationResult result) {
                getView().getEmail().setFocus(true);
                getView().getEmail().addStyleName("validationFailedBorder");
            }
            @Override
            public ValidationResult validate(final ValidationMessages messages) {
                // remove result of previous failed validation if any
                getView().getEmail().removeStyleName("validationFailedBorder");

                ValidationResult rslt = null;
                if (getView().getEmailChannel().getValue() && 0 == getView().getEmail().getText().trim().length()) {
                    rslt = new ValidationResult(CONSTANTS.validationEmptyIfCheckboxSelected());
                }
                return rslt;
            }
        });
        // phone number
        validator.addValidators("phoneNumber", new Validator<Object>() {
            @Override
            public void invokeActions(final ValidationResult result) {
                getView().getPhoneNumber().setFocus(true);
                getView().getPhoneNumber().addStyleName("validationFailedBorder");
            }
            @Override
            public ValidationResult validate(final ValidationMessages messages) {
                // remove result of previous failed validation if any
                getView().getPhoneNumber().removeStyleName("validationFailedBorder");

                ValidationResult rslt = null;
                if (getView().getSmsChannel().getValue() && 0 == getView().getPhoneNumber().getText().trim().length()) {
                    rslt = new ValidationResult(CONSTANTS.validationEmptyIfCheckboxSelected());
                }
                return rslt;
            }
        });
        // birth number
        validator.addValidators("birthNumber",
                new StringLengthValidator(view.getBirthNumber(), 0, 10, false, "textLength")
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

    /**
     * Stores customer.
     */
    private void storeCustomer() {
        final Customer c = new Customer();
        if (null == view.getCustomerId().getValue() || view.getCustomerId().getValue().trim().isEmpty()) {
            c.setId(null);
        } else {
            c.setId(view.getCustomerId().getValue());
        }
        c.setUnit(App.get().getSelectedUnit());
        c.setFirstname(view.getFirstname().getText());
        c.setSurname(view.getSurname().getText());
        c.setPhoneNumber(view.getPhoneNumber().getText());
        c.setEmail(view.getEmail().getText());
        c.setBirthNumber(view.getBirthNumber().getText());
        c.setDegree(view.getDegree().getText());
        c.setStreet(view.getStreet().getText());
        c.setCity(view.getCity().getText());
        c.setZipCode(view.getZipCode().getText());
        c.setEmployer(view.getEmployer().getText());
        c.setCareers(view.getCareers().getText());

        final Map<String, String> params = new HashMap<String, String>();
        params.put("unitId", c.getUnit().getId().toString());
        params.put("firstname", c.getFirstname());
        params.put("surname", c.getSurname());
        params.put("phoneNumber", c.getPhoneNumber());
        params.put("email", c.getEmail());
        params.put("birthNumber", c.getBirthNumber());
        params.put("degree", c.getDegree());
        params.put("street", c.getStreet());
        params.put("city", c.getCity());
        params.put("zipCode", c.getZipCode());
        params.put("employer", c.getEmployer());
        params.put("careers", c.getCareers());
        int sendingChannel = 0;
        if (view.getEmailChannel().getValue()) { sendingChannel |= Event.CHANNEL_EMAIL; }
        if (view.getSmsChannel().getValue()) { sendingChannel |= Event.CHANNEL_SMS; }
        params.put("sendingChannel", Integer.toString(sendingChannel));
        if (null != c.getId()) { params.put("id", c.getId().toString()); }

        final RestHandler rest = new RestHandler("/rest/unit/customer/");
        rest.setCallback(new AbstractRestCallbackWithValidation() {
            @Override
            public void onSuccess(final String jsonText) {
                c.asciiFullname(); // BF 41 - ASCII full name must be updated, is used by the suggestion oracle
                if (null == c.getId()) {
                    final Customer customer = App.get().getJsonDeserializer().deserialize(
                            Customer.class, "customer", jsonText);
                    eventBus.fireEvent(new CrudEvent(OperationType.CREATE, customer));
                    Window.alert(CONSTANTS.patientAdded()[App.get().getSelectedUnitTextVariant()]);
                } else {
                    eventBus.fireEvent(new CrudEvent(OperationType.UPDATE, c));
                    Window.alert(CONSTANTS.patientUpdated()[App.get().getSelectedUnitTextVariant()]);
                }
            }
            @Override
            public void onValidationFailure(final ValidationException ve) {
                validator.processServerErrors(ve);
            }
        });

        rest.post(params);
    }

}
