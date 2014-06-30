package veny.smevente.client.uc;

import veny.smevente.client.utils.CustomerNameSuggestOracle;

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
public class EventDlgViewImpl extends Composite implements EventDlgPresenter.EventDlgView {

    /** UI Binder interface. */
    @UiTemplate("eventDlg.ui.xml")
    interface Binder extends UiBinder<Widget, EventDlgViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField InlineLabel date;
    @UiField ListBox startHour;
    @UiField ListBox startMinute;
    @UiField Label procedureHeader;
    @UiField ListBox procedure;
    @UiField ListBox length;
    @UiField(provided = true) SuggestBox nameSuggestBox;
    @UiField TextBox phoneNumber;
    @UiField TextArea messageText;
    @UiField TextArea notice;
    @UiField DisclosurePanel validationErrors;
    @UiField Hidden eventId;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public EventDlgViewImpl() {
        CustomerNameSuggestOracle oracle = new CustomerNameSuggestOracle();
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
    public Label getProcedureHeader() {
        return procedureHeader;
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getProcedure() {
        return procedure;
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getLength() {
        return length;
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
    public TextArea getMessageText() {
        return messageText;
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
    public Hidden getEventId() {
        return eventId;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
