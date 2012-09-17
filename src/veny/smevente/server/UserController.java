package veny.smevente.server;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import veny.smevente.model.Membership;
import veny.smevente.model.Unit;
import veny.smevente.model.User;
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
//    @Autowired
//    private SmsService smsService;

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
     * <li>units sorted by 'significance'
     * </ul>
     *
     * @param request HTTP request
     * @return model & view
     */
    @RequestMapping(value = "/info/", method = RequestMethod.GET)
    public ModelAndView getUserInfo(final HttpServletRequest request) {
        final User user = DataController.getLoggedInUser(request);

        // username
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("username", user.getUsername());

        // units where the user is member in
//        final List<Unit> units = userService.getUnitsOfUser(user.getId());
List<Membership> units = userService.findMembershipsByUser(user.getId());
        modelAndView.addObject("units", units);

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
    @RequestMapping(value = "/password/", method = RequestMethod.PUT)
    public void changePassword(final HttpServletRequest request, final HttpServletResponse response,
            @RequestParam("old") final String oldPassword,
            @RequestParam("new") final String newPassword) {

        final User user = DataController.getLoggedInUser(request);
        userService.updateUserPassword(user.getId(), oldPassword, newPassword);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Gets all users.
     *
     * @param request HTTP request
     * @param unitId the ID of unit the searched users must belong into
     * @param userName user name to search
     * @param fullName full name to search
     * @return model & view
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView findUsers(
            final HttpServletRequest request,
            @RequestParam("unitId") final Long unitId,
            @RequestParam("username") final String userName,
            @RequestParam("fullname") final String fullName) {

        String un = (null == userName || 0 == userName.trim().length() ? null : userName.trim());
        String fn = (null == fullName || 0 == fullName.trim().length() ? null : fullName.trim());
        List<User> users = userService.findUsers(unitId, un, fn);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    /**
     * Creates a new user.
     * @param request HTTP request
     * @param response HTTP response
     * @param user the user to be created
     * @param unitId unit ID
     * @param type the membership type
     * @param significance the membership significance
     * @return model & view corresponding to newly created user
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView createUser(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final User user,
        @RequestParam("unitId") final Long unitId,
        @RequestParam("type") final Integer type,
        @RequestParam("significance") final Integer significance) {

        // as first encode the password
        user.setPassword(userService.encodePassword(user.getPassword()));
        Membership.Role etype = Membership.Role.values()[type.intValue()];
        final User created = userService.createUser(user, unitId, etype, significance);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("user", created);
        return modelAndView;
    }

//    /**
//     * Updates an existing user.
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param user the user to be updated
//     * @param unitId unit ID
//     * @param type the membership type
//     * @param significance the membership significance
//     */
//    @RequestMapping(value = "/", method = RequestMethod.PUT)
//    public void updateUser(
//        final HttpServletRequest request,
//        final HttpServletResponse response,
//        final User user,
//        @RequestParam("unitId") final Long unitId,
//        @RequestParam("type") final Integer type,
//        @RequestParam("significance") final Integer significance) {
//
//        // as first encode the password if it should be also updated
//        if (!User.DO_NOT_CHANGE_PASSWORD.equals(user.getPassword())) {
//            user.setPassword(userService.encodePassword(user.getPassword()));
//        }
//        Membership.Role etype = Membership.Role.values()[type.intValue()];
//        userService.updateUser(user, unitId, etype, significance);
//        response.setStatus(200);
//    }
//
//    /**
//     * Deletes a user.
//     * @param response HTTP response
//     * @param userId user ID
//     */
//    @RequestMapping(value = "/{id}/", method = RequestMethod.DELETE)
//    public void deleteUser(final HttpServletResponse response, @PathVariable("id") final Long userId) {
//        userService.deleteUser(userId);
//        response.setStatus(200);
//    }
//    // -------------------------------------------------------------- SMS Stuff
//
//    /**
//     * Creates a new SMS.
//     *
//     * @param sms SMS to be created
//     * @param authorId author ID
//     * @return SMS triple as JSON
//     */
//    @RequestMapping(value = "/sms/", method = RequestMethod.POST)
//    public ModelAndView createSms(final Event sms, @RequestParam("authorId") final Long authorId) {
//
//        sms.setAuthorId(authorId);
//        final Event created = smsService.createSms(sms);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("sms", created);
//
//        return modelAndView;
//    }
//
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
//
//    /**
//     * Gets list of SMSs for given period.
//     * This methods does not distinguish between units to see all terms of a given author.
//     *
//     * @param request HTTP request
//     * @param userId author ID
//     * @param from date from
//     * @param to date to
//     * @return list of <code>Sms</code> as JSON
//     */
//    @RequestMapping(value = "/{userId}/sms/from/{from}/to/{to}/", method = RequestMethod.GET)
//    public ModelAndView findSms(
//            final HttpServletRequest request,
//            @PathVariable("userId") final Long userId,
//            @PathVariable("from") final Date from,
//            @PathVariable("to") final Date to) {
//
//        final List<Event> rslt = smsService.findSms(userId, from, to);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("smss", rslt);
//
//        return modelAndView;
//    }
//
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
//
//    /**
//     * Deletes SMS.
//     *
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param smsId SMS ID
//     */
//    @RequestMapping(value = "/sms/{id}/", method = RequestMethod.DELETE)
//    public void deleteSms(
//            final HttpServletRequest request, final HttpServletResponse response,
//            @PathVariable("id") final Long smsId) {
//
//        smsService.deleteSms(smsId);
//        response.setStatus(HttpServletResponse.SC_OK);
//    }
//
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
