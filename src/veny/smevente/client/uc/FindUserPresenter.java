package veny.smevente.client.uc;

import java.util.List;

import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.client.utils.UiUtils;
import veny.smevente.model.MembershipDto;
import veny.smevente.model.MembershipDto.Type;
import veny.smevente.model.User;
import veny.smevente.shared.EntityTypeEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Find User presenter.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 28.7.2010
 */
public class FindUserPresenter
    extends AbstractPresenter<FindUserPresenter.FindUserView>
    implements HeaderHandler {

    /**
     * View interface for the login form.
     *
     * @author Vaclav Sykora
     * @since 0.1
     */
    public interface FindUserView extends View {
        /**
         * Getter for the user name text field.
         * @return the input field for the user name
         */
        TextBox getUserName();
        /**
         * Getter for the full name text field.
         * @return the input field for the full name
         */
        TextBox getFullName();
        /**
         * Getter for the flag is user is an unit administrator.
         * @return the check box for the unit administrator flag
         */
        CheckBox getUnitAdmin();
        /**
         * Getter for the unit administrator label field.
         * @return the label for the unit administrator
         */
        Label getUnitAdminLabel();
        /**
         * Getter for the button to submit.
         * @return the submit element
         */
        HasClickHandlers getSubmit();
        /**
         * Table with result set.
         * @return table with result set
         */
        FlexTable getResultTable();
    }

    /** Popup panel with context menu. */
    private final PopupPanel menuPopupPanel = new PopupPanel(true, true);

    /** List of found users. */
    private List<User> foundUsers;

    /** ID of user where the context menu is raised. */
    private String clickedId = null;

    /** Click handler to user delete. */
    private ClickHandler menuClickHandler;

    /** The index of row in table on which the click event occured. Used to
     *  identify the user for which the update action will be started. */
    private int clickedRowIndex;

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    // -------------------------------------------------- HeaderHandler Methods

    /** {@inheritDoc} */
    @Override
    public void unitChanged(final HeaderEvent event) {
        clean();
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

        view.getUnitAdmin().setVisible(false);
        view.getUnitAdminLabel().setVisible(false);
        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                findUsers();
            }
        });

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
                if (clickedRowIndex >= 0 && clickedRowIndex < foundUsers.size()) {
                    final User p = foundUsers.get(clickedRowIndex);
                    App.get().switchToPresenterByType(PresenterEnum.STORE_USER, p);
                }
            }
        });

        // context menu
        final Command updateCommand = new Command() {
            public void execute() {
                final User u = hideMenuAndGetSelectedUser();
                App.get().switchToPresenterByType(PresenterEnum.STORE_USER, u);
            }
        };
        final Command deleteCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                final int idx = getIndexById(clickedId);
                final User u = foundUsers.get(idx);
                final String name = u.getUsername();
                if (Window.confirm(MESSAGES.deleteUserQuestion(name))) {
                    deleteUser(clickedId, idx + 1);
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
        view.getUserName().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getUserName().setText("");
        view.getFullName().setText("");
        view.getUnitAdmin().setValue(false);
        cleanResultTable();
        clickedId = null;
        if (null != foundUsers) {
            foundUsers.clear();
            foundUsers = null;
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Finds users and show the result set.
     * @param id user ID
     * @param line line in the table to be removed
     */
    private void deleteUser(final String id, final int line) {
        final RestHandler rest = new RestHandler("/rest/user/" + id + "/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final User user = new User();
                long idValue = Long.parseLong(id);
                user.setId(idValue);
                eventBus.fireEvent(new CrudEvent(EntityTypeEnum.USER, OperationType.DELETE, user));
                view.getResultTable().removeRow(line);
                for (User foundUser : foundUsers) {
                    if (foundUser.getId() == idValue) {
                        foundUsers.remove(foundUser);
                        break;
                    }
                }
            }
        });
        rest.delete();
    }

    /**
     * Finds users and show the result set.
     */
    private void findUsers() {
        cleanResultTable();
        final RestHandler rest = new RestHandler("/rest/user"
                + "/?unitId=" + App.get().getSelectedUnit().getId().toString().trim()
                + "&username=" + URL.encodeQueryString(view.getUserName().getText().trim())
                + "&fullname=" + URL.encodeQueryString(view.getFullName().getText().trim())
        );
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                foundUsers = App.get().getJsonDeserializer().deserializeList(User.class, "users", jsonText);
                int line = 1;
                for (User u : foundUsers) {
                    addUser(u, line);
                    line++;
                }
            }
        });
        rest.get();
    }

    /**
     * Adds one user into result set table.
     * @param u the user
     * @param line line where the user will be inserted on
     */
    private void addUser(final User u, final int line) {
        final FlexTable table = view.getResultTable();
        UiUtils.addCell(table, line, 0, new Label("" + line));
        UiUtils.addCell(table, line, 1, new Label(u.getUsername()));
        UiUtils.addCell(table, line, 3, new Label(u.getFullname()));
        CheckBox isUnitAdmin = new CheckBox();
        isUnitAdmin.setValue(isAdmin(u));
        isUnitAdmin.setEnabled(false);
        UiUtils.addCell(table, line, 4, isUnitAdmin);
        final Image menuImg = new Image("images/menu_button.png");
        // user ID is stored as element ID
        menuImg.getElement().setId(u.getId().toString());
        menuImg.addClickHandler(menuClickHandler);
        UiUtils.addCell(table, line, 2, menuImg);
    }

    /**
     * checks if specified user is an administrator of selected unit.
     * @param user the user to be checked if administrator of selected unit
     * @return true if specified user is an administrator of selected unit
     */
    private boolean isAdmin(final User user) {
        List<MembershipDto> unitMembers = App.get().getSelectedUnit().getMembers();

        // just to be sure
        if (null == unitMembers) { throw new IllegalStateException("selected unit members cannot be null"); }

        for (MembershipDto unitMember: unitMembers) {
            if (unitMember.getUser().getId() == user.getId()) {
                return Type.ADMIN == unitMember.getType();
            }
        }

        throw new IllegalStateException("user with ID=" + user.getId() + " is not member of any unit");
    }

    /**
     * Hides the popup menu and gets the user where the action has been selected on.
     * @return selected user
     */
    public User hideMenuAndGetSelectedUser() {
        menuPopupPanel.hide();
        final int idx = getIndexById(clickedId);
        return foundUsers.get(idx);
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
        if (null == foundUsers) { throw new NullPointerException("users collection is null"); }

        final Long id = Long.parseLong(idAsText);
        int i = 0;
        for (User u : foundUsers) {
            if (id.longValue() == u.getId().longValue()) { return i; }
            i++;
        }
        throw new IllegalStateException("user not found, id=" + idAsText
                + ", collection.size=" + foundUsers.size());
    }

}
