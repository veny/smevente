package veny.smevente.client;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * Abstract Event Handler invoked on Enter Key Press.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 */
public abstract class AbstractEnterKeyHandler implements KeyUpHandler {

    /** {@inheritDoc} */
    @Override
    public final void onKeyUp(final KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            onEnterKeyPress();
        }
    }

    /**
     * Called when the Enter key is pressed.
     */
    public abstract void onEnterKeyPress();

}
