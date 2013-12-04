package veny.smevente.client.l10n;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public interface SmeventeMessages extends Messages {

    // CHECKSTYLE:OFF
    String clientSideError(String clazz, String message, String stack);
    String clientSideErrorWithAction(String clazz, String message, String action, String stack);
    String serverSideError(String clazz, String message);

    String deleteUserQuestion(String name);
    String deleteSmsQuestion(String patientName);
    String deleteSpecialSmsQuestion(String name);
    String sendNowQuestion(String patientName);
    String termsInWeek(int delivered, int sum);

    // validation
    String validationTextLength(int min, int max);
    String validationTextLengthSmall(String border);
    String validationTextLengthBig(String border);
    // CHECKSTYLE:ON

}
