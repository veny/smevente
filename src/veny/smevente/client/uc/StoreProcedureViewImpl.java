package veny.smevente.client.uc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for storing a procedure.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 22.8.2010
 */
public class StoreProcedureViewImpl extends Composite
    implements StoreProcedurePresenter.StoreProcedureView {

    /** UI Binder interface. */
    @UiTemplate("storeProcedure.ui.xml")
    interface Binder extends UiBinder<Widget, StoreProcedureViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField TextBox name;
    @UiField TextArea messageText;
    @UiField TextBox time;
    @UiField Label timeLabel;
    @UiField TextBox color;
    @UiField Label colorLabel;
    @UiField Button selectColor;
    @UiField Button submit;
    @UiField Button cancel;
    @UiField DisclosurePanel validationErrors;
    @UiField Hidden procedureId;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public StoreProcedureViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public TextArea getMessageText() {
        return messageText;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getTime() {
        return time;
    }

    /** {@inheritDoc} */
    @Override
    public Label getTimeLabel() {
        return timeLabel;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getColor() {
        return color;
    }

    /** {@inheritDoc} */
    @Override
    public Label getColorLabel() {
        return colorLabel;
    }

    /** {@inheritDoc} */
    @Override
    public Button getSelectColor() {
        return selectColor;
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
    public Hidden getProcedureId() {
        return procedureId;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
