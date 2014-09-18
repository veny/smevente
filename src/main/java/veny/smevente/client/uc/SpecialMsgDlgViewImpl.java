package veny.smevente.client.uc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the Special message dialog.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 11.09.2010
 */
public class SpecialMsgDlgViewImpl extends Composite implements SpecialMsgDlgPresenter.SpecialMsgDlgView {

    /** UI Binder interface. */
    @UiTemplate("specialMsgDlg.ui.xml")
    interface Binder extends UiBinder<Widget, SpecialMsgDlgViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField ListBox type;
    @UiField TextBox fullname;
    @UiField TextBox birthNumber;
    @UiField TextArea msgText;
    @UiField DisclosurePanel validationErrors;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public SpecialMsgDlgViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getFullname() {
        return fullname;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getBirthNumber() {
        return birthNumber;
    }

    /** {@inheritDoc} */
    @Override
    public TextArea getMsgText() {
        return msgText;
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
