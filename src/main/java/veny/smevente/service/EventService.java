package veny.smevente.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import veny.smevente.client.utils.Pair;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.User;

/**
 * Event service.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 14.11.2010
 */
public interface EventService {

    /**
     * Stores (creates or updates) an event.<p/>
     * The criterion to decide if create or update is entity's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param event event to be created
     * @return created event
     */
    Event storeEvent(Event event);

    /**
     * Creates and sends a new special event.
     *
     * @param event event to be created
     * @return created event
     */
    Event createAndSendSpecialEvent(Event event);

    /**
     * Removes given event.
     *
     * @param eventId event ID to delete
     */
    void deleteEvent(Object eventId);

    /**
     * Gets text for event with given ID constructed the same way how 'bulkSend' make it.
     *
     * @param eventId event ID to construct the text
     * @return text of the event
     */
    String getEventText(Object eventId);

    /**
     * Gets event by given ID.
     *
     * @param eventId event ID
     * @return the event
     */
    Event getEvent(Object eventId);

    /**
     * Gets list of events for given author and time period.
     * This methods does not distinguish between units to see all terms of a given author.
     *
     * @param authorId author ID
     * @param from date from
     * @param to date to
     * @return list of events
     */
    List<Event> findEvents(Object authorId, Date from, Date to);

    /**
     * Gets list of all events for given patient.
     * It filters special events of type Event.Type.IMMEDIATE_MESSAGE.
     *
     * @param customerId customer ID
     * @return the customer and his list of events sorted descending by start date
     */
    Pair<Customer, List<Event>> findEventsByCustomer(Object customerId);

    /**
     * Sends message of given event through channel defined by customer preferences (SMS, email, ...).
     *
     * @param event event to send
     * @return the event
     */
    Event send(Event event);

    /**
     * Invoked by cron task to send events.
     *
     * @return count of successfully sent events
     */
    int bulkSend();

    /**
     * Gets events statistics for given user and unit.
     * Returns:<ul>
     * <li>only one item if given user is MEMBER in given unit
     * <li>list of items for all unit members if the given user is ADMIN in given unit
     * </ul>
     *
     * @param unitId unit ID
     * @param userId user ID
     * @param from date from
     * @param to date to
     * @return list of event statistics
     */
    List<Pair<User, Map<String, Integer>>> getEventStatistic(Object unitId, Object userId, Date from, Date to);

    /**
     * Loads all events.
     *
     * @return list of all event
     */
    @Deprecated // only for unit testing purposes
    List<Event> getAllEvents();

}
