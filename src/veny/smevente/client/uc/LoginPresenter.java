package veny.smevente.client.uc;

import java.util.LinkedList;
import java.util.List;

import veny.smevente.client.AbstractEnterKeyHandler;
import veny.smevente.client.App;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.UnauthorizedEvent;
import veny.smevente.client.utils.EmptyValidator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.InvalidValueSerializable;
import eu.maydu.gwt.validation.client.ValidationException;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * Presenter for the login form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class LoginPresenter extends AbstractPresenter<LoginPresenter.LoginView>
    implements UnauthorizedEvent.UnauthorizedEventHandler {

    /**
     * View interface for the login form.
     *
     * @author Vaclav Sykora
     * @since 0.1
     */
    public interface LoginView extends View {
        /**
         * Getter for the user name text field.
         * @return the input field for the user name
         */
        TextBox getUsername();
        /**
         * Getter for the user name text field.
         * @return the input field for the password
         */
        PasswordTextBox getPassword();
        /**
         * Getter for the button to submit.
         * @return the submit element
         */
        Button getSubmit();
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
        eventBus.addHandler(UnauthorizedEvent.TYPE, this);

        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                login();
            }
        });

        // ENTER pressed on password
        view.getPassword().addKeyUpHandler(new AbstractEnterKeyHandler() {
            @Override
            public void onEnterKeyPress() {
                login();
            }
        });

        // ENTER pressed on username
        view.getUsername().addKeyUpHandler(new AbstractEnterKeyHandler() {
            @Override
            public void onEnterKeyPress() {
                PasswordTextBox password = view.getPassword();
                if (password.getText().isEmpty()) {
                    password.setFocus(true);
                } else {
                    login();
                }
            }
        });

        // VALIDATION
        setupValidation();
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        validator.removeValidatorsAndGlobalActions();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object parameter) {
        view.getUsername().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // nothing to do here
    }

    // ------------------------- UnauthorizedEvent.UnauthorizedEventHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void unauthorized(final UnauthorizedEvent event) {
        App.get().showLoginScreen();
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Login action.
     */
    private void login() {
        // validation
        if (!validator.validate()) {
            // One (or more) validations failed. The actions will have been
            // already invoked by the ...validate() call.
            return;
        }

        // send GWT POST request for login
        final RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/rest/login/");

        String postData = "j_username=" + view.getUsername().getValue()
            + "&j_password=" + view.getPassword().getValue();

        builder.setHeader("Content-type", "application/x-www-form-urlencoded");
        builder.setRequestData(postData);
        builder.setCallback(new RequestCallback() {
            @Override
            public void onError(final Request request, final Throwable e) {
                App.get().getFailureHandler().handleClientErrorWithAction(
                        e, "Login, callback.onError", "Login request cannot be sent, uri=" + builder.getUrl());
                view.getSubmit().setEnabled(true);
            }
            @Override
            public void onResponseReceived(final Request request, final Response response) {
                processLoginResponse(response);
                view.getSubmit().setEnabled(true);
            }
        });
        try {
            view.getSubmit().setEnabled(false);
            builder.send();
        } catch (RequestException e) {
            App.get().getFailureHandler().handleClientErrorWithAction(
                    e, "Login, exception by sending request",
                    "Failed to send login request, uri=" + builder.getUrl());
            view.getSubmit().setEnabled(true);
        }
    }

    /**
     * Processes the login HTTP response.
     * @param response HTTP response
     */
    private void processLoginResponse(final Response response) {
        if (200 == response.getStatusCode()) {
            // success -> release the form
            view.getPassword().setText("");
            view.getUsername().setText("");
            App.get().switchToPresenterByHistory();
        } else if (401 == response.getStatusCode()) { // Authentication Failed
            // prepare validation messages
            List<InvalidValueSerializable> invalids = new LinkedList<InvalidValueSerializable>();
            invalids.add(new InvalidValueSerializable("badUsernamePassword", "usernameAndPassword"));
            ValidationException validationException =
                new ValidationException("Bad username/password!", invalids);
            validator.processServerErrors(validationException);
        } else {
            IllegalStateException illegalStateException = new IllegalStateException(
                "Authentication failed with HTTP status: " + response.getStatusCode());
            App.get().getFailureHandler().handleClientErrorWithAction(
                    illegalStateException, "Login, response processing", "unexpected status code");
        }
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
                if ("username".equals(propertyName)) {
                    result = CONSTANTS.username();
                } else if ("password".equals(propertyName)) {
                    result = CONSTANTS.password();
                } else if ("usernameAndPassword".equals(propertyName)) {
                    result = CONSTANTS.usernameAndPassword();
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

        validator.addValidators("username",
                new EmptyValidator(view.getUsername())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("password",
                new EmptyValidator(view.getPassword())
                    .addActionForFailure(focusAction)
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
    }

}
