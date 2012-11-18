package veny.smevente.server;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import veny.smevente.model.User;
import veny.smevente.security.AuthenticationSuccessHandlerImpl;

/**
 * Controller to initialize the datastore.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
@Controller
@RequestMapping(value = "/data")
public class DataController {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(DataController.class.getName());

//    /** Dependency. */
//    @Autowired
//    private UserService userService;
//    /** Dependency. */
//    @Autowired
//    private SmsGatewayService smsService;
//    /** Dependency. */
//    @Autowired
//    private UnitService unitService;
//    /** Dependency. */
//    @Autowired
//    private PatientDaoGae patientDao;
//    /** Dependency. */
//    @Autowired
//    private GaeCache gaeCache;
//
//    /**
//     * This is an overridden version of initBinder method of Spring baseCommandController.
//     * It is used by Spring to register a custom editor for a String field coming in a HTTP request.
//     * This lets you have any data type in your command and not just String.
//     * @param binder Spring data binder
//     */
//    @InitBinder
//    public void initBinder(final WebDataBinder binder) {
//        binder.registerCustomEditor(Map.class, new CustomDateEditor(null, false) {
//            @Override
//            public void setAsText(final String text) throws IllegalArgumentException {
//                setValue(TextUtils.stringToMap(text));
//            }
//        });
//    }
//
//    // ------------------------------------------------------------- User Stuff
//
//    /**
//     * Stores the given user in datastore.
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param username username
//     * @param password password
//     * @param fullname full name
//     * @param root flag of root user
//     * @return model & view
//     */
//    @RequestMapping(value = "/user/", method = RequestMethod.POST)
//    public ModelAndView createUser(
//        final HttpServletRequest request,
//        final HttpServletResponse response,
//        @RequestParam("username") final String username,
//        @RequestParam("password") final String password,
//        @RequestParam("fullname") final String fullname,
//        @RequestParam("root") final boolean root) {
//
//        final User created = userService.createUser(username, password, fullname, root);
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("user", created);
//        return modelAndView;
//    }
//
//    /**
//     * Gets all users in datastore.
//     * @param request HTTP request
//     * @return model & view
//     */
//    @RequestMapping(value = "/user/", method = RequestMethod.GET)
//    public ModelAndView getUsers(final HttpServletRequest request) {
//        @SuppressWarnings("deprecation")
//        final Collection<User> users = userService.getAllUsers();
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("users", users);
//        return modelAndView;
//    }
//
//    // ------------------------------------------------------------- Unit Stuff
//
//    /**
//     * Creates a new unit.
//     * @param request HTTP request
//     * @param unit unit to be created
//     * @return model & view
//     */
//    @RequestMapping(value = "/unit/", method = RequestMethod.POST)
//    public ModelAndView createUnit(final HttpServletRequest request, final UnitDto unit) {
//
//        final UnitDto created = unitService.createUnit(unit);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("unit", created);
//        return modelAndView;
//    }
//
//    // ------------------------------------------------------- Membership Stuff
//
//    /**
//     * Creates a new membership.
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param unitId ID of unit
//     * @param userId ID of user
//     * @param type membership type
//     * @param significance significance of the membership to other memberships of a user
//     */
//    @RequestMapping(value = "/membership/", method = RequestMethod.POST)
//    public void createMembership(
//        final HttpServletRequest request,
//        final HttpServletResponse response,
//        @RequestParam("userId") final Long userId,
//        @RequestParam("unitId") final Long unitId,
//        @RequestParam("type") final Integer type,
//        @RequestParam("significance") final Integer significance) {
//
//        final MembershipDto.Type etype = MembershipDto.Type.values()[type.intValue()];
//        userService.createMembership(unitId, userId, etype, significance.intValue());
//        response.setStatus(200);
//    }
//
//    // -------------------------------------------------------------- SMS Stuff
//
//    /**
//     * Sends a SMS.
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param unitId unit ID
//     */
//    @RequestMapping(value = "/sms/unit/{unitId}/", method = RequestMethod.POST)
//    public void send2veny(
//            final HttpServletRequest request, final HttpServletResponse response,
//            @PathVariable("unitId") final Long unitId) {
//
//        final UnitDto unit = unitService.getById(unitId);
//        try {
//            smsService.send("606146177", "ABC", unit.getMetadata());
//            response.setStatus(200);
//        } catch (SmsException e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // ------------------------------------------------------------- Unit Stuff
//
//    /**
//     * Creates new Medical Help Category.
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param mhc the medical help category
//     * @param unitId unit ID
//     */
//    @RequestMapping(value = "/mhc/", method = RequestMethod.POST)
//    public void createMedicalHelpCategory(
//            final HttpServletRequest request, final HttpServletResponse response,
//            final MedicalHelpCategoryDto mhc, final Long unitId) {
//
//        mhc.setUnit(new UnitDto());
//        mhc.getUnit().setId(unitId);
//        unitService.createMedicalHelpCategory(mhc);
//        response.setStatus(200);
//    }
//
//    // ---------------------------------------------------------- Special Stuff
//
//    /**
//     * Clears the cache.
//     * @param response the HTTP response
//     */
//    @RequestMapping(value = "/cache/clear", method = RequestMethod.GET)
//    public void clearCache(final HttpServletResponse response) {
//        gaeCache.clear();
//        LOG.info("cache cleared");
//    }
//
//    /**
//     * Deletes patients from given unit.
//     * @param response HTTP response
//     * @param unitId unit ID
//     */
//    @RequestMapping(value = "/unit/{unitId}/patient/_del", method = RequestMethod.GET)
//    public void deletePatients(
//            final HttpServletResponse response,
//            @PathVariable("unitId") final Long unitId) {
//
//        // delete patients in given unit
//        List<Patient> patients = patientDao.findBy("unitId", unitId, null);
//        LOG.info("found " + patients.size() + " patients(s) by unit");
//        for (Patient p : patients) {
//            patientDao.remove(p.getId());
//        }
//        LOG.info("patients deleted");
//    }

    // ----------------------------------------------------------- Helper Stuff

    /**
     * Gets the logged in user stored in session.
     * @param request HTTP request
     * @return current logged in user
     */
    public static User getLoggedInUser(final HttpServletRequest request) {
        final User user = (User)
                request.getSession(false).getAttribute(AuthenticationSuccessHandlerImpl.USER_SESSION_KEY);
        if (null == user) {
            throw new IllegalStateException("user not found in session, no authentication?");
        }
        return user;
    }

    /**
     * Asserts if the logged in user stored in session is an root.
     * @param request HTTP request
     */
    public static void assertRoot(final HttpServletRequest request) {
        final User user = getLoggedInUser(request);
        if (!user.isRoot()) {
            LOG.error("unauthorized data change (NOT root), username=" + user.getUsername());
            throw new IllegalStateException("non-privileged access (NOT root)");
        }
    }


}
