package veny.smevente.client;

import com.google.gwt.user.client.ui.Label;


/**
 * This component is displayed to user if there is some idle
 * between firing some action and the moment when is the
 * action results propagated.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 7.10.2010
 * {@link http://visionmasterdesigns.com/tutorial-adding-a-ajax-spinner-in-the-center-of-web-page-using-prototype/}
 * {@link http://kilianvalkhof.com/2010/css-xhtml/css3-loading-spinners-without-images/}
 * {@link http://ajaxload.info/}
 */
public class IdleBar extends Label {

    /**
     * Non-parameterized class constructor.
     */
    public IdleBar() {
        setStyleName("idleBar-loading");
        // default not visible
        hide();
    }

    /**
     * Shows idle bar.
     */
    public void show() {
        this.setVisible(true);
    }

    /**
     * Hides idle bar.
     */
    public void hide() {
        this.setVisible(false);
    }

}
