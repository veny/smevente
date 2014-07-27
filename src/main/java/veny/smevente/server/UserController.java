package veny.smevente.server;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import veny.smevente.misc.AppContext;
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
    /** Dependency. */
    @Autowired
    private AppContext appCtx;

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
                final Calendar cal = DatatypeConverter.parseDateTime(text);
                setValue(cal.getTime());
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
     * @return model & view
     */
    @RequestMapping(value = "/info/", method = RequestMethod.GET)
    public ModelAndView getUserInfo() {
        final User user = appCtx.getLoggedInUser();

        // username
        final ModelAndView modelAndView = new ModelAndView("jsonView");

        modelAndView.addObject("username", user.getUsername());

        // memberships where the user is in
        final List<Membership> membs = userService.getMembershipsByUser(user.getId());
        // we don't need the user
        for (Membership m : membs) { m.setUser(null); }
        modelAndView.addObject("memberships", membs);

        return modelAndView;
    }

    /**
     * Changes password of current logged in user.
     *
     * @param response HTTP response
     * @param oldPassword old password
     * @param newPassword new password
     */
    @RequestMapping(value = "/password/", method = RequestMethod.POST)
    public void changePassword(final HttpServletResponse response,
            @RequestParam("old") final String oldPassword,
            @RequestParam("new") final String newPassword) {

        final User user = appCtx.getLoggedInUser();
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

        final Date startTime = appCtx.fromUserViewToUtc(event.getStartTime());
        event.setStartTime(startTime);
        final Event created = eventService.storeEvent(event);
        created.setStartTime(appCtx.fromUtcToUserView(created.getStartTime()));
        created.setSent(appCtx.fromUtcToUserView(created.getSent()));

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("event", created);

        return modelAndView;
    }

    /**
     * Creates and immediately sends a new special message.
     *
     * @param response HTTP response
     * @param event event to be created and sent
     */
    @RequestMapping(value = "/special-msg/", method = RequestMethod.POST)
    public void createAndSendSpecialEvent(final HttpServletResponse response, final Event event) {
        eventService.createAndSendSpecialEvent(event);
        response.setStatus(HttpServletResponse.SC_OK);
    }

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
     * @param userId author ID
     * @param fromInUserTz date from in user's TZ
     * @param toInUserTz date to in user's TZ
     * @return list of <code>Event</code> as JSON
     */
    @RequestMapping(value = "/{userId}/event/from/{from}/to/{to}/", method = RequestMethod.GET)
    public ModelAndView findEvents(
            @PathVariable("userId") final String userId,
            @PathVariable("from") final Date fromInUserTz,
            @PathVariable("to") final Date toInUserTz) {

        final Date from = appCtx.fromUserViewToUtc(fromInUserTz);
        final Date to = appCtx.fromUserViewToUtc(toInUserTz);
        final List<Event> rslt = eventService.findEvents(userId, from, to);
        // 1. convert all times on event to logged in user time zone
        // 2. delete unit on customer/procedure; we don't need them
        for (final Event event : rslt) {
            event.setStartTime(appCtx.fromUtcToUserView(event.getStartTime()));
            event.setSent(appCtx.fromUtcToUserView(event.getSent()));
            event.getCustomer().setUnit(null);
            event.getProcedure().setUnit(null);
        }

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("events", rslt);

        return modelAndView;
    }

    /**
     * Sends SMS for given event.
     *
     * @param eventId event ID
     * @return the event (as JSON) updated after sending
     */
    @RequestMapping(value = "/sms/{id}/", method = RequestMethod.POST)
    public ModelAndView sendSms(@PathVariable("id") final String eventId) {

        final Event event2send = eventService.getEvent(eventId);
        final Event rslt = eventService.send(event2send);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("sms", rslt);
        return modelAndView;
    }

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

}
