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
import veny.smevente.model.MedicalHelpCategoryDto;
import veny.smevente.shared.EntityTypeEnum;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
 * Find Patient presenter.
 *
 * @author Tomas Zajic [tomas.zajic75@gmail.com]
 * @since 28.7.2010
 */
public class MedicalHelpCategoryTypesPresenter
    extends AbstractPresenter<MedicalHelpCategoryTypesPresenter.MedicalHelpCategoryTypesView>
    implements HeaderHandler {

    /**
     * View interface for the medical help category form.
     *
     * @author Tomas Zajic
     * @since 28.7.2010
     */
    public interface MedicalHelpCategoryTypesView extends View {
        /**
         * Getter for the button to create new category.
         * @return the create new category element
         */
        Button getAddMhc();
        /**
         * Table with result set.
         * @return table with result set
         */
        FlexTable getResultTable();
    }

    /** Popup panel with context menu. */
    private final PopupPanel menuPopupPanel = new PopupPanel(true, true);

    /** List of found medical help categories. */
    private List<MedicalHelpCategoryDto> foundMedicalHelpCategories;

    /** ID of medical help category where the context menu is raised. */
    private String clickedId = null;

    /** Click handler to medical help category delete. */
    private ClickHandler menuClickHandler;

    /** The index of row in table on which the click event occured. Used to
     *  identify the category for which the update action will be started. */
    private int clickedRowIndex;

    /** Type of category to be used by presenter. */
    private short type = MedicalHelpCategoryDto.TYPE_STANDARD;

    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUnitSelection;

    /**
     * Constructor.
     * @param type the type of category
     */
    public MedicalHelpCategoryTypesPresenter(final short type) {
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

        view.getAddMhc().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().switchToPresenterByType(type == MedicalHelpCategoryDto.TYPE_STANDARD
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
                if (clickedRowIndex >= 0 && clickedRowIndex < foundMedicalHelpCategories.size()) {
                    final MedicalHelpCategoryDto mhc = foundMedicalHelpCategories.get(clickedRowIndex);
                    App.get().switchToPresenterByType(type == MedicalHelpCategoryDto.TYPE_STANDARD
                            ? PresenterEnum.STORE_MEDICAL_HELP_CATEGORY
                            : PresenterEnum.STORE_SPECIAL_MEDICAL_HELP_CATEGORY,
                            mhc);
                }
            }
        });

        // context menu
        final Command updateCommand = new Command() {
            public void execute() {
                final MedicalHelpCategoryDto mhc = hideMenuAndGetSelectedMedicalHelpCategory();
                App.get().switchToPresenterByType(type == MedicalHelpCategoryDto.TYPE_STANDARD
                        ? PresenterEnum.STORE_MEDICAL_HELP_CATEGORY
                        : PresenterEnum.STORE_SPECIAL_MEDICAL_HELP_CATEGORY,
                        mhc);
            }
        };
        final Command deleteCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                final int idx = getIndexById(clickedId);
                final MedicalHelpCategoryDto mhc = foundMedicalHelpCategories.get(idx);
                final String name = mhc.getName();
                final String confirmMsg = (type == MedicalHelpCategoryDto.TYPE_STANDARD
                        ? CONSTANTS.deleteMedicalHelpQuestion()[App.get().getSelectedUnitTextVariant()] + "\n" + name
                        : MESSAGES.deleteSpecialSmsQuestion(name));
                if (Window.confirm(confirmMsg)) {
                    deleteMedicalHelpCategory(clickedId, idx + 1);
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
        view.getAddMhc().setText(
                type == MedicalHelpCategoryDto.TYPE_STANDARD
                ? CONSTANTS.addMedicalHelp()[App.get().getSelectedUnitTextVariant()]
                : CONSTANTS.addSpecialSms());

        // unit info is loaded by HeaderEvent.unitChanged
        // but if the initial presenter is other one according to history token (e.g. FindPatient)
        // -> medical help category presenter not created -> not registered on Bus -> info is not loaded
        // [if App.get().getUnits() is null <- post login process in progress -> wait for HeaderEvent]
        if (null != App.get().getUnits()) {
            findMedicalHelpCategories();
        } else {
            App.get().switchToPresenterByType(PresenterEnum.CALENDER, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        cleanResultTable();
        clickedId = null;
        if (null != foundMedicalHelpCategories) {
            foundMedicalHelpCategories.clear();
            foundMedicalHelpCategories = null;
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Deletes the selected medical help category.
     * @param id medical help category ID
     * @param line line in the table to be removed
     */
    private void deleteMedicalHelpCategory(final String id, final int line) {
        final RestHandler rest = new RestHandler("/rest/unit/mhc/" + id + "/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final MedicalHelpCategoryDto mhc = new MedicalHelpCategoryDto();
                long idValue = Long.parseLong(id);
                mhc.setId(idValue);
                eventBus.fireEvent(new CrudEvent(EntityTypeEnum.MHC, OperationType.DELETE, mhc));
                view.getResultTable().removeRow(line);
                for (MedicalHelpCategoryDto foundCategory : foundMedicalHelpCategories) {
                    if (foundCategory.getId() == idValue) {
                        foundMedicalHelpCategories.remove(foundCategory);
                        break;
                    }
                }
            }
        });
        rest.delete();
    }

    /**
     * Finds medical help categories and show the result set.
     */
    private void findMedicalHelpCategories() {
        cleanResultTable();

        final RestHandler rest = new RestHandler("/rest/unit/" + App.get().getSelectedUnit().getId()
                + "/mhc/" + type);
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                foundMedicalHelpCategories = App.get().getJsonDeserializer().deserializeList(
                        MedicalHelpCategoryDto.class, "medicalHelpCategories", jsonText);
                int line = 1;
                for (MedicalHelpCategoryDto mhc : foundMedicalHelpCategories) {
                    addMedicalHelpCategory(mhc, line);
                    line++;
                }
            }
        });
        rest.get();
    }

    /**
     * Adds one medical help category into result set table.
     * @param mhc the medical help category
     * @param line line where the medical help category will be inserted on
     */
    private void addMedicalHelpCategory(final MedicalHelpCategoryDto mhc, final int line) {
        addCell(line, 0, new Label("" + line));
        addCell(line, 1, new Label(mhc.getName()));
        if (type == MedicalHelpCategoryDto.TYPE_STANDARD) {
            addCell(line, 3, new Label(mhc.getColor()));
            addCell(line, 4, new Label("" + mhc.getTime()));
            addCell(line, 5, new Label(mhc.getSmsText()));
            // color
            DOM.setStyleAttribute(view.getResultTable().getWidget(line, 3).getElement(),
                    "backgroundColor", "#" + mhc.getColor());
        } else {
            addCell(line, 3, new Label(mhc.getSmsText()));
        }
        final Image menuImg = new Image("images/menu_button.png");
        // medical help category ID is stored as element ID
        menuImg.getElement().setId(mhc.getId().toString());
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
     * Hides the popup menu and gets the medical help category where the action has been selected on.
     * @return selected medical help category
     */
    public MedicalHelpCategoryDto hideMenuAndGetSelectedMedicalHelpCategory() {
        menuPopupPanel.hide();
        final int idx = getIndexById(clickedId);
        return foundMedicalHelpCategories.get(idx);
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
     * Gets index of medical help category with given ID in collection of found medical help categories.
     * @param idAsText textually medical help category ID
     * @return index in found medical help categories (starting at 0)
     */
    private int getIndexById(final String idAsText) {
        if (null == foundMedicalHelpCategories) { throw new NullPointerException("patients collection is null"); }

        final Long id = Long.parseLong(idAsText);
        int i = 0;
        for (MedicalHelpCategoryDto mhc : foundMedicalHelpCategories) {
            if (id.longValue() == mhc.getId().longValue()) { return i; }
            i++;
        }
        throw new IllegalStateException("medical help category not found, id=" + idAsText
                + ", collection.size=" + foundMedicalHelpCategories.size());
    }
}
