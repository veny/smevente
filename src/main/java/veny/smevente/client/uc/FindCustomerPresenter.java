package veny.smevente.client.uc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.SmeventeDialog;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestCallback;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.CrudEvent.OperationType;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.client.utils.UiUtils;
import veny.smevente.model.Event;
import veny.smevente.model.Customer;
import veny.smevente.model.Procedure;
import veny.smevente.shared.ExceptionJsonWrapper;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Find Patient presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 28.7.2010
 */
public class FindCustomerPresenter
    extends AbstractPresenter<FindCustomerPresenter.FindCustomerView>
    implements HeaderHandler {

    /**
     * View interface for the login form.
     *
     * @author Vaclav Sykora
     * @since 0.1
     */
    public interface FindCustomerView extends View {
        /**
         * Getter for the name text field.
         * @return the input field for the name
         */
        TextBox getName();
        /**
         * Getter for the phone number text field.
         * @return the input field for the phone number
         */
        TextBox getPhoneNumber();
        /**
         * Getter for the birth number text field.
         * @return the input field for the birth number
         */
        TextBox getBirthNumber();
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

    /** List of found customers. */
    private List<Customer> foundCustomers;

    /** ID of customer where the context menu is raised. */
    private String clickedId = null;

    /** Click handler to customer delete. */
    private ClickHandler menuClickHandler;

    /** The index of row in table on which the click event occured. Used to
     *  identify the customer for which the update action will be started. */
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

        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                findCustomers();
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
                if (clickedRowIndex >= 0 && clickedRowIndex < foundCustomers.size()) {
                    final Customer p = foundCustomers.get(clickedRowIndex);
                    App.get().switchToPresenterByType(PresenterEnum.STORE_PATIENT, p);
                }
            }
        });

        // context menu
        final Command updateCommand = new Command() {
            public void execute() {
                final Customer p = hideMenuAndGetSelectedPatient();
                App.get().switchToPresenterByType(PresenterEnum.STORE_PATIENT, p);
            }
        };
        final Command deleteCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                final int idx = getIndexById(clickedId);
                final Customer p = foundCustomers.get(idx);
                final String name = p.getFirstname() + " " + p.getSurname();
                if (Window.confirm(CONSTANTS.deletePatientQuestion()[
                        App.get().getSelectedUnitTextVariant()] + "\n" + name)) {
                    deleteCustomer(clickedId, idx + 1);
                }
                clickedId = null;
            }
        };
        final Command specialSmsCommand = new Command() {
            public void execute() {
                final Customer p = hideMenuAndGetSelectedPatient();
                specialSmsDlg(p);
                clickedId = null;
            }
        };
        final Command historyCommand = new Command() {
            public void execute() {
                final Customer p = hideMenuAndGetSelectedPatient();
                App.get().switchToPresenterByType(PresenterEnum.PATIENT_HISTORY, p.getId());
            }
        };
        final MenuBar popupMenuBar = new MenuBar(true);
        final MenuItem updateItem = new MenuItem(CONSTANTS.update(), true, updateCommand);
        final MenuItem deleteItem = new MenuItem(CONSTANTS.delete(), true, deleteCommand);
        final MenuItem specialSmsItem = new MenuItem(CONSTANTS.specialSms(), true, specialSmsCommand);
        final MenuItem historyItem = new MenuItem(CONSTANTS.visits(), true, historyCommand);
        popupMenuBar.addItem(updateItem);
        popupMenuBar.addItem(deleteItem);
        popupMenuBar.addSeparator();
        popupMenuBar.addItem(specialSmsItem);
        popupMenuBar.addItem(historyItem);
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
        view.getName().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getName().setText("");
        view.getPhoneNumber().setText("");
        view.getBirthNumber().setText("");
        cleanResultTable();
        clickedId = null;
        if (null != foundCustomers) {
            foundCustomers.clear();
            foundCustomers = null;
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Delete given customer.
     * @param id customer ID
     * @param line line in the table to be removed
     */
    private void deleteCustomer(final String id, final int line) {
        final RestHandler rest = new RestHandler("/rest/unit/customer/" + URL.encodePathSegment(id) + "/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final Customer customer = new Customer();
                customer.setId(id);
                eventBus.fireEvent(new CrudEvent(OperationType.DELETE, customer));
                view.getResultTable().removeRow(line);
                for (Customer foundCustomer : foundCustomers) {
                    if (foundCustomer.equals(id)) {
                        foundCustomers.remove(foundCustomer);
                        break;
                    }
                }
            }
        });
        rest.delete();
    }

    /**
     * Finds customers and show the result set.
     */
    private void findCustomers() {
        cleanResultTable();

        final RestHandler rest = new RestHandler("/rest/unit/"
                + URL.encodePathSegment((String) App.get().getSelectedUnit().getId())
                + "/customer/?name=" + URL.encodeQueryString(view.getName().getText().trim())
                + "&phoneNumber=" + URL.encodeQueryString(view.getPhoneNumber().getText().trim())
                + "&birthNumber=" + URL.encodeQueryString(view.getBirthNumber().getText().trim())
        );
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                foundCustomers = App.get().getJsonDeserializer().deserializeList(Customer.class, "customers", jsonText);
                int line = 1;
                for (Customer p : foundCustomers) {
                    addPatient(p, line);
                    line++;
                }
            }
        });
        rest.get();
    }

    /**
     * Adds one patient into result set table.
     * @param p the patient
     * @param line line where the patient will be inserted on
     */
    private void addPatient(final Customer p, final int line) {
        final FlexTable table = view.getResultTable();
        UiUtils.addCell(table, line, 0, new Label("" + line));
        UiUtils.addCell(table, line, 1, new Label(p.fullname()));
        UiUtils.addCell(table, line, 3, new Label(p.getPhoneNumber()));
        UiUtils.addCell(table, line, 4, new Label(p.getEmail()));
        UiUtils.addCell(table, line, 5, new Label(p.getBirthNumber()));
        UiUtils.addCell(table, line, 6, new Label(p.getDegree()));
        UiUtils.addCell(table, line, 7, new Label(p.getStreet()));
        UiUtils.addCell(table, line, 8, new Label(p.getCity()));
        UiUtils.addCell(table, line, 9, new Label(p.getZipCode()));
        final Image menuImg = new Image("images/menu_button.png");
        // patient ID is stored as element ID
        menuImg.getElement().setId(p.getId().toString());
        menuImg.addClickHandler(menuClickHandler);
        UiUtils.addCell(table, line, 2, menuImg);
    }

    /**
     * Hides the popup menu and gets the patient where the action has been selected on.
     * @return selected patient
     */
    public Customer hideMenuAndGetSelectedPatient() {
        menuPopupPanel.hide();
        final int idx = getIndexById(clickedId);
        return foundCustomers.get(idx);
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
     * Gets index of patient with given ID in collection of found patients.
     * @param idAsText textually patient ID
     * @return index in found patients (starting at 0)
     */
    private int getIndexById(final String idAsText) {
        if (null == foundCustomers) { throw new NullPointerException("patients collection is null"); }

        int i = 0;
        for (Customer p : foundCustomers) {
            if (idAsText.equals(p.getId())) { return i; }
            i++;
        }
        throw new IllegalStateException("patient not found, id=" + idAsText
                + ", collection.size=" + foundCustomers.size());
    }

    /**
     * Displays a dialog window to send a special SMS.
     * @param customer the recipient
     */
    private void specialSmsDlg(final Customer customer) {
        final List<Procedure> immediateMsgCategories = App.get().getProcedures(Event.Type.IMMEDIATE_MESSAGE);
        if (immediateMsgCategories.isEmpty()) {
            Window.alert(CONSTANTS.noSpecialSmsInUnit());
        } else {
            final SpecialMsgDlgPresenter p =
                (SpecialMsgDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SPECIAL_SMS_DLG);
            p.init(customer, immediateMsgCategories);
            final SmeventeDialog dlg = new SmeventeDialog("SMS", p);

            dlg.getOkButton().setText(CONSTANTS.send());
            dlg.getOkButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    // validation
                    if (!p.getValidator().validate()) {
                        // One (or more) validations failed. The actions will have been
                        // already invoked by the ...validate() call.
                        return;
                    }
                    final String procedureId = p.getSelectedProcedureId();
                    final String text = p.getView().getMsgText().getText();
                    dlg.hide(); // invokes clean and deletes upper collected data
                    sendSpecialSms(procedureId, customer, text);
                }
            });

            dlg.center();
        }
    }

    /**
     * Sends request to send a special SMS.
     * @param procedureId ID of selected special procedure
     * @param customer the recipient
     * @param text text to be sent
     */
    private void sendSpecialSms(final String procedureId, final Customer customer, final String text) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("procedureId", procedureId);
        params.put("authorId", App.get().getSelectedUnitMember().getId().toString());
        params.put("customerId", customer.getId().toString());
        params.put("text", text);

        final RestHandler rest = new RestHandler("/rest/user/special-msg/");
        rest.setCallback(new RestCallback() {
            @Override
            public void onFailure(final ExceptionJsonWrapper exWrapper) {
                if (exWrapper.getClassName().endsWith("IllegalStateException")
                        && SmsUtils.MSG_LIMIT_EXCEEDE.equals(exWrapper.getMessage())) {
                    Window.alert(CONSTANTS.smsLimitExceeded());
                } else {
                    App.get().getFailureHandler().handleServerError(exWrapper);
                }
            }
            @Override
            public void onSuccess(final String jsonText) {
                Window.alert(CONSTANTS.smsSent());
            }
        });
        rest.post(params);
    }

}
