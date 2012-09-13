package veny.smevente.client.uc;

import java.util.ArrayList;
import java.util.List;

import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.Pair;
import veny.smevente.model.MedicalHelpCategory;
import veny.smevente.model.Patient;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * Special SMS Dialog Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 11.09.2010
 */
public class SpecialSmsDlgPresenter extends AbstractPresenter<SpecialSmsDlgPresenter.SpecialSmsDlgView> {

    /**
     * Special SMS Dialog View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 11.09.2010
     */
    public interface SpecialSmsDlgView extends View {
        /**
         * @return the medical help
         */
        ListBox getType();
        /**
         * @return the name suggest box
         */
        TextBox getFullname();
        /**
         * @return the phone number
         */
        TextBox getPhoneNumber();
        /**
         * @return the SMS text
         */
        TextArea getSmsText();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
    }

    /** List of types and corresponding texts. */
    private List<Pair<String, String>> types = new ArrayList<Pair<String, String>>();


    /**
     * Initializes presenter.
     * @param patient the recipient
     * @param specialCategories the list of special categories
     */
    public void init(final Patient patient, final List<MedicalHelpCategory> specialCategories) {
        // clear all the stuff
        clean();

        initTypes(specialCategories);

        view.getFullname().setText(patient.fullname());
        view.getPhoneNumber().setText(patient.getPhoneNumber());

        // default type - the first one
        changedType(0);
    }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        view.getType().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
                changedType(view.getType().getSelectedIndex());
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
        view.getSmsText().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getType().clear();
        view.getFullname().setText("");
        view.getPhoneNumber().setText("");
        view.getSmsText().setText("");

        types.clear();

        // validation
        validator.reset((String[]) null);
        getView().getSmsText().removeStyleName("validationFailedBorder");
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Invoked if a Type was selected.
     * @param index index of selected type
     */
    private void changedType(final int index) {
        final Pair<String, String> type = types.get(index);
        // SMS text
        view.getSmsText().setText(type.getB());
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
                if ("phoneNumber".equals(propertyName)) {
                    result = CONSTANTS.phoneNumber();
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

        // phone number
        validator.addValidators("phoneNumber",
                new EmptyValidator(view.getPhoneNumber())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        // entered SMS text?
        validator.addValidators("smsText",
                new EmptyValidator(view.getSmsText())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

    /**
     *
     * @param specialCategories the categories used in dialog
     */
    private void initTypes(final List<MedicalHelpCategory> specialCategories) {
        if (specialCategories != null) {
            for (MedicalHelpCategory mhc : specialCategories) {
                Pair<String, String> type = new Pair<String, String>(mhc.getName(),
                        mhc.getSmsText());
                types.add(type);
                view.getType().addItem(type.getA());
            }
        }
    }
}
