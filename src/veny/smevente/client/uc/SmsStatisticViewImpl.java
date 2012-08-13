package veny.smevente.client.uc;

import veny.smevente.client.l10n.SmeventeConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * View implementation for the SMS statistic.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 31.8.2010
 */
public class SmsStatisticViewImpl extends Composite implements SmsStatisticPresenter.SmsStatisticView {

    /** UI Binder interface. */
    @UiTemplate("smsStatistic.ui.xml")
    interface Binder extends UiBinder<Widget, SmsStatisticViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    /** I18n constants. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    // CHECKSTYLE:OFF
    @UiField DateBox from;
    @UiField DateBox to;
    @UiField Button submit;
    @UiField DisclosurePanel validationErrors;
    @UiField Image chart;
    @UiField FlexTable resultTable;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public SmsStatisticViewImpl() {
        initWidget(BINDER.createAndBindUi(this));

        getResultTable().setWidget(0, 0, new Label(CONSTANTS.name()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 0, "resultTable-header-cell");
        getResultTable().setWidget(0, 1, new Label(CONSTANTS.overall()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 1, "resultTable-header-cell");
        getResultTable().setWidget(0, 2, new Label(CONSTANTS.sent()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 2, "resultTable-header-cell");
        getResultTable().setWidget(0, 3, new Label(CONSTANTS.failed()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-cell");
        getResultTable().setWidget(0, 4, new Label(CONSTANTS.deleted()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 4, "resultTable-header-cell");
    }

    /** {@inheritDoc} */
    @Override
    public DateBox getFrom() {
        return from;
    }

    /** {@inheritDoc} */
    @Override
    public DateBox getTo() {
        return to;
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
    public Image getChart() {
        return chart;
    }

    /** {@inheritDoc} */
    @Override
    public FlexTable getResultTable() {
        return resultTable;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
