package veny.smevente.client.uc;

import java.util.Date;
import java.util.List;

import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.PatientNameSuggestOracle;
import veny.smevente.client.utils.PatientSuggestion;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * Event Dialog Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class EventDlgPresenter extends AbstractPresenter<EventDlgPresenter.EventDlgView> {

    /**
     * Event Dialog View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 0.1
     */
    public interface EventDlgView extends View {
        /**
         * @return the date
         */
        InlineLabel getDate();
        /**
         * @return the start hour
         */
        ListBox getStartHour();
        /**
         * @return the start minute
         */
        ListBox getStartMinute();
        /**
         * @return the procedure color
         */
        Label getProcedureHeader();
        /**
         * @return the procedure
         */
        ListBox getProcedure();
        /**
         * @return the event length
         */
        ListBox getLength();
        /**
         * @return the name suggest box
         */
        SuggestBox getNameSuggestBox();
        /**
         * @return the phone number
         */
        TextBox getPhoneNumber();
        /**
         * @return the message text
         */
        TextArea getMessageText();
        /**
         * @return the notice
         */
        TextArea getNotice();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
        /**
         * @return the hidden field wit event ID
         */
        Hidden getEventId();
    }

    /** Possible procedure lengths [min]. */
    private static final String[] PROCEDURE_LENGTHS = new String[] {
        // first hour : 5 minutes period
        "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60",
        // hour 1-4 : 20 minutes period
        "80", "100", "120", "140", "160", "180", "200", "220", "240",
        // hour > 3
        "300", "360", "420", "480"
    };


    /** Initial date and time to send SMS. */
    private Date startTime;
    /** List of available procedures. */
    private List<Procedure> procedures;
    /** Selected customer. */
    private Customer selectedCustomer = null;

    /**
     * Initializes presenter for Create.
     * @param startTime date and time to send SMS
     * @param customers customer list
     * @param mhcs list of medical help categories
     */
    @SuppressWarnings("deprecation")
    public void init(final Date startTime, final List<Customer> customers, final List<Procedure> mhcs) {
        // clear all the stuff
        clean();

        // initialize time
        this.startTime = startTime;
        view.getDate().setText(DateTimeFormat.getLongDateFormat().format(startTime));
        view.getStartHour().setItemSelected(startTime.getHours(), true);
        view.getStartMinute().setItemSelected(startTime.getMinutes() / 5, true);

        // Customer Name Suggestion
        PatientNameSuggestOracle oracle = (PatientNameSuggestOracle) view.getNameSuggestBox().getSuggestOracle();
        oracle.setCustomers(customers);

        // Medical Help Category
        procedures = mhcs;
        for (Procedure mhc : procedures) {
            view.getProcedure().addItem(mhc.getName());
        }
        view.getProcedure().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
                changedMedicalHelpCategory(view.getProcedure().getSelectedIndex(), true);
            }
        });
        changedMedicalHelpCategory(0, true);
    }

    /**
     * Initializes presenter for Update.
     * @param sms SMS to be displayed
     * @param customers customer list
     * @param mhcs list of medical help categories
     */
    public void init(
            final Event sms, final List<Customer> customers, final List<Procedure> mhcs) {

        this.init(sms.getStartTime(), customers, mhcs);

        // set all form elements
        selectedCustomer = sms.getCustomer();
        view.getEventId().setValue(sms.getId().toString());
        view.getNameSuggestBox().getTextBox().setText(selectedCustomer.fullname());
        view.getPhoneNumber().setText(selectedCustomer.getPhoneNumber());
        view.getMessageText().setText(sms.getText());
        view.getNotice().setText(sms.getNotice());
        // MHC length
        for (int i = 0; i < PROCEDURE_LENGTHS.length; i++) {
            if (sms.getLength() <= Long.parseLong(PROCEDURE_LENGTHS[i])) {
                view.getLength().setSelectedIndex(i);
                break;
            }
        }
        // MHC
        int idx = 0;
        for (Procedure mhc : mhcs) {
            if (mhc.getId().equals(sms.getProcedure().getId())) {
                view.getProcedure().setSelectedIndex(idx);
                break;
            }
            idx++;
        }
        changedMedicalHelpCategory(idx, false);
    }

    /**
     * Gets flag whether the dialog represent Update or Create operation.
     * @return <i>true</i> if it's Update operation
     */
    public boolean isUpdate() {
        return (null != view.getEventId().getValue() && !"null".equals(view.getEventId().getValue())
                && view.getEventId().getValue().trim().length() > 0);
    }

    /**
     * Gets the selected customer.
     * @return selected customer
     */
    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    /**
     * Gets selected procedure.
     * @return selected procedure
     */
    public Procedure getSelectedProcedure() {
        return procedures.get(view.getProcedure().getSelectedIndex());
    }

    /**
     * Gets selected start date of medical help.
     * @return start date of medical help
     */
    @SuppressWarnings("deprecation")
    public Date getStartTime() {
        final Date d = (Date) startTime.clone();
        d.setHours(Integer.parseInt(view.getStartHour().getValue(view.getStartHour().getSelectedIndex())));
        d.setMinutes(Integer.parseInt(view.getStartMinute().getValue(view.getStartMinute().getSelectedIndex())));
        return d;
    }

    /**
     * Gets selected length of medical help.
     * @return length of medical help
     */
    public int getMedicalHelpLength() {
        String strLen = view.getLength().getValue(view.getLength().getSelectedIndex());
        return Integer.parseInt(strLen);
    }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        // set values of hour drop down
        for (int i = 0; i < 24; i++) {
            view.getStartHour().addItem("" + i);
        }
        // set values of minutes drop down
        for (int i = 0; i < 60; i += 5) {
            view.getStartMinute().addItem("" + i);
        }

        // set values of 'medical help length' drop down
        for (String len : PROCEDURE_LENGTHS) {
            view.getLength().addItem(len);
        }

        // suggest box selection listener
        view.getNameSuggestBox().addSelectionHandler(new SelectionHandler<Suggestion>() {
            @Override
            public void onSelection(final SelectionEvent<Suggestion> event) {
                final PatientSuggestion sug = (PatientSuggestion) event.getSelectedItem();
                selectedCustomer = sug.getPatient();
                view.getPhoneNumber().setText(sug.getPatient().getPhoneNumber());
            }
        });

        // VALIDATION
        setupValidation();
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    protected void onShow(final Object parameter) {
        // set texts
        view.getProcedureHeader().setText(CONSTANTS.procedure());

        view.getNameSuggestBox().getTextBox().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getEventId().setValue(null);
        view.getDate().setText("");
        view.getStartHour().setItemSelected(0, true);
        view.getStartMinute().setItemSelected(0, true);
        view.getProcedure().clear();
        view.getNameSuggestBox().getTextBox().setText("");
        view.getPhoneNumber().setText("");
        view.getMessageText().setText("");
        view.getNotice().setText("");

        // validation
        validator.reset((String[]) null);
        getView().getNameSuggestBox().getTextBox().removeStyleName("validationFailedBorder");
        getView().getMessageText().removeStyleName("validationFailedBorder");

        selectedCustomer = null;
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Invoked if a Medical Help Category was selected.
     * @param index index of selected Medical Help Category
     * @param switchTimeAndText whether to change the MH length and SMS text
     */
    private void changedMedicalHelpCategory(final int index, final boolean switchTimeAndText) {
        final Procedure mhc = procedures.get(index);

        // color
        DOM.setStyleAttribute(view.getProcedureHeader().getElement(), "backgroundColor", "#" + mhc.getColor());

        if (!switchTimeAndText) { return; }

        // time
        int idx = -1;
        for (int i = 0; i < PROCEDURE_LENGTHS.length; i++) {
            if (mhc.getTime() <= Long.parseLong(PROCEDURE_LENGTHS[i])) {
                idx = i;
                break;
            }
        }
        view.getLength().setSelectedIndex(idx);

        // SMS text
        view.getMessageText().setText(mhc.getMessageText());
    }

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
                if ("name".equals(propertyName)) {
                    result = CONSTANTS.name();
                } else if ("smsText".equals(propertyName)) {
                    result = CONSTANTS.smsText();
                } else {
                    result = super.getPropertyName(propertyName);
                }
                return result;
            }
        };
        validator = new DefaultValidationProcessor(vmess);
        // add DisclosurePanel for validation messages
        validator.addGlobalAction(new DisclosureTextAction(view.getValidationErrors(), "redText"));

        final FocusAction focusAction = new FocusAction();

        // some selected customer?
        validator.addValidators("name", new Validator<Object>() {
            @Override
            public void invokeActions(final ValidationResult result) {
                getView().getNameSuggestBox().setFocus(true);
                getView().getNameSuggestBox().getTextBox().addStyleName("validationFailedBorder");
            }
            @Override
            public ValidationResult validate(final ValidationMessages messages) {
                // remove result of previous failed validation if any
                getView().getNameSuggestBox().getTextBox().removeStyleName("validationFailedBorder");

                ValidationResult rslt = null;
                if (null == selectedCustomer) {
                    rslt = new ValidationResult(CONSTANTS.validationNotSelected());
                }
                return rslt;
            }
        });
        // entered SMS text?
        validator.addValidators("smsText",
                new EmptyValidator(view.getMessageText())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

}
