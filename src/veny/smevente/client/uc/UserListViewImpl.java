package veny.smevente.client.uc;

import veny.smevente.client.l10n.SmeventeConstants;

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
 * View implementation for the 'Users in Unit' UC.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 25.03.2011
 */
public class UserListViewImpl extends Composite implements UserListPresenter.UserListView {

    /** UI Binder interface. */
    @UiTemplate("userList.ui.xml")
    interface Binder extends UiBinder<Widget, UserListViewImpl> { }
    /** UI Binder. */
    private static final Binder BINDER = GWT.create(Binder.class);

    /** I18n messages. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    // CHECKSTYLE:OFF
    @UiField Button addUser;
    @UiField FlexTable resultTable;
    // CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public UserListViewImpl() {
        initWidget(BINDER.createAndBindUi(this));

        getResultTable().setWidget(0, 0, new Label(""));
        getResultTable().getFlexCellFormatter().addStyleName(0, 0, "resultTable-header-cell");
        getResultTable().setWidget(0, 1, new Label(CONSTANTS.username()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 1, "resultTable-header-cell");
        getResultTable().setWidget(0, 2, new Label(""));
        getResultTable().getFlexCellFormatter().addStyleName(0, 2, "resultTable-header-cell");
        getResultTable().setWidget(0, 3, new Label(CONSTANTS.fullname()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 3, "resultTable-header-cell");
        getResultTable().setWidget(0, 4, new Label(CONSTANTS.unitRole()));
        getResultTable().getFlexCellFormatter().addStyleName(0, 4, "resultTable-header-cell");
    }


    /** {@inheritDoc} */
    @Override
    public Button getAddUser() {
        return addUser;
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
