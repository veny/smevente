package veny.smevente.server;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import veny.smevente.model.Event;
import veny.smevente.model.Membership;
import veny.smevente.model.User;
import veny.smevente.service.EventService;
import veny.smevente.service.UserService;

/**
 * Controller of User REST interface.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 5.7.2010
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {

    /** Dependency. */
    @Autowired
    private UserService userService;
    /** Dependency. */
    @Autowired
    private EventService eventService;

    /**
     * This is an overridden version of initBinder method of Spring baseCommandController.
     * It is used by Spring to register a custom editor for a String field coming in a HTTP request.
     * This lets you have any data type in your command and not just String.
     *
     * @param binder Spring data binder
     */
    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(null, false) {
            @Override
            public void setAsText(final String text) throws IllegalArgumentException {
                setValue(new Date(NumberUtils.parseNumber(text, Long.class).longValue()));
            }
        });
    }

    // ------------------------------------------------------------- User Stuff

    /**
     * Gets the current logged in user info.
     * <ul>
     * <li>username
     * <li>memberships sorted by 'significance' (without user that is the currently logged in)
     * </ul>
     *
     * @param request HTTP request
     * @return model & view
     */
    @RequestMapping(value = "/info/", method = RequestMethod.GET)
    public ModelAndView getUserInfo(final HttpServletRequest request) {
        final User user = ControllerHelper.getLoggedInUser(request);

        // username
        final ModelAndView modelAndView = new ModelAndView("jsonView");

        modelAndView.addObject("username", user.getUsername());

        // memberships where the user is in
        final List<Membership> membs = userService.getMembershipsByUser(user.getId());
        // we don't need user
        for (Membership m : membs) { m.setUser(null); }
        modelAndView.addObject("memberships", membs);

        return modelAndView;
    }

    /**
     * Changes password of current logged in user.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param oldPassword old password
     * @param newPassword new password
     */
    @RequestMapping(value = "/password/", method = RequestMethod.POST)
    public void changePassword(final HttpServletRequest request, final HttpServletResponse response,
            @RequestParam("old") final String oldPassword,
            @RequestParam("new") final String newPassword) {

        final User user = ControllerHelper.getLoggedInUser(request);
        userService.updateUserPassword(user.getId(), oldPassword, newPassword);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Gets all users in given unit.
     *
     * @param unitId the ID of unit the users belong into
     * @return model & view
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView getUsersInUnit(@RequestParam("unitId") final String unitId) {

        final List<Membership> membs = userService.getUsersInUnit(unitId);

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("memberships", membs);
        return modelAndView;
    }

    /**
     * Stores (creates or updates) a user.<p/>
     * The criterion to decide if create or update is users's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * @param user the user to be created
     * @param unitId unit ID
     * @param role the membership role
     * @param significance the membership significance
     * @return model & view corresponding to newly created user
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView storeUser(
        final User user,
        @RequestParam("unitId") final String unitId,
        @RequestParam("role") final String role,
        @RequestParam("significance") final Integer significance) {

        final Membership.Role etype = Membership.Role.valueOf(role);
        final User created = userService.storeUser(user, unitId, etype, significance);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("user", created);
        return modelAndView;
    }


    /**
     * Deletes a user.
     *
     * @param userId user ID
     */
    @RequestMapping(value = "/{id}/", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable("id") final String userId) {
        userService.deleteUser(userId);
    }


    // ------------------------------------------------------------ Event Stuff

    /**
     * Stores (creates or updates) an event.<p/>
     * The criterion to decide if create or update is patient's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * There is used trick with @see {@link Event#setAuthorId(Object)}.
     *
     * @param event event to be created/updated
     * @return SMS triple as JSON
     */
    @RequestMapping(value = "/event/", method = RequestMethod.POST)
    public ModelAndView storeEvent(final Event event) {

        final Event created = eventService.storeEvent(event);

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("event", created);

        return modelAndView;
    }

//    /**
//     * Creates a new special SMS.
//     *
//     * @param response HTTP response
//     * @param sms SMS to be created
//     * @param authorId author ID
//     */
//    @RequestMapping(value = "/special-sms/", method = RequestMethod.POST)
//    public void createAndSendSpecialSms(final HttpServletResponse response,
//            final Event sms, @RequestParam("authorId") final Long authorId) {
//
//        sms.setAuthorId(authorId);
//        smsService.createAndSendSpecialSms(sms);
//        response.setStatus(HttpServletResponse.SC_OK);
//    }
//
//    /**
//     * Updates given SMS.
//     * @param sms SMS to update
//     * @param authorId author ID
//     * @return SMS triple as JSON
//     */
//    @RequestMapping(value = "/sms/", method = RequestMethod.PUT)
//    public ModelAndView updateSms(final Event sms, @RequestParam("authorId") final Long authorId) {
//
//        sms.setAuthorId(authorId);
//        final Event updated = smsService.updateSms(sms);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("sms", updated);
//
//        return modelAndView;
//    }

    /**
     * Gets list of events for given period.
     * This methods does not distinguish between units to see all terms of a given author.
     *
     * @param request HTTP request
     * @param userId author ID
     * @param from date from
     * @param to date to
     * @return list of <code>Event</code> as JSON
     */
    @RequestMapping(value = "/{userId}/event/from/{from}/to/{to}/", method = RequestMethod.GET)
    public ModelAndView findEvents(
            final HttpServletRequest request,
            @PathVariable("userId") final String userId,
            @PathVariable("from") final Date from,
            @PathVariable("to") final Date to) {


        final List<Event> rslt = eventService.findEvents(userId, from, to);
        // convert all times on event to logged in user time zone
        final TimeZone currentUserTz = ControllerHelper.getLoggedInUserTimezone(request);
        for (final Event event : rslt) {
            event.setStartTime(fromUtc(event.getStartTime(), currentUserTz));
            event.setSent(fromUtc(event.getSent(), currentUserTz));
        }

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("events", rslt);

        return modelAndView;
    }

//    /**
//     * Sends SMS.
//     *
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param smsId SMS ID
//     * @return list of <code>Sms</code> as JSON
//     */
//    @RequestMapping(value = "/sms/{id}/", method = RequestMethod.POST)
//    public ModelAndView sendSms(
//            final HttpServletRequest request, final HttpServletResponse response,
//            @PathVariable("id") final Long smsId) {
//
//        final Event info = smsService.sendSms(smsId);
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("sms", info);
//        return modelAndView;
//    }

    /**
     * Deletes given event.
     *
     * @param eventId event ID
     */
    @RequestMapping(value = "/event/{id}/", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteEvent(@PathVariable("id") final String eventId) {

        eventService.deleteEvent(eventId);
    }

//    /**
//     * Gets SMS detail (it means triplet of Sms, its patient and MHC).
//     *
//     * @param request HTTP request
//     * @param smsId SMS ID
//     * @return <code>Sms</code> as JSON
//     */
//    @RequestMapping(value = "/sms/{id}/text/", method = RequestMethod.GET)
//    public ModelAndView getSmsText(final HttpServletRequest request, @PathVariable("id") final Long smsId) {
//
//        final String text2send = smsService.getSmsText(smsId);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("sms-text", text2send);
//
//        return modelAndView;
//    }
//
//    /**
//     * Gets SMS detail (it means triplet of Sms, its patient and MHC).
//     *
//     * @param request HTTP request
//     * @param smsId SMS ID
//     * @return <code>Sms</code> as JSON
//     */
//    @RequestMapping(value = "/sms/{id}/info/", method = RequestMethod.GET)
//    public ModelAndView getSmsDetail(final HttpServletRequest request, @PathVariable("id") final Long smsId) {
//
//        final Event info = smsService.getById(smsId);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("sms", info);
//
//        return modelAndView;
//    }
//
//    /**
//     * Gets list of SMSs for given period.
//     *
//     * @param request HTTP request
//     * @param userId author ID
//     * @param unitId unit ID
//     * @param from date from
//     * @param to date to
//     * @return list of <code>Sms</code> as JSON
//     */
//    @RequestMapping(value = "/{userId}/unit/{unitId}/from/{from}/to/{to}/", method = RequestMethod.GET)
//    public ModelAndView getSmsStatistics(
//            final HttpServletRequest request,
//            @PathVariable("userId") final Long userId,
//            @PathVariable("unitId") final Long unitId,
//            @PathVariable("from") final long from,
//            @PathVariable("to") final long to) {
//
//        List<Pair<User, Map<String, Integer>>> rslt =
//            smsService.getSmsStatistic(unitId, userId, new Date(from), new Date(to));
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("smsStatistics", rslt);
//
//        return modelAndView;
//    }

    // ----------------------------------------------------------- Helper Stuff

    /**
     * Calculate time zone offset according to user's locale.
     * @param date date in UTC
     * @param to target time zone
     * @return date recalculated from UTC to given time zone.
     */
    private Date fromUtc(final Date date, final TimeZone to) {
        if (null == date) { return null; }

        final int tzOffset = (to.getOffset(date.getTime()));
        return new Date(date.getTime() + tzOffset);
    }

}
