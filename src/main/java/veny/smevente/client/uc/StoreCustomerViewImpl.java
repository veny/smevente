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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the login form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class StoreCustomerViewImpl extends Composite implements StoreCustomerPresenter.StoreCustomerView {

    /** UI Binder interface. */
    @UiTemplate("storeCustomer.ui.xml")
    interface Binder extends UiBinder<Widget, StoreCustomerViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField TextBox firstname;
    @UiField TextBox surname;
    @UiField TextBox email;
    @UiField CheckBox emailChannel;
    @UiField TextBox phoneNumber;
    @UiField CheckBox smsChannel;
    @UiField TextBox birthNumber;
    @UiField TextBox degree;
    @UiField TextBox street;
    @UiField TextBox city;
    @UiField TextBox zipCode;
    @UiField TextBox employer;
    @UiField TextBox careers;
    @UiField Button submit;
    @UiField Button cancel;
    @UiField DisclosurePanel validationErrors;
    @UiField Hidden customerId;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public StoreCustomerViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getFirstname() {
        return firstname;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getSurname() {
        return surname;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getEmail() {
        return email;
    }
    /** {@inheritDoc} */
    @Override
    public CheckBox getEmailChannel() {
        return emailChannel;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getPhoneNumber() {
        return phoneNumber;
    }
    /** {@inheritDoc} */
    @Override
    public CheckBox getSmsChannel() {
        return smsChannel;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getBirthNumber() {
        return birthNumber;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getDegree() {
        return degree;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getStreet() {
        return street;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getCity() {
        return city;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getZipCode() {
        return zipCode;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getEmployer() {
        return employer;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getCareers() {
        return careers;
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
    public Hidden getCustomerId() {
        return customerId;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
