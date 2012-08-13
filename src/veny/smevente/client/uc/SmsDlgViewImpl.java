package veny.smevente.client.uc;

import veny.smevente.client.utils.PatientNameSuggestOracle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the login form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class SmsDlgViewImpl extends Composite implements SmsDlgPresenter.SmsDlgView {

    /** UI Binder interface. */
    @UiTemplate("smsDlg.ui.xml")
    interface Binder extends UiBinder<Widget, SmsDlgViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField InlineLabel date;
    @UiField ListBox startHour;
    @UiField ListBox startMinute;
    @UiField Label medicalHelpHeader;
    @UiField ListBox medicalHelp;
    @UiField ListBox medicalHelpLength;
    @UiField(provided = true) SuggestBox nameSuggestBox;
    @UiField TextBox phoneNumber;
    @UiField TextArea smsText;
    @UiField TextArea notice;
    @UiField DisclosurePanel validationErrors;
    @UiField Hidden smsId;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public SmsDlgViewImpl() {
        PatientNameSuggestOracle oracle = new PatientNameSuggestOracle();
        nameSuggestBox = new SuggestBox(oracle);

        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public InlineLabel getDate() {
        return date;
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getStartHour() {
        return startHour;
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getStartMinute() {
        return startMinute;
    }

    /** {@inheritDoc} */
    @Override
    public Label getMedicalHelpHeader() {
        return medicalHelpHeader;
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getMedicalHelp() {
        return medicalHelp;
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getMedicalHelpLength() {
        return medicalHelpLength;
    }

    /** {@inheritDoc} */
    @Override
    public SuggestBox getNameSuggestBox() {
        return nameSuggestBox;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getPhoneNumber() {
        return phoneNumber;
    }

    /** {@inheritDoc} */
    @Override
    public TextArea getSmsText() {
        return smsText;
    }

    /** {@inheritDoc} */
    @Override
    public TextArea getNotice() {
        return notice;
    }

    /** {@inheritDoc} */
    @Override
    public DisclosurePanel getValidationErrors() {
        return validationErrors;
    }

    /** {@inheritDoc} */
    @Override
    public Hidden getSmsId() {
        return smsId;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
