package veny.smevente.client.uc;

import java.util.List;

import veny.smevente.client.App;
import veny.smevente.client.IdleBar;
import veny.smevente.client.Menu;
import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.SwitchLocalilzationEvent;
import veny.smevente.client.SwitchLocalilzationEvent.SwitchLocalilzationEventHandler;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.client.rest.AbstractRestCallbackWithErrorHandling;
import veny.smevente.client.rest.AjaxEvent;
import veny.smevente.client.rest.AjaxEvent.AjaxEventHandler;
import veny.smevente.client.rest.RestHandler;
import veny.smevente.client.utils.CrudEvent;
import veny.smevente.client.utils.CrudEvent.CrudEventHandler;
import veny.smevente.client.utils.HeaderEvent;
import veny.smevente.model.Membership;
import veny.smevente.model.Unit;
import veny.smevente.shared.EntityTypeEnum;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;

/**
 * Portal Header Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 12.8.2010
 */
public class HeaderPresenter extends AbstractPresenter<HeaderPresenter.HeaderView>
    implements AjaxEventHandler, SwitchLocalilzationEventHandler, CrudEventHandler {

    /**
     * Portal Header View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 0.1
     */
    public interface HeaderView extends View {
        /**
         * @return 'logout' element
         */
        Anchor getLogout();
        /**
         * @return the languages list box
         */
        ListBox getLanguages();
        /**
         * @return the username
         */
        Label getUsername();
        /**
         * @return the units list box
         */
        ListBox getUnits();
        /**
         * @return the unit members list box
         */
        ListBox getUnitMembers();
        /**
         * @return the label with text of limited version
         */
        Label getLimitedVersion();
        /**
         * @return the loading bar
         */
        Panel getLoadingBar();
        /**
         * Gets the idle bar if some request is running.
         * @return idle bar by request
         */
        IdleBar getIdleBar();

        /**
         * Gets the main menu component.
         * @return the main menu component
         */
        Menu getMenu();
    }

    /** Handler registration for AjaxEvent in the Event Bus. */
    private HandlerRegistration ebusRegistrationAjax;
    /** Handler registration for language switch in the Event Bus. */
    private HandlerRegistration ebusRegistrationLang;
    /** Handler registration for user CRUD in the Event Bus. */
    private HandlerRegistration ebusUserCrud;

    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        // register this to display/hide the loading progress bar
        ebusRegistrationAjax = eventBus.addHandler(AjaxEvent.TYPE, this);
        // register this to switch the localization
        ebusRegistrationLang = eventBus.addHandler(SwitchLocalilzationEvent.TYPE, this);
        // register this to update unit members
        ebusUserCrud = eventBus.addHandler(CrudEvent.TYPE, this);

        // 'Logout' button
        view.getLogout().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                App.get().logout();
            }
        });

        // languages
        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        if (currentLocale.equals("default")) {
            currentLocale = "en";
        }
        // set the language drop down items
        final ListBox languages = view.getLanguages();
        String[] localeNames = LocaleInfo.getAvailableLocaleNames();
        for (String localeName : localeNames) {
            if (!localeName.equals("default") && (localeName.equals("en") || localeName.equals("cs"))) {
                String nativeName = LocaleInfo.getLocaleNativeDisplayName(localeName);
                languages.addItem(nativeName, localeName);
                if (localeName.equals(currentLocale)) {
                    languages.setSelectedIndex(languages.getItemCount() - 1);
                }
            }
        }
        // register the language change handler
        languages.addChangeHandler(new ChangeHandler() {
            public void onChange(final ChangeEvent event) {
                String localeName = languages.getValue(languages.getSelectedIndex());
                UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", localeName);
                Window.Location.replace(builder.buildString());
            }
        });

        // Unit drop down
        view.getUnits().addChangeHandler(new ChangeHandler() {
            public void onChange(final ChangeEvent event) {
                unitSelected(view.getUnits().getSelectedIndex());
            }
        });

        // Member drop down
        view.getUnitMembers().addChangeHandler(new ChangeHandler() {
            public void onChange(final ChangeEvent event) {
                memberSelected(view.getUnitMembers().getSelectedIndex());
                App.get().switchToPresenterByType(PresenterEnum.CALENDER, null);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        ebusRegistrationAjax.removeHandler();
        ebusRegistrationLang.removeHandler();
        ebusUserCrud.removeHandler();
    }

    /** {@inheritDoc} */
    @Override
    protected void onShow(final Object parameter) {
        loadUserInfo();
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        // nothing to do here
    }

    // ---------------------------------- SwitchLocalilzationEventHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void switchLocalization(final SwitchLocalilzationEvent event) {
        final String localeName = event.getLocalization();
        final ListBox languages = view.getLanguages();
        for (int i = 0; i < languages.getItemCount(); i++) {
            if (languages.getValue(i).equals(localeName)) {
                languages.setSelectedIndex(i);
                UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", localeName);
                Window.Location.replace(builder.buildString());
                break;
            }
        }
    }

    // ------------------------------------------------- AjaxEventHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void requestSent(final AjaxEvent event) {
        view.getLoadingBar().setVisible(true);
        view.getIdleBar().show();
    }

    /** {@inheritDoc} */
    @Override
    public void responseReceived(final AjaxEvent event) {
        view.getLoadingBar().setVisible(false);
        view.getIdleBar().hide();
    }

    // ------------------------------------------------- CrudEventHandler Stuff

    /** {@inheritDoc} */
    @Override
    public void create(final CrudEvent event) {
        if (EntityTypeEnum.USER == event.getEntityType()) {
            reloadCurrentUnitInfo();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void read(final CrudEvent event) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void update(final CrudEvent event) {
        if (EntityTypeEnum.USER == event.getEntityType()) {
            reloadCurrentUnitInfo();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(final CrudEvent event) {
        if (EntityTypeEnum.USER == event.getEntityType()) {
            reloadCurrentUnitInfo();
        }
    }

    // -------------------------------------------------------- Assistant Stuff

    /**
     * Loads information about user context:<ul>
     * <li>username
     * <li>units
     * </ul>.
     * After loading it sets the first unit as active and selected in drop down menu.
     */
    private void loadUserInfo() {
        // get user info
        final RestHandler rest = new RestHandler("/rest/user/info/");
        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
            @Override
            public void onSuccess(final String jsonText) {
                // username
                final String username = App.get().getJsonDeserializer().createString("username", jsonText);
                view.getUsername().setText(username);

                // memberships
                final List<Membership> membs =
                        App.get().getJsonDeserializer().deserializeList(Membership.class, "memberships", jsonText);
                appInit(membs);
                // fill the drop down again
                view.getUnits().clear();
                view.getUnits().setSelectedIndex(0);
                for (Membership m : membs) { view.getUnits().addItem(m.unitName()); }
                // simulate selection on first unit -> fire event
                unitSelected(0);
            }
        });
        rest.get();
    }

    /**
     * Reload the current unit info, used when some user CRUD occurred.
     */
    private void reloadCurrentUnitInfo() {
        throw new IllegalStateException("not implemented yet");
//        // get unit info
//        RestHandler rest = new RestHandler("/rest/user/info/");
//        rest.setCallback(new AbstractRestCallbackWithErrorHandling() {
//            @Override
//            public void onSuccess(final String jsonText) {
//                final List<Unit> usedUnits = App.get().getUnits();
//                int selectedUnitIndex = view.getUnits().getSelectedIndex();
//                List<Unit> units = App.get().getJsonDeserializer().deserializeList(Unit.class, "units", jsonText);
//                for (Unit newUnit : units) {
//                    if (newUnit.getId().equals(usedUnits.get(selectedUnitIndex).getId())) {
//                        usedUnits.remove(selectedUnitIndex);
//                        usedUnits.add(selectedUnitIndex, newUnit);
//                        // change content of members drop down
//                        view.getUnitMembers().clear();
//                        for (Membership m : newUnit.getMembers()) {
//                            view.getUnitMembers().addItem(m.getUser().getFullname());
//                        }
//                        break;
//                    }
//                }
//            }
//        });
//        rest.get();
    }

    /**
     * Initialize the application with found memberships.
     * @param membs list of memberships
     */
    private void appInit(final List<Membership> membs) {
        App.get().setMemberships(membs);
        view.getMenu().setupAdminItems();
        if (!App.get().isSelectedUnitMemberAdmin()) {
            App.get().switchToPresenterByType(PresenterEnum.CALENDER, null);
        }
    }

    /**
     * Reaction to selection of an unit in header drop down.
     * @param idx index of selected item
     */
    private void unitSelected(final int idx) {
        App.get().setSelectedUnitIndex(idx);
        final Unit newUnit = App.get().getSelectedUnit();
        // just to be sure
        if (null == newUnit) { throw new IllegalStateException("selected unit cannot be null"); }

        // show/hide 'Limited version' text
        view.getLimitedVersion().setVisible(null != newUnit.getLimitedSmss());

        // change content of members drop down
        view.getUnitMembers().clear();
//XXX        for (Membership m : newUnit.getMembers()) {
//            view.getUnitMembers().addItem(m.getUser().getFullname());
//        }
        // the current logged in user is always on the first position (index 0)
        App.get().setSelectedUnitMemberIndex(0);

        // fire event (unit)
        eventBus.fireEvent(new HeaderEvent(newUnit));
        // fire event (unit member)
//XXX        eventBus.fireEvent(new HeaderEvent(App.get().getSelectedUnitMember()));
    }

    /**
     * Reaction to selection of an unit member in header drop down.
     * @param idx index of selected item
     */
    private void memberSelected(final int idx) {
        App.get().setSelectedUnitMemberIndex(idx);

        // fire event
        final HeaderEvent e = new HeaderEvent(App.get().getSelectedUnitMember());
        eventBus.fireEvent(e);
    }
}
