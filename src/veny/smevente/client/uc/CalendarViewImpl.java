package veny.smevente.client.uc;

import veny.smevente.client.uc.CalendarPresenter.CalendarView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Calendar View implementation.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class CalendarViewImpl extends Composite implements CalendarView {


    /** UIBinder template. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField FlexTable calendarHeader;
    private final Label termCount;
    @UiField SimplePanel calendarBodyScrollContainer;
    @UiField FlexTable calendarBody;
    // CHECKSTYLE:ON

    /** UIBinder template. */
    @UiTemplate("calendar.ui.xml")
    interface Binder extends UiBinder<HTMLPanel, CalendarViewImpl> { }

    /**
     * Constructor.
     */
    public CalendarViewImpl() {
        initWidget(BINDER.createAndBindUi(this));

        // CALENDAR HEADER

        termCount = new Label("");

        FlexCellFormatter cellFormatter = calendarHeader.getFlexCellFormatter();
        cellFormatter.addStyleName(0, 0, "calender-1row1col");

//        calendarHeader.setText(0, 0, "");
        calendarHeader.setWidget(0, 0, termCount);
        for (int i = 1; i <= 7; i++) {
            Label dayLabel = new Label("");
            calendarHeader.setWidget(0, i, dayLabel);
        }

        // CALENDAR BODY

        // first column with hours
        cellFormatter = calendarBody.getFlexCellFormatter();
        cellFormatter.addStyleName(0, 0, "calender-1col");
        final VerticalPanel hoursCol = new VerticalPanel();
        for (int i = 0; i < 24; i++) {
            Label interval = new Label((i < 10 ? "0" : "") + i + ":00");
            interval.addStyleName("calender-hour-interval");
            hoursCol.add(interval);
        }
        calendarBody.setWidget(0, 0, hoursCol);
    }

    /** {@inheritDoc} */
    @Override
    public FlexTable getCalendarHeader() {
        return calendarHeader;
    }
    /** {@inheritDoc} */
    @Override
    public Label getTermCount() {
        return termCount;
    }
    /** {@inheritDoc} */
    @Override
    public Panel getCalendarBodyScrollContainer() {
        return calendarBodyScrollContainer;
    }
    /** {@inheritDoc} */
    @Override
    public FlexTable getCalendarBody() {
        return calendarBody;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

}
