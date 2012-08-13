package veny.smevente.client.mvp;

import veny.smevente.client.PresenterCollection.PresenterEnum;
import veny.smevente.client.l10n.SmeventeConstants;
import veny.smevente.client.l10n.SmeventeMessages;
import veny.smevente.client.rest.RestHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

import eu.maydu.gwt.validation.client.ValidationProcessor;

/**
 * A basic presenter that should be ancestor of all specific presenters.
 * It prepares the {@link SingletonEventBus} to be used.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 *
 * @param <D> a class extending {@link View}
 * @see EventBus
 */
public abstract class AbstractPresenter<D extends View> implements Presenter<D> {

    /** I18n constants. */
    protected static final SmeventeConstants CONSTANTS = GWT.create(SmeventeConstants.class);
    /** I18n messages. */
    protected static final SmeventeMessages MESSAGES = GWT.create(SmeventeMessages.class);

    // CHECKSTYLE:OFF
    /** The view for the presenter. */
    protected D view;

    /** The UC client side validator. */
    protected ValidationProcessor validator;

    /** The event bus for the application. */
    protected final EventBus eventBus = SingletonEventBus.get();
    // CHECKSTYLE:ON

    /** Presenter hidden or visible flag. */
    private boolean presenterVisible;

    /** Presenter identification. */
    private PresenterEnum id;

    /** {@inheritDoc} */
    @Override
    public final D getView() {
        return view;
    }

    /**
     *  Gets the UC client side validator.
     *  @return the UC client side validator
     */
    public ValidationProcessor getValidator() {
        return validator;
    }

    /** {@inheritDoc} */
    @Override
    public final void bind(final D view) {
        this.view = view;
        onBind();
    }

    /** {@inheritDoc} */
    @Override
    public final void unbind() {
        this.view = null;
        onUnbind();
    }

    /**
     * This method is called when binding the presenter. Any additional bindings
     * should be done here.
     */
    protected abstract void onBind();

    /**
     * This method is called when unbinding the presenter. Any additional cleanups
     * should be done here.
     */
    protected abstract void onUnbind();

    /** {@inheritDoc} */
    @Override
    public void show(final Object parameters) {
        presenterVisible = true;
        onShow(parameters);
    }

    /** {@inheritDoc} */
    @Override
    public void hide() {
        presenterVisible = false;
        onHide();
        clean();
    }

    /**
     * Called when the panel is added as a Main Panel.
     * To be sub-classed and overridden,
     * if there is an action presenter needs to perform.
     * @param parameter parameter to pass to the presenter
     */
    protected abstract void onShow(Object parameter);

    /**
     * Called when the panel is removed from the Main Panel.
     * To be sub-classed and overridden,
     * if there is an action presenter needs to perform.
     * Panel clean up should be done here (e. g. hiding opened pop up dialogs).
     */
    protected abstract void onHide();

    /** {@inheritDoc} */
    @Override
    public final boolean isVisible() {
        return presenterVisible;
    }

    /** {@inheritDoc} */
    @Override
    public final PresenterEnum getId() {
        if (null == id) { throw new NullPointerException("ID is null (did you invoke 'setId' before?)"); }
        return id;
    }
    /**
     * Sets the presenter identification.
     * @param id the presenter identification
     */
    public final void setId(final PresenterEnum id) {
        this.id = id;
    }

    /**
     * Create ClientRestHandler that is bound to the visibility of this presenter.
     * The response will not be sent to the callback if this presenter is not visible.
     * @param uri the target URI
     * @return created ClientRestHandler
     */
    protected RestHandler createClientRestHandler(final String uri) {
        return new RestHandler(uri, this);
    }

    /**
     * Create exclusive ({@link RestHandler#getExclusiveIndex()}) ClientRestHandler
     * that is bound to the visibility of this presenter.
     * The response will not be sent to the callback if this presenter is not visible.
     * @param uri the target URI
     * @return created ClientRestHandler
     */
    protected RestHandler createExclusiveClientRestHandler(final String uri) {
        return new RestHandler(uri, this, true);
    }

    /**
     * Update width of dynamically resizable column.
     */
//    protected void updateWidth() {
//        if (view instanceof ViewWithResizeableColumn) {
//            ViewWithResizeableColumn resizeableView = (ViewWithResizeableColumn) view;
//
//            if (Window.getClientWidth() > Wave.MIN_PAGE_WIDTH) {
//                resizeableView.getResizeableColumn().getElement().getStyle().setWidth(
//                    Window.getClientWidth() - resizeableView.getFixedColumnWidth() - 20, Unit.PX);
//            } else {
//                resizeableView.getResizeableColumn().getElement().getStyle().setWidth(
//                    resizeableView.getMinResizeableWidth(), Unit.PX);
//            }
//        } else {
//            throw new IllegalStateException(
//            "updateWidth method cannot be called using view not implementing ViewWithResizeableColumn interface");
//        }
//    }

    /**
     * Gets standard validation message for a given key.
     * @param key of message
     * @param parameters additional parameters
     * @return validation message
     */
    protected String getValidationMessage(final String key, final Object... parameters) {
        String result;
        if (key.equals("badUsernamePassword")) {
            result = CONSTANTS.authenticationValidationBadUsernamePassword();
        } else if (key.equals("empty")) {
            result = CONSTANTS.validationEmpty();
        } else if (key.equals("notNumber")) {
            result = CONSTANTS.validationNotNumber();
        } else if (key.equals("duplicateValue")) {
            result = CONSTANTS.validationDuplicateValue();
        } else if (key.equals("notFound")) {
            result = CONSTANTS.validationNotFound();
        } else if (key.equals("textLength")) {
            result = MESSAGES.validationTextLength(
                    ((Integer) parameters[0]).intValue(), ((Integer) parameters[1]).intValue());
        } else if (key.equals("textLengthSmall")) {
            result = MESSAGES.validationTextLengthSmall((String) parameters[0]);
        } else if (key.equals("textLengthBig")) {
            result = MESSAGES.validationTextLengthBig((String) parameters[0]);
        } else {
            result = "no message localization given for key: " + key;
        }
        return result;
    }


}
