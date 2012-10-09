package veny.smevente.client.uc;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import veny.smevente.client.App;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.SmeventeDialog;
import veny.smevente.client.SmsWidget;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.SingletonEventBus;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestCallback;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.CrudEvent.CrudEventHandler;
import veny.smevente.client.utils.DateUtils;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.client.utils.Pair;
import veny.smevente.client.utils.SmsUtils;
import veny.smevente.client.utils.SmsWidgetEvent;
import veny.smevente.client.utils.SmsWidgetEvent.SmsWidgetHandler;
import veny.smevente.model.Event;
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
import com.google.gwt.user.client.DOM;
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
        implements HeaderHandler, SmsWidgetHandler, CrudEventHandler {

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
    public void showMenu(final SmsWidgetEvent event) {
        final SmsWidget clickedSmsWidget = event.getSmsWidget();
        final PopupPanel popupPanel = createPopupMenu(clickedSmsWidget);
        popupPanel.setPopupPosition(clickedSmsWidget.getAbsoluteLeft(), clickedSmsWidget.getAbsoluteTop());
        popupPanel.setVisible(true);
        popupPanel.show();
    }

    // ------------------------------------------------- CrudEventHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void create(final CrudEvent event) {
        throw new IllegalStateException("has to be finished after refactoring");
//XXX        if (EntityTypeEnum.PATIENT == event.getEntityType() && null != patients) {
//            patients.add((Patient) event.getData());
//        } else if (EntityTypeEnum.MHC == event.getEntityType() && null != medicalHelpCategories) {
//            MedicalHelpCategory newMhc = (MedicalHelpCategory) event.getData();
//            if (MedicalHelpCategory.TYPE_STANDARD == newMhc.getType()) {
//                medicalHelpCategories.add(newMhc);
//            }
//        }
    }
    /** {@inheritDoc} */
    @Override
    public void read(final CrudEvent event) {
    }
    /** {@inheritDoc} */
    @Override
    public void update(final CrudEvent event) {
        throw new IllegalStateException("has to be finished after refactoring");
//XXX        if (EntityTypeEnum.PATIENT == event.getEntityType()) {
//            final int idx = getPatientIndex(((Patient) event.getData()).getId());
//            if (-1 != idx) {
//                patients.set(idx, (Patient) event.getData());
//            }
//        } else if (EntityTypeEnum.MHC == event.getEntityType()) {
//            final int idx = getMedicalHelpCategoryIndex(((MedicalHelpCategory) event.getData()).getId());
//            if (-1 != idx) {
//                medicalHelpCategories.set(idx, (MedicalHelpCategory) event.getData());
//            }
//        }
    }
    /** {@inheritDoc} */
    @Override
    public void delete(final CrudEvent event) {
        throw new IllegalStateException("has to be finished after refactoring");
//XXX        if (EntityTypeEnum.PATIENT == event.getEntityType()) {
//            final int idx = getPatientIndex(((Patient) event.getData()).getId());
//            patients.remove(idx);
//        } else if (EntityTypeEnum.MHC == event.getEntityType()) {
//            final int idx = getMedicalHelpCategoryIndex(((MedicalHelpCategory) event.getData()).getId());
//            medicalHelpCategories.remove(idx);
//        }
    }

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        eventBus.addHandler(HeaderEvent.TYPE, this);
        eventBus.addHandler(SmsWidgetEvent.TYPE, this);
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
//XXX        // unit info is loaded by HeaderEvent.unitChanget
//        // but if the initial presenter is other one according to history token (e.g. FindPatient)
//        // -> calendar presenter not created -> not registered on Bus -> info is not loaded
//        // [if App.get().getUnits() is null <- post login process in progress -> wait for HeaderEvent]
//        if (null == patients && null != App.get().getMemberships()) {
//            loadUnitInfo(App.get().getSelectedUnit().getId());
//        }

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
     * Creates a new SMS and adds a SMS widget into calendar.
     * @param smsId SMS ID for update or <i>null</i> for create
     * @param patientIdAndMhcId ID of the selected patient & ID of the medical help category
     * @param smsTextAndNotice text of SMS & notice
     * @param mhStartTime date and time of the medical help
     * @param mhLen medical help length
     * @param smsWidget SMS widget in calendar
     */
    private void storeSms(
            final String smsId,
            final Pair<Object, Object> patientIdAndMhcId, final Pair<String, String> smsTextAndNotice,
            final Date mhStartTime, final int mhLen, final SmsWidget smsWidget) {

        final boolean update = (null != smsId);
        final Map<String, String> params = new HashMap<String, String>();
        params.put("authorId", App.get().getSelectedUnitMember().getId().toString());
        params.put("patientId", patientIdAndMhcId.getA().toString());
        params.put("medicalHelpCategoryId", patientIdAndMhcId.getB().toString());
        params.put("text", smsTextAndNotice.getA());
        params.put("notice", smsTextAndNotice.getB());
        params.put("medicalHelpStartTime", "" + mhStartTime.getTime());
        params.put("medicalHelpLength", "" + mhLen);

        // send data to server
        final RestHandler rest = createClientRestHandler("/rest/user/sms/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final Event sms = App.get().getJsonDeserializer().deserialize(Event.class, "sms", jsonText);
                if (update) {
                    final DayColumn col = (DayColumn) smsWidget.getParent();
                    col.remove(smsWidget);
                }
                addEventWidget(sms);
            }
        });
        if (update) {
            params.put("id", smsId);
            rest.put(params);
        } else {
            rest.post(params);
        }
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
                + "/event/from/" + from.getTime() + "/to/" + to.getTime() + "/");

        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final List<Event> events = App.get().getJsonDeserializer().deserializeList(
                        Event.class, "events", jsonText);
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
     * Sends request to delete a SMS.
     * @param smsWidget SMS widget wrapping the SMS
     */
    private void deleteSms(final SmsWidget smsWidget) {
        RestHandler rest = new RestHandler("/rest/user/sms/" + smsWidget.getSms().getId() + "/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                final DayColumn col = (DayColumn) smsWidget.getParent();
                col.remove(smsWidget);
            }
        });
        rest.delete();
    }

    /**
     * Sends request to send SMS.
     * @param smsWidget SMS widget
     */
    private void sendSms(final SmsWidget smsWidget) {
        final Event sms2send = smsWidget.getSms();
        final RestHandler rest = new RestHandler("/rest/user/sms/" + sms2send.getId() + "/");
        rest.setCallback(new RestCallback() {
            @Override
            public void onFailure(final ExceptionJsonWrapper exWrapper) {
                if (exWrapper.getClassName().endsWith("IllegalStateException")
                        && SmsUtils.SMS_LIMIT_EXCEEDE.equals(exWrapper.getMessage())) {
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
    private void detailDlg(final SmsWidget smsWidget) {
        final SmsDetailDlgPresenter p =
            (SmsDetailDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SMS_DETAIL_DLG);
        p.init(smsWidget.getSms());
        final SmeventeDialog dlg = new SmeventeDialog("SMS", p, false);
        dlg.center();
    }

    /**
     * Display a SMS update dialog.
     * @param smsWidget SMS widget wrapping the SMS
     */
    private void updateDlg(final SmsWidget smsWidget) {
        final SmsDlgPresenter p =
            (SmsDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SMS_DLG);
        p.init(smsWidget.getSms(), App.get().getPatients(), App.get().getProcedures(Event.Type.IN_CALENDAR));
        final SmeventeDialog dlg = new SmeventeDialog("SMS", p);

        dlg.getOkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                processOkOnSmsDialog(p, dlg, smsWidget);
            }
        });
        dlg.center();
    }

    /**
     * Creates a context menu with operations for a SMS wrapped by given SMS widget.
     * @param smsWidget SMS widget wrapping the SMS
     * @return popup panel with the context menu
     */
    private PopupPanel createPopupMenu(final SmsWidget smsWidget) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        final boolean notSent = (null == smsWidget.getSms().getSent());
        final MenuBar popupMenuBar = new MenuBar(true);
        popupPanel.add(popupMenuBar);

        // update/detail
        if (notSent) {
            final Command updateCommand = new Command() {
                public void execute() {
                    popupPanel.hide();
                    updateDlg(smsWidget);
                }
            };
            final MenuItem updateItem = new MenuItem(CONSTANTS.update(), true, updateCommand);
            popupMenuBar.addItem(updateItem);
        } else {
            final Command showCommand = new Command() {
                public void execute() {
                    popupPanel.hide();
                    detailDlg(smsWidget);
                }
            };
            final MenuItem detailItem = new MenuItem(CONSTANTS.show(), true, showCommand);
            popupMenuBar.addItem(detailItem);
        }
        // delete
        final Command deleteCommand = new Command() {
            public void execute() {
                popupPanel.hide();
                if (Window.confirm(MESSAGES.deleteSmsQuestion(smsWidget.getSms().getPatient().fullname()))) {
                    deleteSms(smsWidget);
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
                    if (Window.confirm(MESSAGES.sendNowQuestion(smsWidget.getSms().getPatient().fullname()))) {
                        sendSms(smsWidget);
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
     * Displays the SMS dialog.
     * @param eventX X coordinate of click event
     * @param eventY Y coordinate of click event
     * @param col corresponding <code>DayColumn</code>
     */
    @SuppressWarnings("deprecation")
    private void displaySmsDialog(final int eventX, final int eventY, final DayColumn col) {
        if (App.get().getPatients().isEmpty()) {
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

            final SmsDlgPresenter smsDlgPresenter =
                (SmsDlgPresenter) App.get().getPresenterCollection().getPresenter(PresenterEnum.SMS_DLG);
            smsDlgPresenter.init(dateTime, App.get().getPatients(), App.get().getProcedures(Event.Type.IN_CALENDAR));
            final SmeventeDialog dlg = new SmeventeDialog("SMS", smsDlgPresenter);
//            dlg.setPopupPosition(eventX, eventY);
            dlg.center();

            dlg.getOkButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    processOkOnSmsDialog(smsDlgPresenter, dlg, null);
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

        final SmsWidget smsWidget = new SmsWidget(event);
        if (height > 25) {
            smsWidget.setHeight("" +  height + "px");
        }

        col.add(smsWidget, 0, y);
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
     * Creates a click handler that is registered on OK button in SMS dialog.
     * @param smsDlgPresenter the presenter
     * @param dlg the dialog window
     * @param smsWidget SMS widget
     */
    private void processOkOnSmsDialog(
            final SmsDlgPresenter smsDlgPresenter, final SmeventeDialog dlg, final SmsWidget smsWidget) {

        // validation
        if (!smsDlgPresenter.getValidator().validate()) { return; }
        final Object patientId = smsDlgPresenter.getSelectedPatient().getId();
        final Object mhcId = smsDlgPresenter.getSelectedMedicalHelpCategory().getId();
        final String smsText = smsDlgPresenter.getView().getSmsText().getText();
        final String notice = smsDlgPresenter.getView().getNotice().getText();
        final Date mhDateTime = smsDlgPresenter.getStartTime();
        final int mhLen = smsDlgPresenter.getMedicalHelpLength();
        final String smsId =
            smsDlgPresenter.isUpdate() ? smsDlgPresenter.getView().getSmsId().getValue() : null;
        // mobile phone number validation
        final String pn = smsDlgPresenter.getView().getPhoneNumber().getText().trim();
        if ((null == smsId) && (0 == pn.length() || !SmsUtils.isValidGsmPhoneNumber(pn, SmsUtils.LOCALE_CS))
            && !Window.confirm(CONSTANTS.badGsmPhoneNumber())) {
            return;
        }
        dlg.hide(); // invokes clean and deletes upper collected data
        storeSms(smsId, new Pair<Object, Object>(patientId, mhcId),
                new Pair<String, String>(smsText, notice), mhDateTime, mhLen, smsWidget);
    }

//    /**
//     * Finds a patient in presenter's patient cache according to given patient ID.
//     * @param patientId ID to search
//     * @return <i>-1</i> if not found, otherwise the patient index
//     */
//    private int getPatientIndex(final Object patientId) {
//        if (null == patientId) { throw new NullPointerException("patient ID cannot be null"); }
//        if (null != App.get().getPatients()) {
//            for (int i = 0; i < App.get().getPatients().size(); i++) {
//                if (App.get().getPatients().get(i).getId().equals(patientId)) { return i; }
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * Finds a procedureId in App model according to given category ID.
//     * @param procedureId ID to search
//     * @return <i>-1</i> if not found, otherwise the category index
//     */
//    private int getProcedureIndex(final Object procedureId) {
//        if (null == procedureId) { throw new NullPointerException("category ID cannot be null"); }
//        Event.Type.IN_CALENDAR
//        if (null != App.get().getProcedures()) {
//            for (int i = 0; i < App.get().getProcedures().size(); i++) {
//                if (App.get().getProcedures().get(i).getId().equals(categoryId)) { return i; }
//            }
//        }
//        return -1;
//    }

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
        DOM.setElementPropertyInt(view.getCalendarBodyScrollContainer().getElement(), "scrollTop",
                DateUtils.CALENDAR_COLUMN_HEIGHT / 24 * hour);
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
            displaySmsDialog(eventX, eventY, dayColumn);
        }

    }

}
