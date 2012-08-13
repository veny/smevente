package veny.smevente.client.uc;

import java.util.Date;
import java.util.List;

import veny.smevente.client.App;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.PatientNameSuggestOracle;
import veny.smevente.client.utils.PatientSuggestion;
import veny.smevente.model.MedicalHelpCategoryDto;
import veny.smevente.model.PatientDto;
import veny.smevente.model.SmsDto;

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
 * SMS Dialog Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class SmsDlgPresenter extends AbstractPresenter<SmsDlgPresenter.SmsDlgView> {

    /**
     * SMS Dialog View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 0.1
     */
    public interface SmsDlgView extends View {
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
         * @return the medical help color
         */
        Label getMedicalHelpHeader();
        /**
         * @return the medical help
         */
        ListBox getMedicalHelp();
        /**
         * @return the medical help length
         */
        ListBox getMedicalHelpLength();
        /**
         * @return the name suggest box
         */
        SuggestBox getNameSuggestBox();
        /**
         * @return the phone number
         */
        TextBox getPhoneNumber();
        /**
         * @return the SMS text
         */
        TextArea getSmsText();
        /**
         * @return the notice
         */
        TextArea getNotice();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
        /**
         * @return the hidden field wit SMS ID
         */
        Hidden getSmsId();
    }

    /** Possible medical help lengths [min]. */
    private static final String[] MH_LENGTHS = new String[] {
        // first hour : 5 minutes period
        "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60",
        // hour 1-4 : 20 minutes period
        "80", "100", "120", "140", "160", "180", "200", "220", "240",
        // hour > 3
        "300", "360", "420", "480"
    };


    /** Initial date and time to send SMS. */
    private Date startTime;
    /** List of available Medical Help Categories. */
    private List<MedicalHelpCategoryDto> medicalHelpCategories;
    /** Selected patient. */
    private PatientDto selectedPatient = null;

    /**
     * Initializes presenter for Create.
     * @param startTime date and time to send SMS
     * @param patients patient list
     * @param mhcs list of medical help categories
     */
    @SuppressWarnings("deprecation")
    public void init(final Date startTime, final List<PatientDto> patients, final List<MedicalHelpCategoryDto> mhcs) {
        // clear all the stuff
        clean();

        // initialize time
        this.startTime = startTime;
        view.getDate().setText(DateTimeFormat.getLongDateFormat().format(startTime));
        view.getStartHour().setItemSelected(startTime.getHours(), true);
        view.getStartMinute().setItemSelected(startTime.getMinutes() / 5, true);

        // Patient Name Suggestion
        PatientNameSuggestOracle oracle = (PatientNameSuggestOracle) view.getNameSuggestBox().getSuggestOracle();
        oracle.setPatients(patients);

        // Medical Help Category
        medicalHelpCategories = mhcs;
        for (MedicalHelpCategoryDto mhc : medicalHelpCategories) {
            view.getMedicalHelp().addItem(mhc.getName());
        }
        view.getMedicalHelp().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
                changedMedicalHelpCategory(view.getMedicalHelp().getSelectedIndex(), true);
            }
        });
        changedMedicalHelpCategory(0, true);
    }

    /**
     * Initializes presenter for Update.
     * @param sms SMS to be displayed
     * @param patients patient list
     * @param mhcs list of medical help categories
     */
    public void init(
            final SmsDto sms, final List<PatientDto> patients, final List<MedicalHelpCategoryDto> mhcs) {

        this.init(sms.getMedicalHelpStartTime(), patients, mhcs);

        // set all form elements
        selectedPatient = sms.getPatient();
        view.getSmsId().setValue(sms.getId().toString());
        view.getNameSuggestBox().getTextBox().setText(selectedPatient.getFullname());
        view.getPhoneNumber().setText(selectedPatient.getPhoneNumber());
        view.getSmsText().setText(sms.getText());
        view.getNotice().setText(sms.getNotice());
        // MHC length
        for (int i = 0; i < MH_LENGTHS.length; i++) {
            if (sms.getMedicalHelpLength() <= Long.parseLong(MH_LENGTHS[i])) {
                view.getMedicalHelpLength().setSelectedIndex(i);
                break;
            }
        }
        // MHC
        int idx = 0;
        for (MedicalHelpCategoryDto mhc : mhcs) {
            if (mhc.getId().equals(sms.getMedicalHelpCategory().getId())) {
                view.getMedicalHelp().setSelectedIndex(idx);
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
        return (null != view.getSmsId().getValue() && !"null".equals(view.getSmsId().getValue())
                && view.getSmsId().getValue().trim().length() > 0);
    }

    /**
     * Gets the selected patient.
     * @return selected patient
     */
    public PatientDto getSelectedPatient() {
        return selectedPatient;
    }

    /**
     * Gets selected Medical Help Category.
     * @return selected Medical Help Category
     */
    public MedicalHelpCategoryDto getSelectedMedicalHelpCategory() {
        return medicalHelpCategories.get(view.getMedicalHelp().getSelectedIndex());
    }

    /**
     * Gets selected start date of medical help.
     * @return start date of medical help
     */
    @SuppressWarnings("deprecation")
    public Date getStartTime() {
        final Date rslt = (Date) startTime.clone();
        rslt.setHours(Integer.parseInt(view.getStartHour().getValue(view.getStartHour().getSelectedIndex())));
        rslt.setMinutes(Integer.parseInt(view.getStartMinute().getValue(view.getStartMinute().getSelectedIndex())));
        return rslt;
    }

    /**
     * Gets selected length of medical help.
     * @return length of medical help
     */
    public int getMedicalHelpLength() {
        String strLen = view.getMedicalHelpLength().getValue(view.getMedicalHelpLength().getSelectedIndex());
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
        for (String len : MH_LENGTHS) {
            view.getMedicalHelpLength().addItem(len);
        }

        // suggest box selection listener
        view.getNameSuggestBox().addSelectionHandler(new SelectionHandler<Suggestion>() {
            @Override
            public void onSelection(final SelectionEvent<Suggestion> event) {
                final PatientSuggestion sug = (PatientSuggestion) event.getSelectedItem();
                selectedPatient = sug.getPatient();
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
        view.getMedicalHelpHeader().setText(CONSTANTS.medicalHelp()[App.get().getSelectedUnitTextVariant()]);

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
        view.getSmsId().setValue(null);
        view.getDate().setText("");
        view.getStartHour().setItemSelected(0, true);
        view.getStartMinute().setItemSelected(0, true);
        view.getMedicalHelp().clear();
        view.getNameSuggestBox().getTextBox().setText("");
        view.getPhoneNumber().setText("");
        view.getSmsText().setText("");
        view.getNotice().setText("");

        // validation
        validator.reset((String[]) null);
        getView().getNameSuggestBox().getTextBox().removeStyleName("validationFailedBorder");
        getView().getSmsText().removeStyleName("validationFailedBorder");

        selectedPatient = null;
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Invoked if a Medical Help Category was selected.
     * @param index index of selected Medical Help Category
     * @param switchTimeAndText whether to change the MH length and SMS text
     */
    private void changedMedicalHelpCategory(final int index, final boolean switchTimeAndText) {
        final MedicalHelpCategoryDto mhc = medicalHelpCategories.get(index);

        // color
        DOM.setStyleAttribute(view.getMedicalHelpHeader().getElement(), "backgroundColor", "#" + mhc.getColor());

        if (!switchTimeAndText) { return; }

        // time
        int idx = -1;
        for (int i = 0; i < MH_LENGTHS.length; i++) {
            if (mhc.getTime() <= Long.parseLong(MH_LENGTHS[i])) {
                idx = i;
                break;
            }
        }
        view.getMedicalHelpLength().setSelectedIndex(idx);

        // SMS text
        view.getSmsText().setText(mhc.getSmsText());
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

        // some selected patient?
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
                if (null == selectedPatient) {
                    rslt = new ValidationResult(CONSTANTS.validationNotSelected());
                }
                return rslt;
            }
        });
        // entered SMS text?
        validator.addValidators("smsText",
                new EmptyValidator(view.getSmsText())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

}
