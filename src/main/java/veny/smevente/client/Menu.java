package veny.smevente.client;

import java.util.Date;

import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.l10n.SmeventeConstants;
import veny.smevente.client.mvp.SingletonEventBus;
import veny.smevente.client.utils.DateUtils;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.client.utils.HeaderEvent.HeaderHandler;
import veny.smevente.model.Unit.TextVariant;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * The header menu.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 27.7.2010
 */
public class Menu extends MenuBar implements HeaderHandler {

    /** I18n messages. */
    private static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);

    /** Variable text - find patient. */
    private final MenuItem findPatient;
    /** Variable text - add patient. */
    private final MenuItem addPatient;
    /** Variable text - procedures. */
    private final MenuItem procedureList;
    /** Variable text - add procedure. */
    private final MenuItem addProcedure;
    /** Variable text - user list. */
    private final MenuItem userList;
    /** Variable text - add user. */
    private final MenuItem addUser;
    /** Variable text - user management separator. */
    private MenuItemSeparator userManagementSeparator;
    /** The application management menu. */
    private final MenuBar managementBar;

    /**
     * Constructor.
     */
    public Menu() {
        setAutoOpen(true);
        setAnimationEnabled(true);

        // text changing according to selected group
        SingletonEventBus.get().addHandler(HeaderEvent.TYPE, this);

        // Main
        final MenuBar bar = new MenuBar(true);
        MenuItem item = new MenuItem(CONSTANTS.menu(), bar);
        addItem(item);
        // Today separator
        this.addSeparator();
        // Main / Calendar
        bar.addItem(new MenuItem(CONSTANTS.calendar(), new Command() {
            @Override
            public void execute() {
                processNewWeekDate(null); // no date change, only UC switch
            }
        }));

        bar.addSeparator();
        // Main / Find Patient
        findPatient = createSwitchUcMenuItem(CONSTANTS.findPatient()[0], PresenterEnum.FIND_PATIENT);
        bar.addItem(findPatient);
        // Main / Add Patient
        addPatient = createSwitchUcMenuItem(CONSTANTS.addPatient()[0], PresenterEnum.STORE_PATIENT);
        bar.addItem(addPatient);
        // separator
        bar.addSeparator();
        // Main / SMS Statistic
        item = createSwitchUcMenuItem(CONSTANTS.smsStatistics(), PresenterEnum.SMS_STATISTIC);
        bar.addItem(item);
        // Main / Change password
        item = createSwitchUcMenuItem(CONSTANTS.changePassword(), PresenterEnum.CHANGE_PASSWORD);
        bar.addItem(item);
        // separator
        bar.addSeparator();
        // Main / Management
        managementBar = new MenuBar(true);
        bar.addItem(CONSTANTS.management() + " >", managementBar);

        // Main / Management / Procedure List
        procedureList = createSwitchUcMenuItem(CONSTANTS.procedures(), PresenterEnum.MEDICAL_HELP_CATEGORY_TYPES);
        managementBar.addItem(procedureList);
        // Main / Management / Add Procedure
        addProcedure = createSwitchUcMenuItem(CONSTANTS.addProcedure(), PresenterEnum.STORE_PROCEDURE);
        managementBar.addItem(addProcedure);
        // separator
        managementBar.addSeparator();
        // Main / Management / Special SMSs
        item = createSwitchUcMenuItem(CONSTANTS.specialSmss(),
                PresenterEnum.SPECIAL_MESSAGES);
        managementBar.addItem(item);
        // Main / Management / Add Special SMS
        item = createSwitchUcMenuItem(CONSTANTS.addSpecialSms(),
                PresenterEnum.STORE_SPECIAL_MEDICAL_HELP_CATEGORY);
        managementBar.addItem(item);
        // Because the user management items are added/removed in runtime,
        // the related components are here only created to be prepared for
        // adding/removing in runtime.
        // Main / Management / Find user
        userList = createSwitchUcMenuItem(CONSTANTS.users(), PresenterEnum.USER_LIST);
        // Main / Management / Add User
        addUser = createSwitchUcMenuItem(CONSTANTS.addUser(), PresenterEnum.STORE_USER);

        // today
        this.addItem(new MenuItem(CONSTANTS.today(), new Command() {
            @Override
            public void execute() {
                processNewWeekDate(new Date());
            }
        }));

        // previous week (arrow left)
        final String prevImg = "<img src='images/arrow-left.gif' title='" + CONSTANTS.prevWeek() + "'/>";
        this.addItem(new MenuItem(prevImg, true, new Command() {
            @Override
            public void execute() {
                final Date newWeekDate = DateUtils.getOtherWeek(App.get().getWeekDate(), -1);
                processNewWeekDate(newWeekDate);
            }
        }));
        // next week (arrow right)
        final String nextImg = "<img src='images/arrow-right.gif' title='" + CONSTANTS.nextWeek() + "'/>";
        this.addItem(new MenuItem(nextImg, true, new Command() {
            @Override
            public void execute() {
                final Date newWeekDate = DateUtils.getOtherWeek(App.get().getWeekDate(), 1);
                processNewWeekDate(newWeekDate);
            }
        }));

        // calendar
        // http://www.trade-offs.nl/blog/?p=28
        final MenuItem calendarItem = new MenuItem(CONSTANTS.calendar(), (Command) null);
        final DatePicker datePicker = new DatePicker();
        final PopupPanel popupPanel = new PopupPanel(true);
        popupPanel.add(datePicker);
        datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Date> event) {
                final Date date = event.getValue();
                popupPanel.hide();
                processNewWeekDate(date);
            }
        });
        calendarItem.setCommand(new Command() {
            @Override
            public void execute() {
                popupPanel.setPopupPosition(
                        calendarItem.getAbsoluteLeft(),
                        calendarItem.getAbsoluteTop() + calendarItem.getOffsetHeight());
                popupPanel.show();
            }
        });
        this.addItem(calendarItem);
        this.addSeparator();
    }

    /**
     * Adds the menu items specific to the administrator users. This
     * action is called from outside because in constructor of <code>
     * Menu</code> class are not set all required variables to decide
     * if current user is/is not an administrator.
     */
    public void setupAdminItems() {
        // Add/remove user management items if the current
        // member is/is not an administrator of current unit.
        if (App.get().isSelectedUnitMemberAdmin()) {
            if (managementBar.getItemIndex(userList) == -1) {
                if (userManagementSeparator == null) {
                    userManagementSeparator = managementBar.addSeparator();
                } else {
                    managementBar.addSeparator(userManagementSeparator);
                }
                managementBar.addItem(userList);
                managementBar.addItem(addUser);
            }
        } else {
            managementBar.removeSeparator(userManagementSeparator);
            managementBar.removeItem(userList);
            managementBar.removeItem(addUser);
        }
    }

    // -------------------------------------------------- HeaderHandler Methods

    /** {@inheritDoc} */
    @Override
    public void unitChanged(final HeaderEvent event) {
        final TextVariant variant = event.getUnit().enumTextVariant();
        findPatient.setText(CONSTANTS.findPatient()[variant.ordinal()]);
        addPatient.setText(CONSTANTS.addPatient()[variant.ordinal()]);
    }
    /** {@inheritDoc} */
    @Override
    public void unitMemberChanged(final HeaderEvent event) { /* I don't care */ }
    /** {@inheritDoc} */
    @Override
    public void dateChanged(final HeaderEvent event) { /* I don't care */ }

    // ------------------------------------------------------ Assistant Methods

    /**
     * Creates a <code>MenuItem</code> to switch an UC.
     * @param text the menu item label
     * @param presenter UC to switch into
     * @return the menu item instance
     */
    private MenuItem createSwitchUcMenuItem(final String text, final PresenterEnum presenter) {
        return new MenuItem(text, new Command() {
            @Override
            public void execute() {
                App.get().switchToPresenterByType(presenter, "");
            }
        });
    }

    /**
     * Store new selected date in <code>App</code> and fires event
     * or only fires event if the new time is <i>null</i>.
     * @param weekDate new selected date
     */
    private void processNewWeekDate(final Date weekDate) {
        if (null != weekDate)  { App.get().setWeekDate(weekDate); }
        // just to be sure
        if (null == App.get().getWeekDate()) { throw new NullPointerException("week date cannot be null"); }

        // switch to calendar if not displayed
        if (PresenterEnum.CALENDER !=  App.get().getActivePresenterId()) {
            App.get().switchToPresenterByType(PresenterEnum.CALENDER, null);
        }

        // fire event
        final HeaderEvent e = new HeaderEvent(App.get().getWeekDate());
        SingletonEventBus.get().fireEvent(e);
    }

}
