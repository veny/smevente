package veny.smevente.client.uc;

import java.util.Date;
import java.util.List;

import veny.smevente.client.App;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.UiUtils;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;

import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * Customer History presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.1.2011
 */
public class CustomerHistoryPresenter
    extends AbstractPresenter<CustomerHistoryPresenter.CustomerHistoryView> {

    /**
     * View interface for the Customer History.
     *
     * @author Vaclav Sykora
     * @since 14.1.2011
     */
    public interface CustomerHistoryView extends View {
        /**
         * @return patient's fullname
         */
        Label getFullname();
        /**
         * @return table with result set
         */
        FlexTable getResultTable();
    }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object patientId) {
        clean();

        // set unit specific text
        view.getResultTable().setWidget(0, 3, new Label(CONSTANTS.procedure()));

        getCustomerHistory(patientId.toString());
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getFullname().setText("");
        final int rows = view.getResultTable().getRowCount();
        for (int i = rows - 1; i > 0; i--) {
            view.getResultTable().removeRow(i);
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Loads customer history from server.
     * @param customerId ID of the customer
     */
    private void getCustomerHistory(final String customerId) {
        clean();

        final RestHandler rest = new RestHandler(
                "/rest/unit/customer/" + URL.encodePathSegment(customerId) + "/history/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final Pair<Customer, List<Event>> historyPair =
                    App.get().getJsonDeserializer().customerHistoryFomJson(jsonText);
                processHistory(historyPair);
            }
        });
        rest.get();
    }

    /**
     * Process the history response delivered from server.
     * @param historyPair pair of patient and his SMSs
     */
    private void processHistory(final Pair<Customer, List<Event>> historyPair) {
        view.getFullname().setText(historyPair.getA().fullname()
                + " [" + historyPair.getA().formattedBirthNumber() + "]");

        final String delimiter = "<img src='images/arrow-up.png' /> " + CONSTANTS.future() + " / "
                + CONSTANTS.history() + " <img src='images/arrow-down.png' />";
        final Date now = new Date();
        boolean future = true;
        int line = 1;
        for (Event sms : historyPair.getB()) {
            if (future && sms.getStartTime().compareTo(now) < 0) {
                future = false;
                if (1 == line) { // no SMSs in future
                    UiUtils.addCell(view.getResultTable(), line, 0, new Label("-- " + CONSTANTS.noItems() + " --"));
                    view.getResultTable().getFlexCellFormatter().setColSpan(line, 0, 5);
                    line++;
                }
                view.getResultTable().setWidget(line, 0, new HTML(delimiter));
                view.getResultTable().getFlexCellFormatter().setColSpan(line, 0, 5);
                view.getResultTable().getFlexCellFormatter().addStyleName(line, 0, "resultTable-header-cell");
                line++;
            }
            addHistoryItem(sms, future, line);
            line++;
        }
        // add 'no item' line if no item in history
        if (future) {
            view.getResultTable().setWidget(line, 0, new HTML(delimiter));
            view.getResultTable().getFlexCellFormatter().setColSpan(line, 0, 5);
            view.getResultTable().getFlexCellFormatter().addStyleName(line, 0, "resultTable-header-cell");
            line++;

            UiUtils.addCell(view.getResultTable(), line, 0, new Label("-- " + CONSTANTS.noItems() + " --"));
            view.getResultTable().getFlexCellFormatter().setColSpan(line, 0, 5);
        }
    }

    /**
     * Adds one history item into result set table.
     * @param sms the SMS as history item
     * @param future flag whether the item is in future
     * @param line line where the patient will be inserted on
     */
    private void addHistoryItem(final Event sms, final boolean future, final int line) {
        final Date startTime = sms.getStartTime();
        final Date endTime = new Date(startTime.getTime() + (sms.getLength() * 60 * 1000));
        final FlexTable table = view.getResultTable();

        UiUtils.addCell(table, line, 0, new Label(
                DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG).format(startTime)));
        UiUtils.addCell(table, line, 1, new Label(
                DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT).format(startTime) + " - "
                + DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT).format(endTime)));
        UiUtils.addCell(table, line, 2, new Label(sms.getAuthor().getFullname()));
        UiUtils.addCell(table, line, 3, new Label(sms.getProcedure().getName()));
        UiUtils.addCell(table, line, 4, new Label(sms.getNotice()));
    }

}
