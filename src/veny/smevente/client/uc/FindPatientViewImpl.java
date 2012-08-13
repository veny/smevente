package veny.smevente.client.uc;

import veny.smevente.client.l10n.SmeventeConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the login form.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class FindPatientViewImpl extends Composite implements FindPatientPresenter.FindPatientView {

    /** UI Binder interface. */
    @UiTemplate("findPatient.ui.xml")
    interface Binder extends UiBinder<Widget, FindPatientViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    /** I18n messages. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    // CHECKSTYLE:OFF
    @UiField TextBox name;
    @UiField TextBox phoneNumber;
    @UiField TextBox birthNumber;
    @UiField Button submit;
    @UiField FlexTable resultTable;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public FindPatientViewImpl() {
        initWidget(BINDER.createAndBindUi(this));

        getResultTable().setWidget(0, 0, new Label(""));
        getResultTable().getFlexCellFormatter().addStyleName(0, 0, "resultTable-header-cell");
        getResultTable().setWidget(0, 1, new Label(CONSTANTS.name()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 1, "resultTable-header-cell");
        getResultTable().setWidget(0, 2, new Label(""));
        getResultTable().getFlexCellFormatter().addStyleName(0, 2, "resultTable-header-cell");
        getResultTable().setWidget(0, 3, new Label(CONSTANTS.phoneNumber()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-cell");
        getResultTable().setWidget(0, 4, new Label(CONSTANTS.birthNumber()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 4, "resultTable-header-cell");
        getResultTable().setWidget(0, 5, new Label(CONSTANTS.degree()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 5, "resultTable-header-cell");
        getResultTable().setWidget(0, 6, new Label(CONSTANTS.street()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 6, "resultTable-header-cell");
        getResultTable().setWidget(0, 7, new Label(CONSTANTS.city()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 7, "resultTable-header-cell");
        getResultTable().setWidget(0, 8, new Label(CONSTANTS.zipCode()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 8, "resultTable-header-cell");
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getPhoneNumber() {
        return phoneNumber;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getBirthNumber() {
        return birthNumber;
    }

    /** {@inheritDoc} */
    @Override
    public HasClickHandlers getSubmit() {
        return submit;
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
