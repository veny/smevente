package veny.smevente.client.uc;

import veny.smevente.client.App;
import veny.smevente.client.l10n.SmeventeConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the login form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class LoginViewImpl extends Composite implements LoginPresenter.LoginView {

    /** UI Binder interface. */
    @UiTemplate("login.ui.xml")
    interface Binder extends UiBinder<Widget, LoginViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    /** I18n constants. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    // CHECKSTYLE:OFF
    @UiField TextBox username;
    @UiField PasswordTextBox password;
    @UiField Button submit;
    @UiField DisclosurePanel validationErrors;
    @UiField Label buildNumber;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public LoginViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
        buildNumber.setText(CONSTANTS.version() + ": " + App.VERSION);
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getUsername() {
        return username;
    }

    /** {@inheritDoc} */
    @Override
    public PasswordTextBox getPassword() {
        return password;
    }

    /** {@inheritDoc} */
    @Override
    public Button getSubmit() {
        return submit;
    }

    /** {@inheritDoc} */
    @Override
    public DisclosurePanel getValidationErrors() {
        return validationErrors;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
