package veny.smevente.client.uc;

import java.util.Date;

import veny.smevente.client.App;
import veny.smevente.client.mvp.AbstractPresenter;
import veny.smevente.client.mvp.View;
import veny.smevente.model.PatientDto;
import veny.smevente.model.SmsDto;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Label;

/**
 * SMS Dialog Presenter.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class SmsDetailDlgPresenter extends AbstractPresenter<SmsDetailDlgPresenter.SmsDetailDlgView> {

    /**
     * SMS Dialog View interface.
     *
     * @author Vaclav Sykora [vaclav.sykora@gmail.com]
     * @since 0.1
     */
    public interface SmsDetailDlgView extends View {
        /**
         * @return the from time
         */
        Label getDate();
        /**
         * @return the to time
         */
        Label getTime();
        /**
         * @return the medical help label
         */
        Label getMedicalHelpLabel();
        /**
         * @return the medical help
         */
        Label getMedicalHelp();
        /**
         * @return the name
         */
        Label getName();
        /**
         * @return the phone number
         */
        Label getPhoneNumber();
        /**
         * @return the SMS text
         */
        Label getSmsText();
        /**
         * @return the notice
         */
        Label getNotice();
        /**
         * @return the date of delivering
         */
        Label getSent();
    }

    /**
     * Initializes presenter.
     * @param sms SMS
     */
    public void init(final SmsDto sms) {
        // clear all the stuff
        clean();

        final Date startTime = sms.getMedicalHelpStartTime();
        final Date endTime = new Date(startTime.getTime() + (sms.getMedicalHelpLength() * 60 * 1000));
        final PatientDto patient = sms.getPatient();

        getView().getDate().setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG).format(startTime));
        getView().getTime().setText(
                DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(startTime) + " - "
                + DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(endTime));
        getView().getSmsText().setText(sms.getText());
        getView().getNotice().setText(sms.getNotice());
        getView().getSent().setText(
                DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(sms.getSent())
                + " [id=" + sms.getId() + "]");
        getView().getName().setText(patient.getFullname() + " [" + patient.getFormattedBirthNumber() + "]");
        getView().getPhoneNumber().setText(patient.getPhoneNumber());
        getView().getMedicalHelpLabel().setText(
                CONSTANTS.medicalHelp()[App.get().getSelectedUnitTextVariant()]);
        getView().getMedicalHelp().setText(sms.getMedicalHelpCategory().getName());
    }
    // -------------------------------------------------------- Presenter Stuff

    /** {@inheritDoc} */
    @Override
    protected void onBind() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnbind() {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    protected void onShow(final Object parameter) {
        // nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    protected void onHide() {
        clean();
    }

    /** {@inheritDoc} */
    @Override
    public void clean() {
        view.getDate().setText("");
        view.getTime().setText("");
        view.getMedicalHelp().setText("");
        view.getName().setText("");
        view.getPhoneNumber().setText("");
        view.getSmsText().setText("");
        view.getNotice().setText("");
        view.getSent().setText("");
    }

    // -------------------------------------------------------- Assistant Stuff

}
