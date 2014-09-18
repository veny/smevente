package veny.smevente.client.uc;

import java.util.List;

import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.model.Customer;
import veny.smevente.model.Procedure;

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
 * Special message Dialog Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 11.09.2010
 */
public class SpecialMsgDlgPresenter extends AbstractPresenter<SpecialMsgDlgPresenter.SpecialMsgDlgView> {

    /**
     * Special message Dialog View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 11.09.2010
     */
    public interface SpecialMsgDlgView extends View {
        /**
         * @return the procedure
         */
        ListBox getType();
        /**
         * @return the name suggest box
         */
        TextBox getFullname();
        /**
         * @return the phone number
         */
        TextBox getBirthNumber();
        /**
         * @return the SMS text
         */
        TextArea getMsgText();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
    }

    /** List of special procedures. */
    private List<Procedure> specialProcedures;


    /**
     * Initializes presenter.
     * @param customer the recipient
     * @param specProcedures the list of special procedures
     */
    public void init(final Customer customer, final List<Procedure> specProcedures) {
        // clear all the stuff
        clean();

        this.specialProcedures = specProcedures;
        initProceduresCombo(specialProcedures);

        view.getFullname().setText(customer.fullname());
        view.getBirthNumber().setText(customer.getBirthNumber());

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
        view.getMsgText().setFocus(true);
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
        view.getBirthNumber().setText("");
        view.getMsgText().setText("");

        if (null != specialProcedures) { specialProcedures.clear(); }

        // validation
        validator.reset((String[]) null);
        getView().getMsgText().removeStyleName("validationFailedBorder");
    }

    /**
     * Get ID of selected special procedure.
     * @return ID of selected special procedure
     */
    public String getSelectedProcedureId() {
        final int idx = view.getType().getSelectedIndex();
        return specialProcedures.get(idx).getId().toString();
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Invoked if a Type was selected.
     * @param index index of selected type
     */
    private void changedType(final int index) {
        view.getMsgText().setText(specialProcedures.get(index).getMessageText());
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

        // entered SMS text?
        validator.addValidators("smsText",
                new EmptyValidator(view.getMsgText())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

    /**
     *
     * @param specialProcedures the procedures used in dialog
     */
    private void initProceduresCombo(final List<Procedure> specialProcedures) {
        if (specialProcedures != null) {
            for (final Procedure proc : specialProcedures) {
                view.getType().addItem(proc.getName());
            }
        }
    }
}
