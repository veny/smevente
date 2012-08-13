package veny.smevente.client.uc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the login form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class SmsDetailDlgViewImpl extends Composite implements SmsDetailDlgPresenter.SmsDetailDlgView {

    /** UI Binder interface. */
    @UiTemplate("smsDetailDlg.ui.xml")
    interface Binder extends UiBinder<Widget, SmsDetailDlgViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField Label date;
    @UiField Label time;
    @UiField Label medicalHelpLabel;
    @UiField Label medicalHelp;
    @UiField Label name;
    @UiField Label phoneNumber;
    @UiField Label smsText;
    @UiField Label notice;
    @UiField Label sent;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public SmsDetailDlgViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public Label getDate() {
        return date;
    }

    /** {@inheritDoc} */
    @Override
    public Label getTime() {
        return time;
    }

    /** {@inheritDoc} */
    @Override
    public Label getMedicalHelpLabel() {
        return medicalHelpLabel;
    }

    /** {@inheritDoc} */
    @Override
    public Label getMedicalHelp() {
        return medicalHelp;
    }

    /** {@inheritDoc} */
    @Override
    public Label getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public Label getPhoneNumber() {
        return phoneNumber;
    }

    /** {@inheritDoc} */
    @Override
    public Label getSmsText() {
        return smsText;
    }

    /** {@inheritDoc} */
    @Override
    public Label getNotice() {
        return notice;
    }

    /** {@inheritDoc} */
    @Override
    public Label getSent() {
        return sent;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
