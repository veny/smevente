package veny.smevente.client.uc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the 'change password' form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 19.9.2010
 */
public class ChangePasswordViewImpl extends Composite implements ChangePasswordPresenter.ChangePasswordView {

    /** UI Binder interface. */
    @UiTemplate("changePassword.ui.xml")
    interface Binder extends UiBinder<Widget, ChangePasswordViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField PasswordTextBox old;
    @UiField PasswordTextBox newP;
    @UiField PasswordTextBox newAgain;
    @UiField Button submit;
    @UiField DisclosurePanel validationErrors;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public ChangePasswordViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public PasswordTextBox getOldPassword() {
        return old;
    }

    /** {@inheritDoc} */
    @Override
    public PasswordTextBox getNewPassword() {
        return newP;
    }

    /** {@inheritDoc} */
    @Override
    public PasswordTextBox getNewPasswordAgain() {
        return newAgain;
    }

    /** {@inheritDoc} */
    @Override
    public HasClickHandlers getSubmit() {
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
