package veny.smevente.client.uc;

import veny.smevente.client.IdleBar;
import veny.smevente.client.Menu;
import veny.smevente.client.uc.HeaderPresenter.HeaderView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Portal Header View implementation.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class HeaderViewImpl extends Composite implements HeaderView {


    /** UIBinder template. */
    private static final Binder BINDER = GWT.create(Binder.class);

    // CHECKSTYLE:OFF
    @UiField Anchor logout;
    @UiField ListBox languages;
    @UiField InlineLabel username;
    @UiField ListBox units;
    @UiField ListBox unitMembers;
    @UiField Label limitedVersion;
    @UiField HTMLPanel loadingBar;
    @UiField IdleBar idlebar;
    @UiField Menu menu;
    // CHECKSTYLE:ON

    /** UIBinder template. */
    @UiTemplate("header.ui.xml")

    interface Binder extends UiBinder<HTMLPanel, HeaderViewImpl> { }

    /**
     * Constructor.
     */
    public HeaderViewImpl() {
        initWidget(BINDER.createAndBindUi(this));
        loadingBar.setVisible(false);
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Anchor getLogout() {
        return logout;
    }
    /** {@inheritDoc} */
    @Override
    public ListBox getLanguages() {
        return languages;
    }
    /** {@inheritDoc} */
    @Override
    public Label getUsername() {
        return username;
    }
    /** {@inheritDoc} */
    @Override
    public ListBox getUnits() {
        return units;
    }
    /** {@inheritDoc} */
    @Override
    public ListBox getUnitMembers() {
        return unitMembers;
    }
    /** {@inheritDoc} */
    @Override
    public Label getLimitedVersion() {
        return limitedVersion;
    }
    /** {@inheritDoc} */
    @Override
    public Panel getLoadingBar() {
        return loadingBar;
    }
    /** {@inheritDoc} */
    @Override
    public IdleBar getIdleBar() {
        return idlebar;
    }
    /** {@inheritDoc} */
    @Override
    public Menu getMenu() {
        return menu;
    }
}
