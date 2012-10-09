package veny.smevente.client.uc;

import java.util.List;

import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * List of Procedures presenter.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 28.7.2010
 */
public class ProcedureListPresenter
    extends AbstractPresenter<ProcedureListPresenter.ProcedureListView>
    implements HeaderHandler {

    /**
     * View interface for the procedure list.
     *
     * @author Tomas Zajic
     * @since 28.7.2010
     */
    public interface ProcedureListView extends View {
        /**
         * Getter for the button to create new procedure.
         * @return button to create new procedure
         */
        Button getAddProcedure();
        /**
         * Table with result set.
         * @return table with result set
         */
        FlexTable getResultTable();
    }

    /** Popup panel with context menu. */
    private final PopupPanel menuPopupPanel = new PopupPanel(true, true);

    /** List of found procedures. */
    private List<Procedure> procedures;

    /** ID of procedure where the context menu is raised. */
    private String clickedId = null;

    /** Click handler to procedure delete. */
    private ClickHandler menuClickHandler;

    /** The index of row in table on which the click event occured. Used to
     *  identify the procedure for which the update action will be started. */
    private int clickedRowIndex;

    /** Type of procedure to be used by presenter. */
    private Event.Type type = Event.Type.IN_CALENDAR;

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    /**
     * Constructor.
     * @param type the type of procedure
     */
    public ProcedureListPresenter(final Event.Type type) {
        this.type = type;
    }

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

        view.getAddProcedure().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().switchToPresenterByType(type == Event.Type.IN_CALENDAR
                        ? PresenterEnum.STORE_MEDICAL_HELP_CATEGORY
                        : PresenterEnum.STORE_SPECIAL_MEDICAL_HELP_CATEGORY,
                        null);
            }
        });

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
                if (clickedRowIndex >= 0 && clickedRowIndex < procedures.size()) {
                    final Procedure proc = procedures.get(clickedRowIndex);
                    App.get().switchToPresenterByType(type == Event.Type.IN_CALENDAR
                            ? PresenterEnum.STORE_MEDICAL_HELP_CATEGORY
                            : PresenterEnum.STORE_SPECIAL_MEDICAL_HELP_CATEGORY,
                            proc);
                }
            }
        });

        // context menu
        final Command updateCommand = new Command() {
            public void execute() {
                final Procedure proc = hideMenuAndGetSelectedProcedure();
                App.get().switchToPresenterByType(type == Event.Type.IN_CALENDAR
                        ? PresenterEnum.STORE_MEDICAL_HELP_CATEGORY
                        : PresenterEnum.STORE_SPECIAL_MEDICAL_HELP_CATEGORY,
                        proc);
            }
        };
        final Command deleteCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                final int idx = getIndexById(clickedId);
                final Procedure proc = procedures.get(idx);
                final String name = proc.getName();
                final String confirmMsg = (type == Event.Type.IN_CALENDAR
                        ? CONSTANTS.deleteMedicalHelpQuestion()[App.get().getSelectedUnitTextVariant()] + "\n" + name
                        : MESSAGES.deleteSpecialSmsQuestion(name));
                if (Window.confirm(confirmMsg)) {
                    deleteProcedure(clickedId, idx + 1);
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
        // set unit specific text
        view.getAddProcedure().setText(
                type == Event.Type.IN_CALENDAR
                ? CONSTANTS.addMedicalHelp()[App.get().getSelectedUnitTextVariant()]
                : CONSTANTS.addSpecialSms());

        // unit info is loaded by HeaderEvent.unitChanged
        // but if the initial presenter is other one according to history token (e.g. FindPatient)
        // -> procedure presenter not created -> not registered on Bus -> info is not loaded
        // [if App.get().getUnits() is null <- post login process in progress -> wait for HeaderEvent]
        if (null != App.get().getMemberships()) {
            loadProcedures();
        } else {
            App.get().switchToPresenterByType(PresenterEnum.CALENDER, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        cleanResultTable();
        clickedId = null;
        if (null != procedures) {
            procedures.clear();
            procedures = null;
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Deletes the selected procedure.
     * @param id procedure ID
     * @param line line in the table to be removed
     */
    private void deleteProcedure(final String id, final int line) {
        final RestHandler rest = new RestHandler("/rest/unit/procedure/" + URL.encodePathSegment(id) + "/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final Procedure toDel = new Procedure();
                toDel.setId(id);
                eventBus.fireEvent(new CrudEvent(OperationType.DELETE, toDel));
                view.getResultTable().removeRow(line);
                for (Procedure proc : procedures) {
                    if (proc.getId().equals(id)) {
                        procedures.remove(proc);
                        break;
                    }
                }
            }
        });
        rest.delete();
    }

    /**
     * Loads procedures for currently selected unit and show the result set.
     */
    private void loadProcedures() {
        cleanResultTable();

        final RestHandler rest = new RestHandler("/rest/unit/"
                + URL.encodePathSegment(App.get().getSelectedUnit().getId().toString())
                + "/procedure/" + type);
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                procedures = App.get().getJsonDeserializer().deserializeList(Procedure.class, "procedures", jsonText);
                int line = 1;
                for (Procedure proc : procedures) {
                    addProcedure(proc, line);
                    line++;
                }
            }
        });
        rest.get();
    }

    /**
     * Adds one procedure into result set table.
     * @param proc the procedure to add
     * @param line line where the procedure will be inserted on
     */
    private void addProcedure(final Procedure proc, final int line) {
        addCell(line, 0, new Label("" + line));
        addCell(line, 1, new Label(proc.getName()));
        if (type == Event.Type.IN_CALENDAR) {
            addCell(line, 3, new Label(proc.getColor()));
            addCell(line, 4, new Label("" + proc.getTime()));
            addCell(line, 5, new Label(proc.getMessageText()));
            // color
            DOM.setStyleAttribute(view.getResultTable().getWidget(line, 3).getElement(),
                    "backgroundColor", "#" + proc.getColor());
        } else {
            addCell(line, 3, new Label(proc.getMessageText()));
        }
        final Image menuImg = new Image("images/menu_button.png");
        // procedure ID is stored as element ID
        menuImg.getElement().setId(proc.getId().toString());
        menuImg.addClickHandler(menuClickHandler);
        addCell(line, 2, menuImg);
    }

    /**
     * Adds cell into result set table.
     * @param row row
     * @param col column
     * @param w widget
     */
    private void addCell(final int row, final int col, final Widget w) {
        final String style  = (0 != (row % 2) ? "resultTable-cell-odd" : "resultTable-cell-even");
        view.getResultTable().setWidget(row, col, w);
        view.getResultTable().getFlexCellFormatter().addStyleName(row, col, style);
    }

    /**
     * Hides the popup menu and gets the procedure where the action has been selected on.
     * @return selected procedure
     */
    public Procedure hideMenuAndGetSelectedProcedure() {
        menuPopupPanel.hide();
        final int idx = getIndexById(clickedId);
        return procedures.get(idx);
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
     * Gets index of procedure with given ID in collection of found procedures.
     * @param idAsText textually procedure ID
     * @return index in found procedures (starting at 0)
     */
    private int getIndexById(final String idAsText) {
        if (null == procedures) { throw new NullPointerException("procedures collection is null"); }

        int i = 0;
        for (Procedure proc : procedures) {
            if (idAsText.equals(proc.getId())) { return i; }
            i++;
        }
        throw new IllegalStateException("procedure not found, id=" + idAsText
                + ", collection.size=" + procedures.size());
    }

}
