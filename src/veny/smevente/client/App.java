package veny.smevente.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.mvp.Presenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.RestCallback;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.uc.HeaderPresenter;
import veny.smevente.client.uc.LoginPresenter;
import veny.smevente.client.uc.LoginViewImpl;
import veny.smevente.model.AbstractEntity;
import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.Patient;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
import veny.smevente.shared.ExceptionJsonWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This class represents the application.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public final class App implements ValueChangeHandler<String> {

    /** Logger. */
    public static final Logger LOG = Logger.getLogger("App");

    /** Minimal page width in pixel - if browser window is smaller, scrollbars will appear. */
    public static final int MIN_PAGE_WIDTH = 1000;

    /** Base URL used to creating absolute URL. Should be more advanced in next versions (dynamically resolved). */
    private static final String BASE_URL = "/smevente";

    /** Singleton instance. */
    private static App singleton;

    /** Login presenter. */
    private final LoginPresenter loginPresenter;
    /** Presenter of the portal footer. */
//    private final FooterPresenter footerPresenter;


    /** Presenter collection. */
    private final PresenterCollection presenterCollection;
    /** JSON deserializer. */
    private final JsonDeserializer jsonDeserializer;
    /** Client side error handler. */
    private FailureHandler failureHandler;

    /** The presenter that is currently active and present in the main panel. */
    private Presenter< ? > activeMainPresenter = null;

    // Models

    /** List of memberships->units sorted by significance for the current logged in user. */
    private List<Membership> memberships;
    /** List of other members in currently selected unit. */
    private List<User> unitMembers;
    /** List of available patients in currently selected unit. */
    private List<Patient> patients;
    /** List of available procedures in currently selected unit. */
    private List<Procedure> procedures;

    // Current Set Data

    /** Index of currently selected unit. */
    private int selectedUnitIndex;
    /** Index of currently selected unit member. */
    private int selectedUnitMemberIndex;
    /** A date in currently displayed week. */
    private Date weekDate;

    /**
     * Private constructor to avoid custom instances.
     */
    private App() {
        weekDate = new Date();

        loginPresenter = new LoginPresenter();
        loginPresenter.bind(new LoginViewImpl());

//aaa        footerPresenter = new FooterPresenter();
//        footerPresenter.bind(new FooterViewImpl());

        presenterCollection = new PresenterCollection();
        jsonDeserializer = new JsonDeserializer();

        // register window resize handler
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent arg0) {
                updateWidth();
            }
        });

        // attach this singleton with history
        History.addValueChangeHandler(this);

        LOG.log(Level.INFO, "initialize OK, baseUrl=" + this.getBaseUrl());
    }

    /**
     * Update width of dynamically whole page width.
     */
    private void updateWidth() {
        Element body = DOM.getElementById("body");
        if (body != null) {
            if (Window.getClientWidth() > MIN_PAGE_WIDTH) {
                body.getStyle().setWidth(100, com.google.gwt.dom.client.Style.Unit.PCT);
            } else {
                body.getStyle().setWidth(MIN_PAGE_WIDTH, com.google.gwt.dom.client.Style.Unit.PX);
            }
        }
    }

    /**
     * Gets singleton instance.
     *
     * @return the singleton instance
     */
    public static synchronized App get() {
        if (null == singleton) {
            singleton = new App();
        }
        return singleton;
    }

    // --------------------------------------------------- Authentication Stuff

    /**
     * Logs out the current logged in user. Sends request to an URL which
     * invalidates the current session.
     */
    public void logout() {
        // send request to invalidate the session
        RestHandler rest = new RestHandler("/rest/logout/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                // browser will follow redirect to /rest/ping
                // -> 403 -> RestHandler -> EventBus -> login form
            }
        });
        rest.get();

        // business
        selectedUnitIndex = 0;
        selectedUnitMemberIndex = 0;
        memberships = null;

        // inform all presenters
        for (Presenter< ? extends View> p : presenterCollection.getAll()) {
            p.hide();
        }
    }

    /**
     * Checks validity of a presented session with the <i>ping</i> request.
     * Invoked from application bootstrap in 'onModuleLoad' method.
     */
    public void checkWithServerIfSessionIdIsStillLegal() {
        final String sessionID = Cookies.getCookie("JSESSIONID");
        if (null == sessionID || 0 == sessionID.trim().length()) {
            // no session
            LOG.info("no cookie found");
            showLoginScreen();
            return;
        }

        LOG.info("cookie found: " + sessionID);

        // check validity of the found session
        final RestHandler rest = new RestHandler("/rest/ping/");
        rest.setCallback(new RestCallback() {
            @Override
            public void onSuccess(final String jsonText) {
                LOG.info("cookie in valid: " + sessionID);
                switchToPresenterByHistory();
            }
            @Override
            public void onFailure(final ExceptionJsonWrapper wrapper) {
                // this variant is covered by UnauthorizedEvent precessed in LoginPresenter,
                // so next section never should be invoked.
                // (is here just likek a historical relict)
                LOG.info("cookie is invalid: " + sessionID);
                showLoginScreen();
            }
        });
        rest.get();
    }

    /**
     * Shows login screen.
     */
    public void showLoginScreen() {

        RootPanel.get("header").clear();
        RootPanel.get("footer").clear();
        RootPanel.get("main").clear();
        if (null != activeMainPresenter) {
            activeMainPresenter.hide();
        }

        LOG.info("Login dialog activated");
        RootPanel.get("main").add(loginPresenter.getView().asWidget());
        loginPresenter.onShow(null);

        updateWidth();
    }

    /**
     * Switch to another presenter defined by its name and pass it additional
     * parameters.
     *
     * @param pe the corresponding enumeration item
     * @param parameter parameter to pass to the presenter
     */
    public void switchToPresenterByType(final PresenterEnum pe, final Object parameter) {
        LOG.info("switching to presenter, type=" + pe);

        // find the new presenter
        final Presenter< ? extends View > presenter = getPresenterCollection().getPresenter(pe);

        // set header if not set yet
        final RootPanel headerSlot = RootPanel.get("header");
        if (0 == headerSlot.getWidgetCount()) {
            final HeaderPresenter hp = (HeaderPresenter) presenterCollection.getPresenter(PresenterEnum.HEADER);
            hp.show(null);
            headerSlot.add(hp.getView().asWidget());
        }

        if (PresenterEnum.CALENDER != pe) {
            final HeaderPresenter hp = (HeaderPresenter) presenterCollection.getPresenter(PresenterEnum.HEADER);
            int selectedIndex = hp.getView().getUnitMembers().getSelectedIndex();
            // Can be also "-1" when called immediately after login,
            // therefore following condition "> 0".
            if (selectedIndex > 0) {
                hp.getView().getUnitMembers().setSelectedIndex(0);
                DomEvent.fireNativeEvent(Document.get().createChangeEvent(), hp.getView().getUnitMembers());
            }
        }
//aaa        // set footer if not set yet
//        final RootPanel footer = RootPanel.get("footer");
//        if (0 == footer.getWidgetCount()) {
//            footer.add(footerPresenter.getView().asWidget());
//        }

        // - - - switch presenters in the main panel...

        // first hide the old presenter
        if (activeMainPresenter != null) {
            activeMainPresenter.hide();
        }

        // clear the main panel
        RootPanel.get("main").clear();

        // then show the new presenter
        RootPanel.get("main").add(presenter.getView().asWidget());
        activeMainPresenter = presenter;
        activeMainPresenter.show(parameter);

        // add tokens to the history stack in the hidden history frame
        String historyToken = pe.getId();
        if (null != parameter) {
            final String param = (parameter instanceof AbstractEntity
                    ? ((AbstractEntity) parameter).getId().toString() : parameter.toString());
            if (!param.trim().isEmpty()) {
                historyToken += ("_" + param);
            }
        }
        History.newItem(historyToken, false);

        updateWidth();
    }

    /**
     * Getter for the presenterCollection.
     *
     * @return the presenterCollection
     */
    public PresenterCollection getPresenterCollection() {
        return presenterCollection;
    }

    /**
     * Gets identification of the active presenter (the currently visible in main slot).
     * @return identification of the active presenter
     */
    public PresenterEnum getActivePresenterId() {
        if (null == activeMainPresenter) {
            throw new NullPointerException("active presenter is null");
        }
        return activeMainPresenter.getId();
    }

    /**
     * Getter for the JSON deserializer.
     *
     * @return the JSON deserializer
     */
    public JsonDeserializer getJsonDeserializer() {
        return jsonDeserializer;
    }

    /**
     * Gets the client side error handler.
     *
     * @return the error handler
     */
    public FailureHandler getFailureHandler() {
        return failureHandler;
    }

    /**
     * Sets the client side failure handler.
     *
     * @param failureHandler the failure handler
     */
    public void setFailureHandler(final FailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    // ----------------------------------------------- ValueChangeHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void onValueChange(final ValueChangeEvent<String> event) {
        // This method is called whenever the application's history changes.
        // Set the label to reflect the current history token.
        onHistoryChange(event.getValue());
    }

    // ---------------------------------------------------- History Stack Stuff

    /**
     * Changes a presenter if some history change event occurs.
     *
     * @param historyToken the history token
     */
    public void onHistoryChange(final String historyToken) {
        String presenterId = historyToken;
        String parameters = null;
        // check if the parameters are stored in the history token
        int idx = historyToken.indexOf("_");
        if (idx > 0) {
            presenterId = historyToken.substring(0, idx);
            parameters = historyToken.substring(idx + 1);
        }
        switchToPresenterByType(PresenterEnum.getValue(presenterId), parameters);
    }

    /**
     * Switches to the last presenter according to the history stack or to the
     * default if no entry in the history stack.
     */
    public void switchToPresenterByHistory() {
        final String historyToken = History.getToken();
        if (null == historyToken || 0 == historyToken.trim().length()) {
            // the default presenter
            App.get().onHistoryChange(PresenterEnum.CALENDER.getId());
        } else {
            App.get().onHistoryChange(historyToken);
        }
    }

    // ----------------------------------------------------------- Layout Stuff

    // --------------------------------------------------------- Business Stuff

    /**
     * Gets list of memberships sorted by significance for the current logged in user.
     * @return list of memberships sorted by significance for the current logged in user
     */
    public List<Membership> getMemberships() {
        return memberships;
    }
    /**
     * Sets list of memberships sorted by significance for the current logged in user.
     * @param memberships list of memberships sorted by significance for the current logged in user
     */
    public void setMemberships(final List<Membership> memberships) {
        this.memberships = memberships;
    }

    /**
     * Gets list of unit members in currently selected unit.
     * @return unit members in currently selected unit
     */
    public List<User> getUnitMembers() {
        return unitMembers;
    }
    /**
     * Sets list of unit members in currently selected unit.
     * @param unitMembers unit members in currently selected unit
     */
    public void setUnitMembers(final List<User> unitMembers) {
        this.unitMembers = unitMembers;
    }

    /**
     * Gets available patients in currently selected unit.
     * @return patients in currently selected unit
     */
    public List<Patient> getPatients() {
        return patients;
    }
    /**
     * Sets patients for currently selected unit.
     * @param patients patients for currently selected unit
     */
    public void setPatients(final List<Patient> patients) {
        this.patients = patients;
    }

    /**
     * Gets available procedures in currently selected unit.
     * @return procedures in currently selected unit
     */
    public List<Procedure> getAllProcedures() {
        return procedures;
    }
    /**
     * Gets available procedures in currently selected unit by given type.
     * @param type type of procedure
     * @return procedures in currently selected unit of specified type
     */
    public List<Procedure> getProcedures(final Event.Type type) {
        if (null == type) { throw new NullPointerException("procedure type cannot be null"); }
        if (null == procedures) { throw new NullPointerException("procedures are null (not loaded?)"); }
        final List<Procedure> rslt = new ArrayList<Procedure>();
        for (Procedure p : procedures) {
            if (p.enumType() == type) { rslt.add(p); }
        }
        return rslt;
    }
    /**
     * Sets procedures for currently selected unit.
     * @param procedures procedures for currently selected unit
     */
    public void setProcedures(final List<Procedure> procedures) {
        this.procedures = procedures;
    }

    /**
     * Sets index of currently selected unit.
     * @param selectedUnitIndex index of currently selected unit
     */
    public void setSelectedUnitIndex(final int selectedUnitIndex) {
        if (selectedUnitIndex < 0 || (null != memberships && selectedUnitIndex >= memberships.size())) {
            throw new IllegalArgumentException("invalid index: " + selectedUnitIndex);
        }
        this.selectedUnitIndex = selectedUnitIndex;
    }
    /**
     * Gets the current selected membership.
     * @return the current selected membership
     */
    public Membership getSelectedMembership() {
        if (null == memberships) { throw new NullPointerException("memberships is null"); }
        final Membership memb = memberships.get(selectedUnitIndex);
        if (null == memb) { throw new NullPointerException("selected membership is null"); }
        if (null == memb.getUnit()) { throw new NullPointerException("unit of selected membership is null"); }
        return memb;
    }
    /**
     * Gets the current selected unit.
     * @return the current selected unit
     */
    public Unit getSelectedUnit() {
        return getSelectedMembership().getUnit();
    }
    /**
     * Gets the text variant of the current selected unit.
     * @return the text variant
     */
    public int getSelectedUnitTextVariant() {
        return getSelectedUnit().enumTextVariant().ordinal();
    }

    /**
     * Sets index of currently selected unit member.
     * @param selectedUnitMemberIndex index of currently selected unit member
     */
    public void setSelectedUnitMemberIndex(final int selectedUnitMemberIndex) {
        this.selectedUnitMemberIndex = selectedUnitMemberIndex;
    }
    /**
     * Gets the current selected unit member.
     * @return the current selected unit member
     */
    public User getSelectedUnitMember() {
        if (null == unitMembers) { throw new NullPointerException("unit members are null"); }
        final User user = unitMembers.get(selectedUnitMemberIndex);
        if (null == user) { throw new NullPointerException("selected unit member is null"); }
        return user;
    }
    /**
     * Returns the true if the current selected unit member
     * is administrator of selected unit.
     * @return true if the current selected unit member
     * is administrator, otherwise false
     */
    public boolean isSelectedUnitMemberAdmin() {
        if (null == memberships) { throw new NullPointerException("memberships is null"); }
        final Membership memb = memberships.get(selectedUnitIndex);
        if (null == memb) { throw new NullPointerException("selected membership is null"); }
        return Membership.Role.ADMIN == memb.enumRole();
    }
    /**
     * Gets a date in currently displayed week in calendar.
     * @return a date in currently displayed week in calendar
     */
    public Date getWeekDate() {
        return weekDate;
    }

    /**
     * Sets a date in currently displayed week in calendar.
     * @param weekDate a date in currently displayed week in calendar
     */
    public void setWeekDate(final Date weekDate) {
        this.weekDate = weekDate;
    }

    // ---------------------------------------------------------------- Helpers

    /**
     * Resolve base URL of application. Depends on actual running mode:
     * <ul>
     *      <li>Production - returns configured value</li>
     *      <li>Hosted mode - returns always /</li>
     * </ul>
     * @return current URL prefix
     */
    public String getBaseUrl() {
        final String resultUrlPrefix;
        if (GWT.isScript()) {
            resultUrlPrefix = BASE_URL;
        } else {
            resultUrlPrefix = "";
        }

        return resultUrlPrefix;
    }

}
