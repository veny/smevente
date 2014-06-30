package veny.smevente.dao;

import java.util.Date;
import java.util.List;

import veny.smevente.model.Event;
import veny.smevente.model.Unit;

/**
 * Interface for persistence operation with <code>Event</code> entity.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 20.7.2010
 */
public interface EventDao extends GenericDao<Event> {

    /**
     * Gets list of events for given author and time period (not deleted).
     *
     * @param authorId author ID
     * @param from from time
     * @param to to time
     * @param includeDeleted whether the deleted SMSs should be included
     * @return list of SMSs
     */
    List<Event> findByAuthorAndPeriod(Object authorId, Date from, Date to, boolean includeDeleted);

    /**
     * Gets list of events for given customer (not deleted and special).
     *
     * @param customerId customer ID
     * @return list of events sorted by start time descending
     */
    List<Event> findByCustomer(Object customerId);

    /**
     * Gets list of events that are older than given timestamp.
     *
     * @param unit unit where the events belong into
     * @param olderThan time that all older SMSs should be found
     * @return list of SMSs
     */
    List<Event> findEvents2BulkSend(Unit unit, Date olderThan);

}
