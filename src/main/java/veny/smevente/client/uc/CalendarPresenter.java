package veny.smevente.client.uc;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.client.App;
import veny.smevente.client.EventWidget;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.SmeventeDialog;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.SingletonEventBus;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestCallback;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.CrudEvent.CrudEventHandler;
import veny.smevente.client.utils.DateUtils;
import veny.smevente.client.utils.EventWidgetEvent;
import veny.smevente.client.utils.EventWidgetEvent.EventWidgetHandler;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.User;
import veny.smevente.shared.ExceptionJsonWrapper;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Calendar Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.8.2010
 */
public class CalendarPresenter extends AbstractPresenter<CalendarPresenter.CalendarView>
        implements HeaderHandler, EventWidgetHandler, CrudEventHandler {

    /**
     * Calendar View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 0.1
     */
    public interface CalendarView extends View {
        /**
         * Gets table of calendar header.
         * @return table of calendar header
         */
        FlexTable getCalendarHeader();
        /**
         * Gets term counter on position 0/0 in calendar table header.
         * @return term counter
         */
        Label getTermCount();
        /**
         * Gets scrollable panel with calendar body.
         * @return scrollable panel with calendar body
         */
        Panel getCalendarBodyScrollContainer();
        /**
         * Gets table of calendar body.
         * @return table of calendar body
         */
        FlexTable getCalendarBody();
    }


    /** Min height value, we cannot go under this value during height update. */
    private static final int MIN_CALENDAR_HEIGHT = 200;
    /** Offset from top (app header + calendar table header). */
    private static final int TOP_HEIGHT_OFFSET = 103;

    /** Date formatter used for ISO 8601 without time zone. */
    private final DateTimeFormat dateTimeFormatter = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** Popup panel with context menu. */
    private final PopupPanel menuPopupPanel = new PopupPanel(false, false);


    // ---------------------------------------------------- HeaderHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void unitChanged(final HeaderEvent event) {
        /* I don't care */
    }

    /** {@inheritDoc} */
    @Override
    public void unitMemberChanged(final HeaderEvent event) {
        loadEventsForWeek(event.getUnitMember(), null); // weekDate=null -> take date from App
    }

    /** {@inheritDoc} */
    @Override
    public void dateChanged(final HeaderEvent event) {
        final Date date = event.getDate();
        if (null == date) { throw new NullPointerException("date is null"); }

        setCalendarHeader(date);

        view.getTermCount().setText("-");
        view.getTermCount().setTitle("");

        loadEventsForWeek(App.get().getSelectedUnitMember(), date);
    }

    // ------------------------------------------------- SmsWidgetHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void showMenu(final EventWidgetEvent event) {
        final EventWidget clickedEventWidget = event.getEventWidget();
        final PopupPanel popupPanel = createPopupMenu(clickedEventWidget);
        popupPanel.setPopupPosition(clickedEventWidget.getAbsoluteLeft(), clickedEventWidget.getAbsoluteTop());
        popupPanel.setVisible(true);
        popupPanel.show();
    }

    // ------------------------------------------------- CrudEventHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void create(final CrudEvent event) {
        if (event.getData() instanceof Customer) {
            App.get().getCustomers().add((Customer) event.getData());
        } else if (event.getData() instanceof Procedure) {
            App.get().getAllProcedures().add((Procedure) event.getData());
        }
    }
    /** {@inheritDoc} */
    @Override
    public void read(final CrudEvent event) { /* I don't care */ }
    /** {@inheritDoc} */
    @Override
    public void update(final CrudEvent event) {
        if (event.getData() instanceof Customer) {
            final int idx = getCustomerIndex(((Customer) event.getData()).getId());
            if (-1 != idx) {
                App.get().getCustomers().set(idx, (Customer) event.getData());
            }
        } else if (event.getData() instanceof Procedure) {
            final int idx = getProcedureIndex(((Procedure) event.getData()).getId());
            if (-1 != idx) {
                App.get().getAllProcedures().set(idx, (Procedure) event.getData());
            }
        }
    }
    /** {@inheritDoc} */
    @Override
    public void delete(final CrudEvent event) {
        if (event.getData() instanceof Customer) {
            final int idx = getCustomerIndex(((Customer) event.getData()).getId());
            if (-1 != idx) {
                App.get().getCustomers().remove(idx);
            }
        } else if (event.getData() instanceof Procedure) {
            final int idx = getProcedureIndex(((Procedure) event.getData()).getId());
            if (-1 != idx) {
                App.get().getAllProcedures().remove(idx);
            }
        }
    }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        eventBus.addHandler(HeaderEvent.TYPE, this);
        eventBus.addHandler(EventWidgetEvent.TYPE, this);
        eventBus.addHandler(CrudEvent.TYPE, this);

        // week calendar header
        setCalendarHeader(App.get().getWeekDate());

        // context menu
        final CreateEventCommand createEventCommand = new CreateEventCommand();
        final Command prevWeekCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                switchWeek(-1);
            }
        };
        final Command nextWeekCommand = new Command() {
            public void execute() {
                menuPopupPanel.hide();
                switchWeek(1);
            }
        };
        final MenuBar popupMenuBar = new MenuBar(true);
        final MenuItem createEventItem = new MenuItem(CONSTANTS.createEvent(), true, createEventCommand);
        final MenuItem prevWeekItem = new MenuItem(CONSTANTS.prevWeek(), true, prevWeekCommand);
        final MenuItem nextWeekItem = new MenuItem(CONSTANTS.nextWeek(), true, nextWeekCommand);
        popupMenuBar.addItem(createEventItem);
        popupMenuBar.addSeparator();
        popupMenuBar.addItem(prevWeekItem);
        popupMenuBar.addItem(nextWeekItem);
        menuPopupPanel.add(popupMenuBar);

        // columns with days
        for (int i = 1; i <= 7; i++) {
            final DayColumn col = new DayColumn(i);
            col.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    if (menuPopupPanel.isShowing()) {
                        menuPopupPanel.hide();
                    } else {
                        createEventCommand.setDayColumn(col);
                        createEventCommand.setEventX(event.getNativeEvent().getClientX());
                        createEventCommand.setEventY(event.getNativeEvent().getClientY());
                        menuPopupPanel.setPopupPosition(
                                event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                        menuPopupPanel.setVisible(true);
                        menuPopupPanel.show();
                    }
                }
            });
            view.getCalendarBody().setWidget(0, i, col);
        }

        // register window resize handler
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent arg0) {
                updateHeight();
            }
        });
        updateHeight();
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    protected void onShow(final Object parameter) {
        setScrollByTime();
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        menuPopupPanel.hide();
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // clear the calendar
        for (int i = 1; i <= 7; i++) {
            final DayColumn col = (DayColumn) view.getCalendarBody().getWidget(0, i);
            col.clear();
        }
    }

    // --------------------------------------------------------------- SMS CRUD

    /**
     * Creates a new event and adds a event widget into calendar.
     * @param eventId event ID for update or <i>null</i> for create
     * @param patientIdAndProcedureId ID of the selected patient & ID of the medical help category
     * @param textAndNotice text of event & notice
     * @param startTime date and time of event
     * @param length length
     * @param eventWidget event widget in calendar
     */
    private void storeEvent(
            final String eventId,
            final Pair<Object, Object> patientIdAndProcedureId, final Pair<String, String> textAndNotice,
            final Date startTime, final int length, final EventWidget eventWidget) {

        final Map<String, String> params = new HashMap<String, String>();
        if (null != eventId) {
            params.put("id", eventId);
        }
        params.put("authorId", App.get().getSelectedUnitMember().getId().toString());
        params.put("customerId", patientIdAndProcedureId.getA().toString());
        params.put("procedureId", patientIdAndProcedureId.getB().toString());
        params.put("text", textAndNotice.getA());
        params.put("notice", textAndNotice.getB());
        params.put("startTime", "" + dateTimeFormatter.format(startTime));
        params.put("length", "" + length);

        // send data to server
        final RestHandler rest = createClientRestHandler("/rest/user/event/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final Event event = App.get().getJsonDeserializer().deserialize(Event.class, "event", jsonText);
                if (null != eventId) { // by update remove old widget
                    final DayColumn col = (DayColumn) eventWidget.getParent();
                    col.remove(eventWidget);
                }
                addEventWidget(event);
            }
        });

        rest.post(params);
    }

    /**
     * Loads events for given user and calendar week.
     * @param user author of events
     * @param weekDate a date in currently displayed week in calendar
     */
    private void loadEventsForWeek(final User user, final Date weekDate) {
        if (null == user || null == user.getId()) { throw new NullPointerException("user identification is null"); }
        clean();

        final Date from = DateUtils.getWeekFrom(null == weekDate ? App.get().getWeekDate() : weekDate);
        final Date to = DateUtils.getWeekTo(null == weekDate ? App.get().getWeekDate() : weekDate);
        final RestHandler rest = createExclusiveClientRestHandler(
                "/rest/user/" + URL.encodePathSegment((String) user.getId())
                + "/event/from/" + URL.encodePathSegment(dateTimeFormatter.format(from))
                + "/to/" + URL.encodePathSegment(dateTimeFormatter.format(to))
                + "/");

        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final List<Event> events =
                        App.get().getJsonDeserializer().deserializeList(Event.class, "events", jsonText);
                int sentCnt = 0;
                for (Event e : events) {
                    addEventWidget(e);
                    if (null != e.getSent()) { sentCnt++; }
                }
                // set SMS counter on position 0/0 in calendar table header
                view.getTermCount().setText(sentCnt + "/" + events.size());
                view.getTermCount().setTitle(MESSAGES.termsInWeek(sentCnt, events.size()));
            }
        });
        rest.get();
    }

    /**
     * Sends request to delete event.
     * @param eventWidget event widget wrapping the event
     */
    private void deleteEvent(final EventWidget eventWidget) {
        final RestHandler rest = new RestHandler("/rest/user/event/"
                + URL.encodePathSegment((String) eventWidget.getEvent().getId()) + "/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final DayColumn col = (DayColumn) eventWidget.getParent();
                col.remove(eventWidget);
            }
        });
        rest.delete();
    }

    /**
     * Sends request to send SMS.
     * @param smsWidget SMS widget
     */
    private void sendSms(final EventWidget smsWidget) {
        final Event sms2send = smsWidget.getEvent();
        final RestHandler rest = new RestHandler("/rest/user/sms/"
                + URL.encodePathSegment(sms2send.getId().toString()) + "/");
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
                final Event sms = App.get().getJsonDeserializer().deserialize(Event.class, "sms", jsonText);
                sms2send.setSent(sms.getSent());
                smsWidget.removeStyleName("sms-widget");
                smsWidget.addStyleName("sms-widget-sent");
            }
        });
        rest.post(null);
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Display a SMS detail dialog.
     * @param smsWidget SMS widget wrapping the SMS
     */
    private void detailDlg(final EventWidget smsWidget) {
        final SmsDetailDlgPresenter p =
            (SmsDetailDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SMS_DETAIL_DLG);
        p.init(smsWidget.getEvent());
        final SmeventeDialog dlg = new SmeventeDialog("SMS", p, false);
        dlg.center();
    }

    /**
     * Display a SMS update dialog.
     * @param smsWidget SMS widget wrapping the SMS
     */
    private void updateDlg(final EventWidget smsWidget) {
        final EventDlgPresenter p =
            (EventDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SMS_DLG);
        p.init(smsWidget.getEvent(), App.get().getCustomers(), App.get().getProcedures(Event.Type.IN_CALENDAR));
        final SmeventeDialog dlg = new SmeventeDialog("SMS", p);

        dlg.getOkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                processOkOnEventDialog(p, dlg, smsWidget);
            }
        });
        dlg.center();
    }

    /**
     * Creates a context menu with operations for a SMS wrapped by given SMS widget.
     * @param eventWidget event widget wrapping the event
     * @return popup panel with the context menu
     */
    private PopupPanel createPopupMenu(final EventWidget eventWidget) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        final boolean notSent = (null == eventWidget.getEvent().getSent());
        final MenuBar popupMenuBar = new MenuBar(true);
        popupPanel.add(popupMenuBar);

        // update/detail
        if (notSent) {
            final Command updateCommand = new Command() {
                public void execute() {
                    popupPanel.hide();
                    updateDlg(eventWidget);
                }
            };
            final MenuItem updateItem = new MenuItem(CONSTANTS.update(), true, updateCommand);
            popupMenuBar.addItem(updateItem);
        } else {
            final Command showCommand = new Command() {
                public void execute() {
                    popupPanel.hide();
                    detailDlg(eventWidget);
                }
            };
            final MenuItem detailItem = new MenuItem(CONSTANTS.show(), true, showCommand);
            popupMenuBar.addItem(detailItem);
        }
        // delete
        final Command deleteCommand = new Command() {
            public void execute() {
                popupPanel.hide();
                if (Window.confirm(MESSAGES.deleteSmsQuestion(eventWidget.getEvent().getCustomer().fullname()))) {
                    deleteEvent(eventWidget);
                }
            }
        };
        final MenuItem deleteItem = new MenuItem(CONSTANTS.delete(), true, deleteCommand);
        popupMenuBar.addItem(deleteItem);
        // send now
        if (notSent) {
            final Command sendCommand = new Command() {
                public void execute() {
                    popupPanel.hide();
                    if (Window.confirm(MESSAGES.sendNowQuestion(eventWidget.getEvent().getCustomer().fullname()))) {
                        sendSms(eventWidget);
                    }
                }
            };
            final MenuItem sendNowItem = new MenuItem(CONSTANTS.sendNow(), true, sendCommand);
            popupMenuBar.addItem(sendNowItem);
        }

        return popupPanel;
    }

    /**
     * Sets the week calendar header according to the given date.
     * @param date date in week to be displayed
     */
    private void setCalendarHeader(final Date date) {
        final DateTimeFormat fmt = DateTimeFormat.getFormat("E d/M");
        final int dayIdxToday = DateUtils.getWeekIndex(new Date());
        final Date firstWeekDay = DateUtils.getWeekFrom(date);

        for (int i = 1; i <= 7; i++) {
            final Label dayLabel = (Label) view.getCalendarHeader().getWidget(0, i);
            final Date d = new Date(firstWeekDay.getTime() + ((i - 1) * DateUtils.MILIS_IN_DAY));

            dayLabel.removeStyleName("calender-weektop-today");
            dayLabel.removeStyleName("calender-weektop-day");
            if (i == dayIdxToday) {
                dayLabel.addStyleName("calender-weektop-today");
            } else {
                dayLabel.addStyleName("calender-weektop-day");
            }
            dayLabel.setText(fmt.format(d));
        }
    }

    /**
     * Displays the event dialog.
     * @param eventX X coordinate of click event
     * @param eventY Y coordinate of click event
     * @param col corresponding <code>DayColumn</code>
     */
    @SuppressWarnings("deprecation")
    private void displayEventDialog(final int eventX, final int eventY, final DayColumn col) {
        if (App.get().getCustomers().isEmpty()) {
            Window.alert(CONSTANTS.noPatientInUnit()[App.get().getSelectedUnitTextVariant()]);
        } else if (App.get().getProcedures(Event.Type.IN_CALENDAR).isEmpty()) {
            Window.alert(CONSTANTS.noMhcInUnit()[App.get().getSelectedUnitTextVariant()]);
        } else {
            final int y = eventY - col.getAbsoluteTop() + Document.get().getScrollTop();
            final Date time = DateUtils.calculateDateFromClick(col.getDayIndex(), y);
            final Date dateTime = DateUtils.getWeekFrom(App.get().getWeekDate());
            dateTime.setDate(dateTime.getDate() + (col.getDayIndex() - 1));
            dateTime.setHours(time.getHours());
            dateTime.setMinutes(time.getMinutes());

            final EventDlgPresenter eventDlgPresenter =
                (EventDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SMS_DLG);
            eventDlgPresenter.init(dateTime, App.get().getCustomers(), App.get().getProcedures(Event.Type.IN_CALENDAR));
            final SmeventeDialog dlg = new SmeventeDialog(CONSTANTS.event(), eventDlgPresenter);
//            dlg.setPopupPosition(eventX, eventY);
            dlg.center();

            dlg.getOkButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    processOkOnEventDialog(eventDlgPresenter, dlg, null);
                }
            });

            dlg.show();
        }
    }

    /**
     * Adds event widget to corresponding place in week calendar.
     * @param event event to be represented with the widget
     */
    private void addEventWidget(final Event event) {
        // coordinates
        final int dayIdx = DateUtils.getWeekIndex(event.getStartTime());
        final DayColumn col = (DayColumn) view.getCalendarBody().getWidget(0, dayIdx);
        final int y = DateUtils.calculateYFromDate(event.getStartTime());
        final int height = DateUtils.calculateWidgetHeight(event.getLength());

        final EventWidget eventWidget = new EventWidget(event);
        if (height > 25) {
            eventWidget.setHeight("" +  height + "px");
        }

        col.add(eventWidget, 0, y);
    }

    /**
     * Is called whenever window is resized and sets fixed height.
     */
    private void updateHeight() {
        int newHeight = Window.getClientHeight() - TOP_HEIGHT_OFFSET;
        if (newHeight < MIN_CALENDAR_HEIGHT) { newHeight = MIN_CALENDAR_HEIGHT; }

        view.getCalendarBodyScrollContainer().getElement().getStyle().setHeight(newHeight, Unit.PX);
    }

    /**
     * Creates a click handler that is registered on OK button in Event dialog.
     * @param eventDlgPresenter the presenter
     * @param dlg the dialog window
     * @param eventWidget event widget
     */
    private void processOkOnEventDialog(
            final EventDlgPresenter eventDlgPresenter, final SmeventeDialog dlg, final EventWidget eventWidget) {

        // validation
        if (!eventDlgPresenter.getValidator().validate()) { return; }
        final Object customerId = eventDlgPresenter.getSelectedCustomer().getId();
        final Object procedureId = eventDlgPresenter.getSelectedProcedure().getId();
        final String msgText = eventDlgPresenter.getView().getMessageText().getText();
        final String notice = eventDlgPresenter.getView().getNotice().getText();
        final Date mhDateTime = eventDlgPresenter.getStartTime();
        final int mhLen = eventDlgPresenter.getProcedureLength();
        final String eventId =
                eventDlgPresenter.isUpdate() ? eventDlgPresenter.getView().getEventId().getValue() : null;
        // show warning if customer has no channel to sent the message (neither SMS nor email)
        if (null == eventId) { // only for new event (creation)
            final Customer customer = eventDlgPresenter.getSelectedCustomer();
            if ((null == customer.getPhoneNumber() || 0 == customer.getPhoneNumber().trim().length())
                && (null == customer.getEmail() || 0 == customer.getEmail().trim().length())) { // no phone/email
                Window.alert(CONSTANTS.noChannelToSendMessage());
            }
        }
        dlg.hide(); // invokes clean and deletes upper collected data
        storeEvent(eventId, new Pair<Object, Object>(customerId, procedureId),
                new Pair<String, String>(msgText, notice), mhDateTime, mhLen, eventWidget);
    }

    /**
     * Finds a customer in App's customer cache according to given ID.
     * @param customerId ID to search
     * @return <i>-1</i> if not found, otherwise the customer's index
     */
    private int getCustomerIndex(final Object customerId) {
        if (null == customerId) { throw new NullPointerException("customer ID cannot be null"); }
        if (null != App.get().getCustomers()) {
            for (int i = 0; i < App.get().getCustomers().size(); i++) {
                if (App.get().getCustomers().get(i).getId().equals(customerId)) { return i; }
            }
        }
        return -1;
    }

    /**
     * Finds a procedure in App's procedure cache according to given procedure ID.
     * @param procedureId ID to search
     * @return <i>-1</i> if not found, otherwise the procedure index
     */
    private int getProcedureIndex(final Object procedureId) {
        if (null == procedureId) { throw new NullPointerException("procedure ID cannot be null"); }
        if (null != App.get().getAllProcedures()) {
            for (int i = 0; i < App.get().getAllProcedures().size(); i++) {
                if (App.get().getAllProcedures().get(i).getId().equals(procedureId)) { return i; }
            }
        }
        return -1;
    }

    /**
     * Switches the current week according to given offset in weeks.
     * @param offset weeks to be moved in time
     */
    private void switchWeek(final int offset) {
        final Date newWeekDate = DateUtils.getOtherWeek(App.get().getWeekDate(), offset);
        App.get().setWeekDate(newWeekDate);
        final HeaderEvent e = new HeaderEvent(newWeekDate);
        SingletonEventBus.get().fireEvent(e);
    }

    /**
     * Sets the scroll bar position according to the current time.
     */
    private void setScrollByTime() {
        @SuppressWarnings("deprecation")
        final int hour = new Date().getHours();
        view.getCalendarBodyScrollContainer().getElement().setPropertyInt(
                "scrollTop", DateUtils.CALENDAR_COLUMN_HEIGHT / 24 * hour);
    }

    /**
     * This class represents a day column in week calendar.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 1.7.2010
     */
    public class DayColumn extends AbsolutePanel implements HasClickHandlers {
        /** Day index (Mon=1, Sun=7). */
        private final int dayIdx;

        /**
         * Constructor.
         * @param idx day index
         */
        public DayColumn(final int idx) {
            dayIdx = idx;
            addStyleName("calender-week-day");
        }

        /**
         * Gets day index.
         * @return day index
         */
        public int getDayIndex() {
            return dayIdx;
        }

        /** {@inheritDoc} */
        @Override
        public HandlerRegistration addClickHandler(final ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }

    /**
     * This class represents a command to be invoked on 'Create Item' menu item.
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 10.3.2011
     */
    private class CreateEventCommand implements Command {
        /** Source day column. */
        private DayColumn dayColumn;
        /** X coordinate of the click event. */
        private int eventX;
        /** Y coordinate of the click event. */
        private int eventY;

        /**
         * Constructor.
         * @param dayColumn source day column
         */
        public void setDayColumn(final DayColumn dayColumn) {
            this.dayColumn = dayColumn;
        }
        /**
         * Sets X coordinate of the click event.
         * @param eventX X coordinate of the click event
         */
        public void setEventX(final int eventX) {
            this.eventX = eventX;
        }
        /**
         * Sets Y coordinate of the click event.
         * @param eventY Y coordinate of the click event
         */
        public void setEventY(final int eventY) {
            this.eventY = eventY;
        }

        /** {@inheritDoc} */
        @Override
        public void execute() {
            menuPopupPanel.hide();
            if (null == dayColumn) { throw new NullPointerException("day column cannot be null"); }
            if (eventX <= 0) { throw new NullPointerException("X coordinate lesser than 0"); }
            if (eventY <= 0) { throw new NullPointerException("Y coordinate lesser than 0"); }
            displayEventDialog(eventX, eventY, dayColumn);
        }

    }

}
