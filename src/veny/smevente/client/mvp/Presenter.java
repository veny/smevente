package veny.smevente.client.mvp;

import veny.smevente.client.PresenterCollection.PresenterEnum;

/**
 * This class represents the <i>Presenter</i> that acts upon the model and the view.
 * It retrieves data from repositories (the model), persists it,
 * and formats it for display in the view.
 * <br/>
 * It's important to note that the presenter has no knowledge
 * of the actual UI layer of the application.
 * It knows it can talk to an interface,
 * but it does not know or care what the implementation of that interface is.
 * This promotes reuse of presenters between disparate UI technologies.
 * <br/>
 * The presenter, pushes the datas of the model to the view.
 * <p>
 * The life-cycle of a presenter starts with a call to bind()
 * and ends with a call to unbind().
 * Between these two calls, onShow() and onHide() are called.
 * <br/>
 * A typical life-cycle of a presenter follows this pattern:
 * <br/>
 * bind() -> onShow() -> onHide() -> onShow() -> onHide() -> ... -> unbind()
 * <br/>
 * The bind() method is invoked when the presenter is to be displayed
 * for the first time. The unbind() method is invoked when the
 * browser window is being closed.
 * </p>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 *
 * @param <T> a class extending {@link View}
 */
public interface Presenter<T extends View> extends Hideable {

    /**
     * Called when the presenter is initialized. This is called before any other
     * methods. Any event handlers and other setup should be done here rather
     * than in the constructor.
     * @param view the corresponding view
     */
    void bind(T view);

    /**
     * Called if the current view is removed.
     */
    void unbind();

    /**
     * Returns the view for the presenter.
     *
     * @return The view.
     */
    T getView();

    /**
     * Called when the panel is displayed as a Main Panel.
     * Invoked as a sub-process by calling the <code>Wave.switchToPresenterByName()</code> method.
     *
     * @param parameter parameter to pass to the presenter
     */
    void show(Object parameter);

    /**
     * Called when the panel is removed from the Main Panel.
     * Invoked as a sub-process by calling the Wave.switchToPresenterByName() method.
     * Panel clean up should be done here (e. g. hiding opened pop up dialogs).
     */
    void hide();

    /**
     * Cleans the presenter stuff to be prepared for a new use.
     */
    void clean();

    /**
     * Gets the presenter identification.
     * @return the presenter identification
     */
    PresenterEnum getId();

}
