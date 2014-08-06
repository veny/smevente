package veny.smevente.client.l10n;

import com.google.gwt.i18n.client.Constants;

/**
 * I18n constants.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public interface SmeventeConstants extends Constants {


    // CHECKSTYLE:OFF
    // ---------------------------------------------------------------- Actions
    String logout();
    String login();
    String minimize();
    String maximize();
    String add();
    String save();
    String cancel();
    String restore();
    String close();
    String change();
    String search();
    String delete();
    String advancedSettings();
    String find();
    String update();
    String show();
    String send();
    String management();
    String sendNow();
    String selectColor();

    // ----------------------------------------------------------- Dialog texts
    String ok();
    String yes();
    String no();
    String error();
    String warning();
    String information();
    String confirm();

    // --------------------------------------------------------------- UI texts
    String from();
    String to();
    String or();
    String and();
    String is();
    String in();
    String date();
    String time();
    String settings();
    String version();
    String user();
    String users();
    String username();
    String fullname();
    String root();
    String unitRole();
    String unitOrder();
    String updatePassword();
    String password();
    String passwordAgain();
    String usernameAndPassword();
    String about();
    String aboutSmevente();
    String urlAddress();
    String unspecifiedError();
    String name();
    String firstname();
    String surname();
    String degreeBefore();
    String degreeAfter();
    String employer();
    String careers();
    String phone();
    String phoneNumber();
    String address();
    String website();
    String birthday();
    String birthNumber();
    String email();
    String of();
    String comments();
    String participantId();
    String unit();
    String unitMember();
    String inUnit();
    String[] patient();
    String procedure();
    String procedures();
    String procedureLength();
    String addProcedure();
    String menu();
    String view();
    String calendar();
    String today();
    String degree();
    String street();
    String city();
    String zipCode();
    String addUser();
    String userAdded();
    String userUpdated();
    String[] findPatient();
    String[] addPatient();
    String[] patientAdded();
    String[] patientUpdated();
    String deleteSmsQuestion();
    String[] deletePatientQuestion();
    String overall();
    String sent();
    String failed();
    String deleted();
    String type();
    String notice();
    String badGsmPhoneNumber();
    String oldPassword();
    String newPassword();
    String newPasswordAgain();
    String changePassword();
    String smsStatistics();
    String passwordChanged();
    String[] noPatientInUnit();
    String[] noMhcInUnit();
    String noSpecialSmsInUnit();
    String visits();
    String history();
    String future();
    String noItems();
    String color();
    String procedureAdded();
    String procedureUpdated();
    String[] deleteProcedureQuestion();
    String specialSms();
    String specialSmss();
    String addSpecialSms();
    String specialSmsAdded();
    String specialSmsUpdated();
    String nextWeek();
    String prevWeek();
    String createEvent();
    String limitedVersion();
    String roleAdmin();
    String roleMember();
    String message();
    String event();
    String smsChannel();
    String emailChannel();
    String premiumService();

    // ------------------------------------------------------------- Validation
    String validationRequired();
    String validationEmpty();
    String validationBadFormat();
    String validationFillAtLeastOne();
    String validationNotSelected();
    String validationNotNumber();
    String validationNotGreaterZero();
    String validationDuplicateValue();
    String validationFromBiggerTo();
    String validationChangePassword();
    String validationChangePasswordNew();
    String validationOldPasswordBad();
    String validationNotFound();
    String smsLimitExceeded();
    String validationEmptyIfCheckboxSelected();

    // ----------------------------------------------------------------- Header
    String headerLoading();
    String headerLanguage();
    String headerTheme();
    String headerBreadcrumbLabel();

    // ------------------------------------------------------------------ Login
    String authenticationHeaderLogin();
    String authenticationValidationBadUsernamePassword();

    // ------------------------------------------------------------------ Event
    String smsText();
    String smsSent();
    // CHECKSTYLE:ON

}
