package veny.smevente.client.uc;

import java.util.HashMap;
import java.util.Map;

import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithValidation;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.EmptyValidator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.PasswordTextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * Change password presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 19.9.2010
 */
public class ChangePasswordPresenter
    extends AbstractPresenter<ChangePasswordPresenter.ChangePasswordView> {

    /**
     * View interface for the 'change password' form.
     *
     * @author Vaclav Sykora
     * @since 19.9.2010
     */
    public interface ChangePasswordView extends View {
        /**
         * Getter for the old user password text field.
         * @return the input field for the password
         */
        PasswordTextBox getOldPassword();
        /**
         * Getter for the new user password text field.
         * @return the input field for the password
         */
        PasswordTextBox getNewPassword();
        /**
         * Getter for the new user password again text field.
         * @return the input field for the password
         */
        PasswordTextBox getNewPasswordAgain();
        /**
         * Getter for the change button.
         * @return change button
         */
        HasClickHandlers getSubmit();
        /**
         * Getter for validation errors panel.
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
    }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                // validation
                if (!validator.validate()) {
                    // One (or more) validations failed. The actions will have been
                    // already invoked by the ...validate() call.
                    return;
                }
                changePassword(view.getOldPassword().getText(), view.getNewPassword().getText());
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
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object parameter) {
        view.getOldPassword().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getOldPassword().setText("");
        view.getNewPassword().setText("");
        view.getNewPasswordAgain().setText("");
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Sends request to change password of the current logged in user.
     * @param old the old password
     * @param newP the new password
     */
    private void changePassword(final String old, final String newP) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("old", old);
        params.put("new", newP);

        final RestHandler rest = new RestHandler("/rest/user/password/");
        rest.setCallback(new AbstractRestCallbackWithValidation() {
            @Override
            public void onSuccess(final String jsonText) {
                Window.alert(CONSTANTS.passwordChanged());
            }
            @Override
            public void onValidationFailure(final ValidationException ve) {
                validator.processServerErrors(ve);
            }
        });
        rest.post(params);
    }

    /**
     * Sets up validation for the presenter.
     */
    private void setupValidation() {
        // validation messages
        ValidationMessages vmess = new UcValidationMessages();
        validator = new DefaultValidationProcessor(vmess);
        // add DisclosurePanel for validation messages
        validator.addGlobalAction(new DisclosureTextAction(view.getValidationErrors(), "redText"));

        final FocusAction focusAction = new FocusAction();

        validator.addValidators("old",
                new EmptyValidator(view.getOldPassword())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("new",
                new EmptyValidator(view.getNewPassword())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("newAgain",
                new EmptyValidator(view.getNewPasswordAgain())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        // new == new again
        validator.addValidators("newDontEqual", new Validator<Object>() {
            @Override
            public void invokeActions(final ValidationResult result) {
                getView().getNewPasswordAgain().setFocus(true);
                getView().getNewPasswordAgain().addStyleName("validationFailedBorder");
            }
            @Override
            public ValidationResult validate(final ValidationMessages messages) {
                // remove result of previous failed validation if any
                getView().getNewPasswordAgain().removeStyleName("validationFailedBorder");

                ValidationResult rslt = null;
                if (!view.getNewPassword().getText().equals(view.getNewPasswordAgain().getText())) {
                    rslt = new ValidationResult(CONSTANTS.validationChangePasswordNew());
                }
                return rslt;
            }
        });
    }

    /**
     * Validation messages specific for this presenter.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 14.10.2010
     */
    class UcValidationMessages extends ValidationMessages {

        /** {@inheritDoc} */
        @Override
        public String getCustomMessage(final String key, final Object... parameters) {
            if ("validationOldPasswordBad".equals(key)) {
                return CONSTANTS.validationOldPasswordBad();
            }
            return getValidationMessage(key, parameters);
        }

        /** {@inheritDoc} */
        @Override
        public String getPropertyName(final String propertyName) {
            String result;
            if ("old".equals(propertyName)) {
                result = CONSTANTS.oldPassword();
            } else if ("new".equals(propertyName)) {
                result = CONSTANTS.newPassword();
            } else if ("newAgain".equals(propertyName)) {
                result = CONSTANTS.newPasswordAgain();
            } else if ("newDontEqual".equals(propertyName)) {
                result = CONSTANTS.newPasswordAgain();
            } else { result = super.getPropertyName(propertyName); }
            return result;
        }
    }

}
