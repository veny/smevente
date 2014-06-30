package veny.smevente.client.uc;

import veny.smevente.client.l10n.SmeventeConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the Customer History.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.1.2011
 */
public class CustomerHistoryViewImpl extends Composite implements CustomerHistoryPresenter.CustomerHistoryView {

    /** UI Binder interface. */
    @UiTemplate("customerHistory.ui.xml")
    interface Binder extends UiBinder<Widget, CustomerHistoryViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    /** I18n constants. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    // CHECKSTYLE:OFF
    @UiField Label fullname;
    @UiField FlexTable resultTable;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public CustomerHistoryViewImpl() {
        initWidget(BINDER.createAndBindUi(this));

        getResultTable().setWidget(0, 0, new Label(CONSTANTS.date()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 0, "resultTable-header-cell");
        getResultTable().getFlexCellFormatter().addStyleName(0, 0, "resultTable-header-thin");
        getResultTable().setWidget(0, 1, new Label(CONSTANTS.time()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 1, "resultTable-header-cell");
        getResultTable().getFlexCellFormatter().addStyleName(0, 1, "resultTable-header-thin");
        getResultTable().setWidget(0, 2, new Label(CONSTANTS.unitMember()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 2, "resultTable-header-cell");
        getResultTable().getFlexCellFormatter().addStyleName(0, 2, "resultTable-header-thin");
        // getResultTable().setWidget(0, 3, new Label(CONSTANTS.medicalHelp())); depends on unit type
        getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-cell");
        getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-thin");
        getResultTable().setWidget(0, 4, new Label(CONSTANTS.notice()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 4, "resultTable-header-cell");
    }

    /** {@inheritDoc} */
    @Override
    public Label getFullname() {
        return fullname;
    }

    /** {@inheritDoc} */
    @Override
    public FlexTable getResultTable() {
        return resultTable;
    }

}
