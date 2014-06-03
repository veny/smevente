package veny.smevente.client;

import java.util.Date;

import veny.smevente.client.mvp.SingletonEventBus;
import veny.smevente.client.utils.EventWidgetEvent;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * This class represents a UI widget used to display an event.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 1.7.2010
 */
public class EventWidget extends FlowPanel implements HasClickHandlers /*HasDoubleClickHandlers*/ {

    /** Wrapped event. */
    private final Event event;

    /**
     * Constructor.
     * @param event event to be wrapped
     */
    public EventWidget(final Event event) {
        // PRE-CONDITIONS
        if (null == event) { throw new NullPointerException("event cannot be null"); }
        if (null == event.getAuthor()) { throw new NullPointerException("author cannot be null"); }
        if (null == event.getCustomer()) { throw new NullPointerException("customer cannot be null"); }
        if (null == event.getProcedure()) {
            throw new NullPointerException("procedure cannot be null");
        }

        final Label header = new Label();
        header.addStyleName("sms-widget-header");
        this.add(header);
        final Label notice = new Label();
        notice.addStyleName("sms-widget-notice");
        this.add(notice);

        this.event = event;
        final Customer customer = event.getCustomer();
        final Procedure procedure = event.getProcedure();

        if (null == event.getSent()) {
            if (event.getSendAttemptCount() >= Event.MAX_SEND_ATTEMPTS) {
                addStyleName("sms-widget-failed");
            } else {
                addStyleName("sms-widget");
            }
        } else {
            addStyleName("sms-widget-sent");
        }
        DOM.setStyleAttribute(getElement(), "backgroundColor", "#" + procedure.getColor());

        // header text
        final Date startTime = event.getStartTime();
        final Date endTime = new Date(startTime.getTime() + (event.getLength() * 60 * 1000));

        @SuppressWarnings("deprecation")
        final StringBuilder text = new StringBuilder(customer.getFirstname())
            .append(' ')
            .append(customer.getSurname())
            .append(" [")
            .append(DateTimeFormat.getShortTimeFormat().format(startTime))
            .append("-")
            .append(DateTimeFormat.getShortTimeFormat().format(endTime))
            .append(']');
        header.setText(text.toString());

        // event text
        notice.setText(event.getNotice());

        // click -> popup menu
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                event.stopPropagation();
                final EventWidgetEvent eventWidgetEvent = new EventWidgetEvent(EventWidget.this);
                SingletonEventBus.get().fireEvent(eventWidgetEvent);
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
     * Gets wrapped event.
     * @return wrapped event
     */
    public Event getEvent() {
        return event;
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
