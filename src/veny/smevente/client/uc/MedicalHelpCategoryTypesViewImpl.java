package veny.smevente.client.uc;

import veny.smevente.client.l10n.SmeventeConstants;
import veny.smevente.model.Event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * View implementation for the medical help category form.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 28.7.2010
 */
public class MedicalHelpCategoryTypesViewImpl extends Composite
    implements MedicalHelpCategoryTypesPresenter.MedicalHelpCategoryTypesView {

    /** UI Binder interface. */
    @UiTemplate("medicalHelpCategoryTypes.ui.xml")
    interface Binder extends UiBinder<Widget, MedicalHelpCategoryTypesViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    /** I18n messages. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    // CHECKSTYLE:OFF
    @UiField Button addMhc;
    @UiField FlexTable resultTable;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     * @param type the type of category
     */
    public MedicalHelpCategoryTypesViewImpl(final Event.Type type) {
        initWidget(BINDER.createAndBindUi(this));

        getResultTable().setWidget(0, 0, new Label(""));
        getResultTable().getFlexCellFormatter().addStyleName(0, 0, "resultTable-header-cell");
        getResultTable().setWidget(0, 1, new Label(CONSTANTS.name()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 1, "resultTable-header-cell");
        getResultTable().setWidget(0, 2, new Label(""));
        getResultTable().getFlexCellFormatter().addStyleName(0, 2, "resultTable-header-cell");
        if (type == Event.Type.IN_CALENDAR) {
            getResultTable().setWidget(0, 3, new Label(CONSTANTS.color()));
            getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-cell");
            getResultTable().setWidget(0, 4, new Label(CONSTANTS.time()));
            getResultTable().getFlexCellFormatter().addStyleName(0, 4, "resultTable-header-cell");
            getResultTable().setWidget(0, 5, new Label(CONSTANTS.smsText()));
            getResultTable().getFlexCellFormatter().addStyleName(0, 5, "resultTable-header-cell");
        } else {
            getResultTable().setWidget(0, 3, new Label(CONSTANTS.smsText()));
            getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-cell");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Button getAddMhc() {
        return addMhc;
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
