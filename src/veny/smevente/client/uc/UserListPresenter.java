package veny.smevente.client.uc;

import java.util.List;

import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.client.utils.UiUtils;
import veny.smevente.model.Membership;
import veny.smevente.model.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * 'Users in Unit' presenter.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 28.7.2010
 */
public class UserListPresenter
    extends AbstractPresenter<UserListPresenter.UserListView>
    implements HeaderHandler {

    /**
     * View interface for Users in Unit UC.
     *
     * @author Vaclav Sykora
     * @since 28.7.2010
     */
    public interface UserListView extends View {
        /**
         * Getter for the button to create new user.
         * @return button to create new user
         */
        Button getAddUser();
        /**
         * Table with result set.
         * @return table with result set
         */
        FlexTable getResultTable();
    }

    /** Popup panel with context menu. */
    private final PopupPanel menuPopupPanel = new PopupPanel(true, true);

    /** List of found memberships with associated users. */
    private List<Membership> membershipsWithUser;

    /** ID of user where the context menu is raised. */
    private String clickedId = null;

    /** The index of row in table on which the click event occured. Used to
     *  identify the user for which the update action will be started. */
    private int clickedRowIndex;

    /** Click handler to user delete. */
    private ClickHandler menuClickHandler;

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    // -------------------------------------------------- HeaderHandler Methods

    /** {@inheritDoc} */
    @Override
    public void unitChanged(final HeaderEvent event) {
        onShow(null);
    }
    /** {@inheritDoc} */
    @Override
    public void unitMemberChanged(final HeaderEvent event) { /* I don't care */ }
    /** {@inheritDoc} */
    @Override
    public void dateChanged(final HeaderEvent event) { /* I don't care */ }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        // register this to display/hide the loading progress bar
        ebusUnitSelection = eventBus.addHandler(HeaderEvent.TYPE, this);

        menuClickHandler = new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                clickedId = event.getRelativeElement().getId();
                menuPopupPanel.setPopupPosition(
                        event.getRelativeElement().getAbsoluteRight(),
                        event.getRelativeElement().getAbsoluteTop());
                menuPopupPanel.setVisible(true);
                menuPopupPanel.show();
            }
        };

        view.getAddUser().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().switchToPresenterByType(PresenterEnum.STORE_USER, null);
            }
        });

        // this is workaround because method 'getCellForEvent' is not callable with 'DoubleClickEvent'
        view.getResultTable().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                // the row index starts form "1"
                clickedRowIndex = view.getResultTable().getCellForEvent(event).getRowIndex() - 1;
            }
        });

        view.getResultTable().addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(final DoubleClickEvent event) {
                if (clickedRowIndex >= 0 && clickedRowIndex < membershipsWithUser.size()) {
                    final User p = membershipsWithUser.get(clickedRowIndex).getUser();
                    App.get().switchToPresenterByType(PresenterEnum.STORE_USER, p);
                }
            }
        });

        // context menu
        final Command updateCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                final int idx = getIndexById(clickedId);
                final User u = membershipsWithUser.get(idx).getUser();
                App.get().switchToPresenterByType(PresenterEnum.STORE_USER, u);
            }
        };
        final Command deleteCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                final int idx = getIndexById(clickedId);
                final Membership memb = membershipsWithUser.get(idx);
                final String name = memb.getUser().getUsername();
                if (Window.confirm(MESSAGES.deleteUserQuestion(name))) {
                    deleteUser(memb, idx + 1);
                }
                clickedId = null;
            }
        };
        final MenuBar popupMenuBar = new MenuBar(true);
        final MenuItem updateItem = new MenuItem(CONSTANTS.update(), true, updateCommand);
        final MenuItem deleteItem = new MenuItem(CONSTANTS.delete(), true, deleteCommand);
        popupMenuBar.addItem(updateItem);
        popupMenuBar.addItem(deleteItem);
        menuPopupPanel.add(popupMenuBar);
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        ebusUnitSelection.removeHandler();
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object parameter) {
        loadUsers();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        cleanResultTable();
        clickedId = null;
        if (null != membershipsWithUser) {
            membershipsWithUser.clear();
            membershipsWithUser = null;
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Finds users and show the result set.
     * @param memb membership with associated user to delete
     * @param line line in the table to be removed
     */
    private void deleteUser(final Membership memb, final int line) {
        throw new IllegalStateException("not implemented yet");
//        final RestHandler rest = new RestHandler("/rest/user/" + memb.getUser().getId() + "/");
//        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
//            @Override
//            public void onSuccess(final String jsonText) {
//                eventBus.fireEvent(new CrudEvent(OperationType.DELETE, memb.getUser()));
//                view.getResultTable().removeRow(line);
//                for (Membership foundMemb : membershipsWithUser) {
//                    if (foundMemb.getId().equals(memb.getId())) {
//                        membershipsWithUser.remove(foundUser);
//                        break;
//                    }
//                }
//            }
//        });
//        rest.delete();
    }

    /**
     * Load users of selected unit and show the result set.
     */
    private void loadUsers() {
        cleanResultTable();
        final RestHandler rest = new RestHandler("/rest/user"
                + "/?unitId=" + URL.encodeQueryString(App.get().getSelectedUnit().getId().toString().trim())
        );
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                membershipsWithUser =
                        App.get().getJsonDeserializer().deserializeList(Membership.class, "memberships", jsonText);
                int line = 1;
                for (Membership memb : membershipsWithUser) {
                    addUser(memb, line);
                    line++;
                }
            }
        });
        rest.get();
    }

    /**
     * Adds one user into result set table.
     * @param memb the membership with associated user
     * @param line line where the user will be inserted on
     */
    private void addUser(final Membership memb, final int line) {
        final FlexTable table = view.getResultTable();
        UiUtils.addCell(table, line, 0, new Label("" + line));
        UiUtils.addCell(table, line, 1, new Label(memb.getUser().getUsername()));
        UiUtils.addCell(table, line, 3, new Label(memb.getUser().getFullname()));
        final CheckBox isUnitAdmin = new CheckBox();
        isUnitAdmin.setValue(memb.enumRole().equals(Membership.Role.ADMIN)); // TODO [veny,C] should be text
        isUnitAdmin.setEnabled(false);
        UiUtils.addCell(table, line, 4, isUnitAdmin);
        final Image menuImg = new Image("images/menu_button.png");
        // user ID is stored as element ID
        menuImg.getElement().setId(memb.getUser().getId().toString());
        menuImg.addClickHandler(menuClickHandler);
        UiUtils.addCell(table, line, 2, menuImg);
    }

    /**
     * Cleans the result set table.
     */
    private void cleanResultTable() {
        final int rows = view.getResultTable().getRowCount();
        for (int i = rows - 1; i > 0; i--) {
            view.getResultTable().removeRow(i);
        }
    }

    /**
     * Gets index of user with given ID in collection of found users.
     * @param idAsText textually user ID
     * @return index in found users (starting at 0)
     */
    private int getIndexById(final String idAsText) {
        if (null == membershipsWithUser) { throw new NullPointerException("users collection is null"); }

        int i = 0;
        for (Membership m : membershipsWithUser) {
            if (idAsText.equals(m.getUser().getId())) { return i; }
            i++;
        }
        throw new IllegalStateException("user not found, id=" + idAsText
                + ", collection.size=" + membershipsWithUser.size());
    }

}
