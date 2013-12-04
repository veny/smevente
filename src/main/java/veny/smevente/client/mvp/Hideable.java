package veny.smevente.client.mvp;

/**
 * Hideable visual component (a GWT Widget).
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public interface Hideable {

    /**
     * Whatever is the component visible.
     * @return true or false, according to the visible or hidden attribute flag
     */
    boolean isVisible();

}
