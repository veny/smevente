package veny.smevente.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.Presenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.uc.CalendarPresenter;
import veny.smevente.client.uc.CalendarViewImpl;
import veny.smevente.client.uc.ChangePasswordPresenter;
import veny.smevente.client.uc.ChangePasswordViewImpl;
import veny.smevente.client.uc.FindCustomerPresenter;
import veny.smevente.client.uc.FindCustomerViewImpl;
import veny.smevente.client.uc.UserListPresenter;
import veny.smevente.client.uc.UserListViewImpl;
import veny.smevente.client.uc.HeaderPresenter;
import veny.smevente.client.uc.HeaderViewImpl;
import veny.smevente.client.uc.ProcedureListPresenter;
import veny.smevente.client.uc.ProcedureListViewImpl;
import veny.smevente.client.uc.CustomerHistoryPresenter;
import veny.smevente.client.uc.CustomerHistoryViewImpl;
import veny.smevente.client.uc.SmsDetailDlgPresenter;
import veny.smevente.client.uc.SmsDetailDlgViewImpl;
import veny.smevente.client.uc.EventDlgPresenter;
import veny.smevente.client.uc.EventDlgViewImpl;
import veny.smevente.client.uc.SmsStatisticPresenter;
import veny.smevente.client.uc.SmsStatisticViewImpl;
import veny.smevente.client.uc.SpecialMsgDlgPresenter;
import veny.smevente.client.uc.SpecialMsgDlgViewImpl;
import veny.smevente.client.uc.StoreProcedurePresenter;
import veny.smevente.client.uc.StoreProcedureViewImpl;
import veny.smevente.client.uc.StoreCustomerPresenter;
import veny.smevente.client.uc.StoreCustomerViewImpl;
import veny.smevente.client.uc.StoreUserPresenter;
import veny.smevente.client.uc.StoreUserViewImpl;
import veny.smevente.model.Event;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;

