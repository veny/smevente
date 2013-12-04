package veny.smevente.client.uc;

import java.util.List;
import java.util.Map;

import veny.smevente.client.App;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.DateUtils;
import veny.smevente.client.utils.EmptyValidator;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.client.utils.Pair;
import veny.smevente.model.Event;
import veny.smevente.model.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.actions.DisclosureTextAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * SMS Statistic presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 31.7.2010
 */
public class SmsStatisticPresenter
    extends AbstractPresenter<SmsStatisticPresenter.SmsStatisticView>
    implements HeaderHandler {

    /**
     * View interface for the SMS Statistic.
     *
     * @author Vaclav Sykora
     * @since 0.1
     */
    public interface SmsStatisticView extends View {
        /**
         * Getter for the 'date from' text field.
         * @return the input field for the 'date from'
         */
        DateBox getFrom();
        /**
         * Getter for the 'date to' text field.
         * @return the input field for the 'date to'
         */
        DateBox getTo();
        /**
         * Getter for the button to submit.
         * @return the submit element
         */
        HasClickHandlers getSubmit();
        /**
         * @return the errors panel
         */
        DisclosurePanel getValidationErrors();
        /**
         * @return chart image element
         */
        Image getChart();
        /**
         * Table with result set.
         * @return table with result set
         */
        FlexTable getResultTable();
    }

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

        final DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);
        view.getFrom().setFormat(new DateBox.DefaultFormat(dateFormat));
        view.getTo().setFormat(new DateBox.DefaultFormat(dateFormat));

        view.getSubmit().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                // validation
                if (!validator.validate()) {
                    // One (or more) validations failed. The actions will have been
                    // already invoked by the ...validate() call.
                    return;
                }
                getStatistics();
            }
        });

        // VALIDATION
        setupValidation();
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        ebusUnitSelection.removeHandler();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow(final Object parameter) {
        view.getFrom().setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getFrom().getTextBox().setText("");
        view.getTo().getTextBox().setText("");
        view.getChart().setUrl((String) null);
        cleanResultTable();

        // validation
        validator.reset((String[]) null);
        view.getFrom().removeStyleName("validationFailedBorder");
        view.getTo().removeStyleName("validationFailedBorder");
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Loads SMS statistics from server.
     */
    private void getStatistics() {
        cleanResultTable();

        final StringBuilder uri = new StringBuilder("/rest/user/")
            .append(App.get().getSelectedUnitMember().getId())
            .append("/unit/")
            .append(App.get().getSelectedUnit().getId())
            .append("/from/")
            .append(DateUtils.getStartOfDay(view.getFrom().getValue()).getTime())
            .append("/to/")
            .append(DateUtils.getEndOfDay(view.getTo().getValue()).getTime())
            .append("/");
        final RestHandler rest = new RestHandler(uri.toString());
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final List<Pair<User, Map<String, Long>>> statistics =
                    App.get().getJsonDeserializer().smsStatisticsFromJson(jsonText);
                int line = 1;
                for (Pair<User, Map<String, Long>> entry : statistics) {
                    addCell(line, 0, new InlineLabel(entry.getA().getFullname()));
                    addCell(line, 1, new Label(entry.getB().get(Event.SUM).toString()));
                    addCell(line, 2, new Label(entry.getB().get(Event.SENT).toString()));
                    addCell(line, 3, new Label(entry.getB().get(Event.FAILED).toString()));
                    addCell(line, 4, new Label(entry.getB().get(Event.DELETED).toString()));
                    line++;
                }
                view.getChart().setUrl(constructChartUrl(statistics));
            }
        });
        rest.get();
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
     * Cleans the result set table.
     */
    private void cleanResultTable() {
        final int rows = view.getResultTable().getRowCount();
        for (int i = rows - 1; i > 0; i--) {
            view.getResultTable().removeRow(i);
        }
    }

    /**
     * Constructs URL for Google Chart Tools.
     * @param statistics list with statistics data
     * @return URL for Google Chart Tools
     */
    private String constructChartUrl(final List<Pair<User, Map<String, Long>>> statistics) {
        final StringBuilder rslt = new StringBuilder(
                "http://chart.apis.google.com/chart?cht=p3&chs=400x100&");
        final StringBuilder data = new StringBuilder("chd=t:");
        final StringBuilder labels = new StringBuilder("chl=");
        for (Pair<User, Map<String, Long>> entry : statistics) {
            data.append(entry.getB().get(Event.SUM).toString());
            labels.append(entry.getA().getFullname());
            data.append(',');
            labels.append('|');
        }
        // remove last delimiters
        data.deleteCharAt(data.length() - 1);
        labels.deleteCharAt(labels.length() - 1);

        rslt.append(data.toString())
            .append('&')
            .append(labels.toString());
        return URL.encode(rslt.toString());
    }

    /**
     * Sets up validation for the presenter.
     */
    private void setupValidation() {
        // validation messages
        ValidationMessages vmess = new ValidationMessages() {
            @Override
            public String getCustomMessage(final String key, final Object... parameters) {
                return getValidationMessage(key, parameters);
            }
            @Override
            public String getPropertyName(final String propertyName) {
                String result;
                if ("from".equals(propertyName)) {
                    result = CONSTANTS.from();
                } else if ("to".equals(propertyName)) {
                    result = CONSTANTS.to();
                } else if ("fromBiggerTo".equals(propertyName)) {
                    result = CONSTANTS.from();
                } else { result = super.getPropertyName(propertyName); }
                return result;
            }
        };
        validator = new DefaultValidationProcessor(vmess);
        // add DisclosurePanel for validation messages
        validator.addGlobalAction(new DisclosureTextAction(view.getValidationErrors(), "redText"));

        validator.addValidators("from",
                new EmptyValidator(view.getFrom().getTextBox())
                    .addActionForFailure(new StyleAction("validationFailedBorder")));
        validator.addValidators("to",
                new EmptyValidator(view.getTo().getTextBox())
                    .addActionForFailure(new StyleAction("validationFailedBorder")));

        // from < to
        validator.addValidators("fromBiggerTo", new Validator<Object>() {
            @Override
            public void invokeActions(final ValidationResult result) {
                getView().getFrom().getTextBox().addStyleName("validationFailedBorder");
            }
            @Override
            public ValidationResult validate(final ValidationMessages messages) {
                // remove result of previous failed validation if any
                getView().getFrom().getTextBox().removeStyleName("validationFailedBorder");

                if (null != getView().getFrom().getValue()
                    && null != getView().getTo().getValue()
                    && getView().getFrom().getValue().after(getView().getTo().getValue())) {
                    return new ValidationResult(CONSTANTS.validationFromBiggerTo());
                }
                return null;
            }
        });
    }

}
