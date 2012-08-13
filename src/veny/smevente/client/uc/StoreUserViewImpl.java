package veny.smevente.client.uc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the login form.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 26.11.2011
 */
public class StoreUserViewImpl extends Composite implements StoreUserPresenter.StoreUserView {

    /** UI Binder interface. */
    @UiTemplate("storeUser.ui.xml")
    interface Binder extends UiBinder<Widget, StoreUserViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField TextBox username;
    @UiField TextBox fullname;
    @UiField CheckBox updatePassword;
    @UiField PasswordTextBox password;
    @UiField PasswordTextBox passwordAgain;
    @UiField CheckBox unitAdmin;
    @UiField TextBox unitOrder;
    @UiField Button submit;
    @UiField Button cancel;
    @UiField DisclosurePanel validationErrors;
    @UiField Hidden userId;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public StoreUserViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getUsername() {
        return username;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getFullname() {
        return fullname;
    }

    /** {@inheritDoc} */
    @Override
    public CheckBox getUpdatePassword() {
        return updatePassword;
    }

    /** {@inheritDoc} */
    @Override
    public PasswordTextBox getPassword() {
        return password;
    }

    /** {@inheritDoc} */
    @Override
    public PasswordTextBox getPasswordAgain() {
        return passwordAgain;
    }

    /** {@inheritDoc} */
    @Override
    public CheckBox getUnitAdmin() {
        return unitAdmin;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getUnitOrder() {
        return unitOrder;
    }

    /** {@inheritDoc} */
    @Override
    public HasClickHandlers getSubmit() {
        return submit;
    }

    /** {@inheritDoc} */
    @Override
    public HasClickHandlers getCancel() {
        return cancel;
    }

    /** {@inheritDoc} */
    @Override
    public DisclosurePanel getValidationErrors() {
        return validationErrors;
    }

    /** {@inheritDoc} */
    @Override
    public Hidden getUserId() {
        return userId;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
