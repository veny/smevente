package veny.smevente.client.uc;

import java.util.HashMap;
import java.util.List;
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
import veny.smevente.model.Membership;
import veny.smevente.model.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;
import eu.maydu.gwt.validation.client.validators.numeric.LongValidator;

/**
 * Add Patient presenter.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 26.11.2011
 */
public class StoreUserPresenter
    extends AbstractPresenter<StoreUserPresenter.StoreUserView>
    implements HeaderHandler {

    /**
     * View interface for the Add Patient.
     *
     * @author Vaclav Sykora
     * @since 22.8.2010
     */
    public interface StoreUserView extends View {
        /**
         * Getter for the user name text field.
         * @return the input field for the user name
         */
        TextBox getUsername();
        /**
         * Getter for the full name text field.
         * @return the input field for the full name
         */
        TextBox getFullname();
        /**
         * Getter for the flag is the password should
         * be also set. Used only in case of user update.
         * @return the check box for the set password flag
         */
        CheckBox getUpdatePassword();
        /**
         * Getter for the user password text field.
         * @return the input field for the password
         */
        PasswordTextBox getPassword();
        /**
         * Getter for the user password again text field.
         * @return the input field for the password
         */
        PasswordTextBox getPasswordAgain();
        /**
         * Getter for the unit order text field.
         * @return the input field for the unit order
         */
        TextBox getUnitOrder();
        /**
         * Getter for the flag is user is an
         * unit administrator.
         * @return the check box for the unit
         * administrator flag
         */
        CheckBox getUnitAdmin();
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
         * @return the hidden field wit user ID
         */
        Hidden getUserId();
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

        view.getUpdatePassword().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (view.getUpdatePassword().getValue()) {
                    view.getPassword().setEnabled(true);
                    view.getPasswordAgain().setEnabled(true);
                } else {
                    view.getPassword().removeStyleName("validationFailedBorder");
                    view.getPasswordAgain().removeStyleName("validationFailedBorder");
                    view.getPassword().setEnabled(false);
                    view.getPasswordAgain().setEnabled(false);
                }
                // Initialize the validation once again, because
                // it depends also on the flag value.
                setupValidation();
            }
        });

        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                // validation
                if (!validator.validate()) {
                    // One (or more) validations failed. The actions will have been
                    // already invoked by the ...validate() call.
                    return;
                }
                storeUser();
            }
        });

        view.getCancel().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().switchToPresenterByType(PresenterEnum.FIND_USER, null);
            }
        });
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
        view.getUsername().setFocus(true);

        if (null != parameter && parameter instanceof User) {
            final User u = (User) parameter;
            Membership m = getMembership(u);
            view.getUserId().setValue(u.getId().toString());
            view.getUsername().setText(u.getUsername());
            view.getFullname().setText(u.getFullname());
            view.getUnitAdmin().setValue(m.enumRole() == Membership.Role.ADMIN);
            view.getUnitOrder().setValue("" + (m.getSignificance() + 1));
            view.getUpdatePassword().setValue(null);
            view.getUpdatePassword().setEnabled(true);
            view.getPassword().setEnabled(false);
            view.getPasswordAgain().setEnabled(false);
        } else {
            // Using a null as argument on IE7 will lead to the setting of
            // string "null" as value, therefore the empty string is used instead.
            view.getUserId().setValue("");
            // set default value to avoid the "empty value error" when submit
            view.getUnitOrder().setValue("1");
            view.getUpdatePassword().setValue(true);
            view.getUpdatePassword().setEnabled(false);
            view.getPassword().setEnabled(true);
            view.getPasswordAgain().setEnabled(true);
        }

        // VALIDATION
        // The validation has to be configured here, because when configured
        // in method "onBind", the required flag if the password should be
        // checked or not isn't yet set.
        setupValidation();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // Using a null as argument on IE7 will lead to the setting of
        // string "null" as value, therefore the empty string is used instead.
        view.getUserId().setValue("");
        view.getUsername().setText("");
        view.getFullname().setText("");
        view.getPassword().setText("");
        view.getPasswordAgain().setText("");
        view.getUnitAdmin().setValue(null);
        view.getUnitOrder().setText("");
        view.getUpdatePassword().setValue(true);
        view.getUpdatePassword().setEnabled(false);
        view.getPassword().setEnabled(true);
        view.getPasswordAgain().setEnabled(true);

        // validation
        validator.reset((String[]) null);
        view.getUsername().removeStyleName("validationFailedBorder");
        view.getFullname().removeStyleName("validationFailedBorder");
        view.getUnitOrder().removeStyleName("validationFailedBorder");
        view.getPassword().removeStyleName("validationFailedBorder");
        view.getPasswordAgain().removeStyleName("validationFailedBorder");
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Sets up validation for the presenter.
     */
    private void setupValidation() {
        // CHECKSTYLE:OFF
        // validation messages
        ValidationMessages vmess = new ValidationMessages() {
            @Override
            public String getCustomMessage(final String key, final Object... parameters) {
                return getValidationMessage(key, parameters);
            }
            @Override
            public String getPropertyName(final String propertyName) {
                String result;
                if ("username".equals(propertyName)) {
                    result = CONSTANTS.username();
                } else if ("fullname".equals(propertyName)) {
                    result = CONSTANTS.fullname();
                } else if ("password".equals(propertyName)) {
                    result = CONSTANTS.password();
                } else if ("unitOrder".equals(propertyName)) {
                    result = CONSTANTS.unitOrder();
                } else if ("passwordAgain".equals(propertyName)) {
                    result = CONSTANTS.passwordAgain();
                } else { result = super.getPropertyName(propertyName); }
                return result;
            }
        };
        // CHECKSTYLE:ON
        validator = new DefaultValidationProcessor(vmess);
        // add DisclosurePanel for validation messages
        validator.addGlobalAction(new DisclosureTextAction(view.getValidationErrors(), "redText"));

        final FocusAction focusAction = new FocusAction();

        validator.addValidators("username",
                new EmptyValidator(view.getUsername())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("fullname",
                new EmptyValidator(view.getFullname())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("unitOrder",
                new LongValidator(view.getUnitOrder(), false, "notNumber") {
                    @Override
                    public void invokeActions(final ValidationResult result) {
                        getView().getUnitOrder().setFocus(true);
                        getView().getUnitOrder().addStyleName("validationFailedBorder");
                    }
                    @Override
                    public ValidationResult validate(final ValidationMessages allMessages) {
                        // remove result of previous failed validation if any
                        getView().getUnitOrder().removeStyleName("validationFailedBorder");
                        ValidationResult rslt = super.validate(allMessages);
                        if (rslt == null || !rslt.validationFailed()) {
                            int unitOrder = Integer.parseInt(view.getUnitOrder().getValue());
                            if (unitOrder <= 0) {
                                rslt = new ValidationResult(CONSTANTS.validationNotGreaterZero());
                            }
                        }
                        return rslt;
                    };
                });
        if (view.getUpdatePassword().getValue()) {
            validator.addValidators("password",
                    new EmptyValidator(view.getPassword())
                        .addActionForFailure(focusAction)
                        .addActionForFailure(new StyleAction("validationFailedBorder")));
            // password == password again
            validator.addValidators("passwordAgain", new Validator<Object>() {
                @Override
                public void invokeActions(final ValidationResult result) {
                    getView().getPasswordAgain().setFocus(true);
                    getView().getPasswordAgain().addStyleName("validationFailedBorder");
                }
                @Override
                public ValidationResult validate(final ValidationMessages messages) {
                    // remove result of previous failed validation if any
                    getView().getPasswordAgain().removeStyleName("validationFailedBorder");

                    ValidationResult rslt = null;
                    if (!view.getPassword().getText().equals(view.getPasswordAgain().getText())) {
                        rslt = new ValidationResult(CONSTANTS.validationChangePassword());
                    }
                    return rslt;
                }
            });
        }
    }

    /**
     * Creates a new patient.
     */
    private void storeUser() {
        final User u = new User();
        if (null == view.getUserId().getValue() || view.getUserId().getValue().trim().isEmpty()) {
            u.setId(null);
        } else {
            u.setId(Long.parseLong(view.getUserId().getValue()));
        }
        u.setUsername(view.getUsername().getText());
        u.setFullname(view.getFullname().getText());
        u.setPassword(view.getUpdatePassword().getValue()
                ? view.getPassword().getText()
                : User.DO_NOT_CHANGE_PASSWORD);

        final Map<String, String> params = new HashMap<String, String>();
        params.put("unitId", App.get().getSelectedUnit().getId().toString());
        params.put("type", "" + (view.getUnitAdmin().getValue()
                ? Membership.Role.ADMIN.ordinal() : Membership.Role.MEMBER.ordinal()));
        params.put("significance", "" + (Integer.parseInt(view.getUnitOrder().getValue()) - 1));
        params.put("username", u.getUsername());
        params.put("fullname", u.getFullname());
        params.put("password", u.getPassword());
        if (null != u.getId()) { params.put("id", u.getId().toString()); }

        final RestHandler rest = new RestHandler("/rest/user/");
        rest.setCallback(new AbstractRestCallbackWithValidation() {
            @Override
            public void onSuccess(final String jsonText) {
                if (null == u.getId()) {
                    final User user = App.get().getJsonDeserializer().deserialize(
                            User.class, "user", jsonText);
                    eventBus.fireEvent(new CrudEvent(OperationType.CREATE, user));
                    Window.alert(CONSTANTS.userAdded());
                } else {
                    eventBus.fireEvent(new CrudEvent(OperationType.UPDATE, u));
                    Window.alert(CONSTANTS.userUpdated());
                }
            }
            @Override
            public void onValidationFailure(final ValidationException ve) {
                validator.processServerErrors(ve);
            }
        });

        if (null == u.getId()) {
            rest.post(params);
        } else {
            rest.put(params);
        }
    }

    /**
     *
     * @param user the user for which the membership will be searched
     * @return the membership for given user and current unit
     */
    private Membership getMembership(final User user) {
        List<Membership> memberships = null;//XXX App.get().getSelectedUnit().getMembers();

        if (memberships != null) {
            for (Membership membership: memberships) {
                if (membership.getUser().getId() == user.getId()) {
                    return membership;
                }
            }
        }

        throw new IllegalStateException("no membership found for user id=" + user.getId());
    }
}