/**
 * GWT Presenters collection implementation with lazy binding.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public class PresenterCollection {

    /**
     * Enumeration of all presenters to simplify the web flow.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     */
    public enum PresenterEnum {

        /** Header. */
        HEADER,
        /** Week calendar. */
        CALENDER,
        /** SMS dialog. */
        SMS_DLG,
        /** SMS detail dialog. */
        SMS_DETAIL_DLG,
        /** Special SMS dialog. */
        SPECIAL_SMS_DLG,
        /** User list. */
        USER_LIST,
        /** Add user. */
        STORE_USER,
        /** Find patient. */
        FIND_PATIENT,
        /** Add patient. */
        STORE_PATIENT,
        /** Add medical help category. */
        STORE_PROCEDURE,
        /** Medical help category types. */
        MEDICAL_HELP_CATEGORY_TYPES,
        /** Add special medical help category. */
        STORE_SPECIAL_MEDICAL_HELP_CATEGORY,
        /** Special medical help category types. */
        SPECIAL_MESSAGES,
        /** Patient History. */
        PATIENT_HISTORY,
        /** SMS statistic. */
        SMS_STATISTIC,
        /** Change password. */
        CHANGE_PASSWORD;

        /**
         * Gets the presenter ID.
         *
         * @return the presenter ID
         */
        public String getId() {
            return this.name().replaceAll("_", "-").toLowerCase();
        }

        /**
         * Gets an enumeration item according to the given presenter ID.
         *
         * @param value the presenter ID
         * @return the enumeration item
         */
        public static PresenterEnum getValue(final String value) {
            return valueOf(value.replaceAll("-", "_").toUpperCase());
        }

    }


    /**
     * The cache of the presenters.
     */
    private final Map<PresenterEnum, Presenter< ? extends View> > presenterCache;

    /**
     * Constructor creates the presenter cache map
     * and handles the window close event to presenter.unbind().
     */
    public PresenterCollection() {
        presenterCache = new HashMap<PresenterEnum, Presenter< ? extends View>>();

        // call 'unbind' on all presenters on window closing
        Window.addCloseHandler(new CloseHandler<Window>() {
            @Override
            public void onClose(final CloseEvent<Window> arg0) {
                for (PresenterEnum key : presenterCache.keySet()) {
                    Presenter< ? extends View> presenter = presenterCache.get(key);
                    try {
                        presenter.unbind();
                    } catch (Exception ex) {
                        App.get().getFailureHandler().handleClientErrorWithAction(
                            ex, "Unbind presenter by closing window", "id=" + key.getId());
                    }
                }
            }
        });
    }

    /**
     * Gets all presenters.
     * @return all presenters
     */
    public Collection<Presenter< ? extends View> > getAll() {
        return presenterCache.values();
    }

    /**
     * Gets the presenter specified by an enumeration item.
     *
     * @param presenterId the corresponding enumeration item
     * @return the presenter specified by enumeration item
     */
    public Presenter< ? extends View> getPresenter(final PresenterEnum presenterId) {
        if (null == presenterId) { throw new NullPointerException("presenter ID cannot be null"); }

        if (!presenterCache.containsKey(presenterId)) {
            createPresenterByNameInCache(presenterId);
        }
        return presenterCache.get(presenterId);
    }


    /**
     * Creating the presenters for lazy binding in the presenter cache.
     * @param presenterId the name of the presenter to be added to the cache
     */
    private void createPresenterByNameInCache(final PresenterEnum presenterId) {
        Presenter< ? > presenter = null;

        switch (presenterId) {
            case HEADER:
                HeaderPresenter headerPresenter = new HeaderPresenter();
                headerPresenter.bind(new HeaderViewImpl());
                presenter = headerPresenter;
                break;
            case CALENDER:
                CalendarPresenter calendarPresenter = new CalendarPresenter();
                calendarPresenter.bind(new CalendarViewImpl());
                presenter = calendarPresenter;
                break;
            case SMS_DLG:
                EventDlgPresenter smsDlgPresenter = new EventDlgPresenter();
                smsDlgPresenter.bind(new EventDlgViewImpl());
                presenter = smsDlgPresenter;
                break;
            case SMS_DETAIL_DLG:
                SmsDetailDlgPresenter smsDetailDlgPresenter = new SmsDetailDlgPresenter();
                smsDetailDlgPresenter.bind(new SmsDetailDlgViewImpl());
                presenter = smsDetailDlgPresenter;
                break;
            case SPECIAL_SMS_DLG:
                SpecialMsgDlgPresenter specialSmsDlgPresenter = new SpecialMsgDlgPresenter();
                specialSmsDlgPresenter.bind(new SpecialMsgDlgViewImpl());
                presenter = specialSmsDlgPresenter;
                break;
            case USER_LIST:
                UserListPresenter findUserPresenter = new UserListPresenter();
                findUserPresenter.bind(new UserListViewImpl());
                presenter = findUserPresenter;
                break;
            case STORE_USER:
                StoreUserPresenter storeUserPresenter = new StoreUserPresenter();
                storeUserPresenter.bind(new StoreUserViewImpl());
                presenter = storeUserPresenter;
                break;
            case FIND_PATIENT:
                FindCustomerPresenter findPatientPresenter = new FindCustomerPresenter();
                findPatientPresenter.bind(new FindCustomerViewImpl());
                presenter = findPatientPresenter;
                break;
            case STORE_PATIENT:
                StoreCustomerPresenter addPatientPresenter = new StoreCustomerPresenter();
                addPatientPresenter.bind(new StoreCustomerViewImpl());
                presenter = addPatientPresenter;
                break;
            case PATIENT_HISTORY:
                CustomerHistoryPresenter patientHistoryPresenter = new CustomerHistoryPresenter();
                patientHistoryPresenter.bind(new CustomerHistoryViewImpl());
                presenter = patientHistoryPresenter;
                break;
            case STORE_PROCEDURE:
                StoreProcedurePresenter createMedicalHelpCategoryPresenter =
                    new StoreProcedurePresenter(Event.Type.IN_CALENDAR);
                createMedicalHelpCategoryPresenter.bind(new StoreProcedureViewImpl());
                presenter = createMedicalHelpCategoryPresenter;
                break;
            case MEDICAL_HELP_CATEGORY_TYPES:
                ProcedureListPresenter medicalHelpCategoryTypesPresenter =
                    new ProcedureListPresenter(Event.Type.IN_CALENDAR);
                medicalHelpCategoryTypesPresenter.bind(
                        new ProcedureListViewImpl(Event.Type.IN_CALENDAR));
                presenter = medicalHelpCategoryTypesPresenter;
                break;
            case STORE_SPECIAL_MEDICAL_HELP_CATEGORY:
                StoreProcedurePresenter createSpecialMedicalHelpCategoryPresenter =
                    new StoreProcedurePresenter(Event.Type.IMMEDIATE_MESSAGE);
                createSpecialMedicalHelpCategoryPresenter.bind(new StoreProcedureViewImpl());
                presenter = createSpecialMedicalHelpCategoryPresenter;
                break;
            case SPECIAL_MESSAGES:
                ProcedureListPresenter specialMedicalHelpCategoryTypesPresenter =
                    new ProcedureListPresenter(Event.Type.IMMEDIATE_MESSAGE);
                specialMedicalHelpCategoryTypesPresenter.bind(
                        new ProcedureListViewImpl(Event.Type.IMMEDIATE_MESSAGE));
                presenter = specialMedicalHelpCategoryTypesPresenter;
                break;
            case SMS_STATISTIC:
                SmsStatisticPresenter smsStatisticPresenter = new SmsStatisticPresenter();
                smsStatisticPresenter.bind(new SmsStatisticViewImpl());
                presenter = smsStatisticPresenter;
                break;
            case CHANGE_PASSWORD:
                ChangePasswordPresenter changePasswordPresenter = new ChangePasswordPresenter();
                changePasswordPresenter.bind(new ChangePasswordViewImpl());
                presenter = changePasswordPresenter;
                break;
            default:
                throw new IllegalArgumentException("presenter not available: " + presenter);
        }
        ((AbstractPresenter< ? >) presenter).setId(presenterId);
        presenterCache.put(presenterId, presenter);
    }

}
