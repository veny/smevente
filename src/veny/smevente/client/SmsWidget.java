package veny.smevente.client;

import java.util.Date;

import veny.smevente.client.mvp.SingletonEventBus;
import veny.smevente.client.utils.SmsWidgetEvent;
import veny.smevente.model.MedicalHelpCategoryDto;
import veny.smevente.model.PatientDto;
import veny.smevente.model.SmsDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * This class represents a UI widget used to display a SMS.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public class SmsWidget extends FlowPanel implements HasClickHandlers /*HasDoubleClickHandlers*/ {

    /** Wrapped SMS. */
    private final SmsDto sms;

    /**
     * Constructor.
     * @param sms SMS to be wrapped
     */
    public SmsWidget(final SmsDto sms) {
        // PRE-CONDITIONS
        if (null == sms) { throw new NullPointerException("SMS cannot be null"); }
        if (null == sms.getAuthor()) { throw new NullPointerException("SMS author cannot be null"); }
        if (null == sms.getPatient()) { throw new NullPointerException("patient cannot be null"); }
        if (null == sms.getMedicalHelpCategory()) {
            throw new NullPointerException("medical help category cannot be null");
        }

        final Label header = new Label();
        header.addStyleName("sms-widget-header");
        this.add(header);
        final Label notice = new Label();
        notice.addStyleName("sms-widget-notice");
        this.add(notice);

        this.sms = sms;
        final PatientDto patient = sms.getPatient();
        final MedicalHelpCategoryDto mhc = sms.getMedicalHelpCategory();

        if (null == sms.getSent()) {
            if (sms.getSendAttemptCount() >= SmsDto.MAX_SEND_ATTEMPTS) {
                addStyleName("sms-widget-failed");
            } else {
                addStyleName("sms-widget");
            }
        } else {
            addStyleName("sms-widget-sent");
        }
        DOM.setStyleAttribute(getElement(), "backgroundColor", "#" + mhc.getColor());

        // header text
        final Date startTime = sms.getMedicalHelpStartTime();
        final Date endTime = new Date(startTime.getTime() + (sms.getMedicalHelpLength() * 60 * 1000));

        @SuppressWarnings("deprecation")
        final StringBuilder text = new StringBuilder(patient.getFirstname())
            .append(' ')
            .append(patient.getSurname())
            .append(" [")
            .append(DateTimeFormat.getShortTimeFormat().format(startTime))
            .append("-")
            .append(DateTimeFormat.getShortTimeFormat().format(endTime))
            .append(']');
        header.setText(text.toString());

        // notice text
        notice.setText(sms.getNotice());

        // click -> popup menu
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                event.stopPropagation();
                final SmsWidgetEvent smsWidgetEvent = new SmsWidgetEvent(SmsWidget.this);
                SingletonEventBus.get().fireEvent(smsWidgetEvent);
            }
        });

//        addDoubleClickHandler(new DoubleClickHandler() {
//            @Override
//            public void onDoubleClick(final DoubleClickEvent event) {
//                event.stopPropagation();
//                final SmsWidgetEvent smsWidgetEvent = new SmsWidgetEvent(SmsWidget.this);
//                SingletonEventBus.get().fireEvent(smsWidgetEvent);
//            }
//        });
    }

    /**
     * Gets wrapped SMS triple.
     * @return wrapped SMS triple
     */
    public SmsDto getSms() {
        return sms;
    }

    // ------------------------------------------------- HasClickHandlers Stuff

    /** {@inheritDoc} */
    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    // ------------------------------------------- HasDoubleClickHandlers Stuff

//    /** {@inheritDoc} */
//    @Override
//    public HandlerRegistration addDoubleClickHandler(final DoubleClickHandler handler) {
//        return addDomHandler(handler, DoubleClickEvent.getType());
//    }

    // -------------------------------------------------------- Assistant Stuff

}
