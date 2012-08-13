package veny.smevente.client;

import veny.smevente.client.l10n.SmeventeMessages;
import veny.smevente.shared.ExceptionJsonWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Implementation of the client side error handler
 * that displays problems in the JS alert window.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class AlertFailureHandler implements FailureHandler {

    /** I18n. */
    private static final SmeventeMessages MESSAGES = GWT.create(SmeventeMessages.class);

    /** {@inheritDoc} */
    @Override
    public void handleClientError(final Throwable th, final String detail) {
        Window.alert(MESSAGES.clientSideError(th.getClass().getName(), th.getMessage(), getCustomStackTrace(th)));
    }

    /** {@inheritDoc} */
    @Override
    public void handleClientErrorWithAction(final Throwable th, final String action, final String detail) {
        Window.alert(MESSAGES.clientSideErrorWithAction(
                th.getClass().getName(), th.getMessage(), action, getCustomStackTrace(th)));
    }

    /** {@inheritDoc} */
    @Override
    public void handleServerError(final ExceptionJsonWrapper exWrapper) {
        Window.alert(MESSAGES.serverSideError(exWrapper.getClassName(), exWrapper.getMessage()));
    }

    /**
     * Defines a custom format for the stack trace as String.
     * @param t throwable to be serialized
     * @return exception stack trace
     */
    public static String getCustomStackTrace(final Throwable t) {
        //add the class name and any message passed to constructor
        final StringBuilder result = new StringBuilder();
        //add each element of the stack trace
        for (StackTraceElement element : t.getStackTrace()) {
            result.append(element);
            result.append("\n");
        }
        return result.toString();
    }

}
