package veny.smevente.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import veny.smevente.client.utils.Pair;
import veny.smevente.misc.AppContext;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.User;
import veny.smevente.service.EventService;
import veny.smevente.service.UnitService;
import veny.smevente.service.UserService;

import com.google.common.base.Strings;

/**
 * Controller of Unit REST interface.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
@Controller
@RequestMapping(value = "/unit")
public class UnitController {

    /** Dependency. */
    @Autowired
    private UnitService unitService;
    /** Dependency. */
    @Autowired
    private UserService userService;
    /** Dependency. */
    @Autowired
    private EventService eventService;
    /** Dependency. */
    @Autowired
    private AppContext appCtxHelper;

    // ------------------------------------------------------------- Unit Stuff

    /**
     * Gets unit info.
     *
     * @param unitId unit ID
     * @return model & view
     */
    @RequestMapping(value = "/{id}/info/", method = RequestMethod.GET)
    public ModelAndView getUnitInfo(@PathVariable("id") final String unitId) {

        final ModelAndView modelAndView = new ModelAndView("jsonView");

        // other users if the logged-in user is ADMIN in given unit
        final User user = appCtxHelper.getLoggedInUser();
        final List<User> other = userService.getOtherUsersInUnit(unitId, user.getId());
        modelAndView.addObject("unitMembers", other);

        // patients
        final List<Customer> patients = unitService.getPatientsByUnit(unitId);
        modelAndView.addObject("patients", patients);

        // procedures (type=null => all)
        final List<Procedure> procedures = unitService.getProceduresByUnit(unitId, null);
        modelAndView.addObject("procedures", procedures);

        return modelAndView;
    }

    // --------------------------------------------------------- Customer Stuff

    /**
     * Stores (creates or updates) a customer.<p/>
     * The criterion to decide if create or update is customer's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * There is used trick with @see {@link Customer#setUnitId(Object)}.
     *
     * @param customer customer
     * @return model & view
     */
    @RequestMapping(value = "/customer/", method = RequestMethod.POST)
    public ModelAndView storePatient(final Customer customer) {

        final Customer created = unitService.storePatient(customer);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("customer", created);
        return modelAndView;
    }

//    /**
//     * Get patient by ID.
//     *
//     * @param patientId patient ID
//     * @return model & view
//     */
//    @RequestMapping(value = "/patient/{id}/", method = RequestMethod.GET)
//    public ModelAndView getPatient(@PathVariable("id") final Long patientId) {
//        ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("patient", unitService.getPatientById(patientId));
//        return modelAndView;
//    }

    /**
     * Finds customers in given unit according to name and/or birth number and/or phone number.
     *
     * @param request HTTP request
     * @param unitId ID to search in
     * @param name name to search
     * @param phoneNumber phone number to search
     * @param birthNumber birth number to search
     * @return model & view
     */
    @RequestMapping(value = "/{id}/customer/", method = RequestMethod.GET)
    public ModelAndView findCustomers(
            final HttpServletRequest request,
            @PathVariable("id") final String unitId,
            @RequestParam("name") final String name,
            @RequestParam("phoneNumber") final String phoneNumber,
            @RequestParam("birthNumber") final String birthNumber) {

        final String n = (Strings.isNullOrEmpty(name) ? null : name.trim());
        final String pn = (Strings.isNullOrEmpty(phoneNumber) ? null : phoneNumber.trim());
        final String bn = (Strings.isNullOrEmpty(birthNumber) ? null : birthNumber.trim());
        final List<Customer> customers = unitService.findPatients(unitId, n, pn, bn);

        // we don't need the unit
        for (Customer c : customers) { c.setUnit(null); }

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("customers", customers);
        return modelAndView;
    }

    /**
     * Deletes a customer.
     *
     * @param customerId customer ID
     */
    @RequestMapping(value = "/customer/{id}/", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePatient(@PathVariable("id") final String customerId) {
        unitService.deletePatient(customerId);
    }

    /**
     * Gets list of all events for given patient ordered by Start Time.
     *
     * @param customerId ID of the customer
     * @return list of <code>Event</code> as JSON
     */
    @RequestMapping(value = "/customer/{id}/history/", method = RequestMethod.GET)
    public ModelAndView getCustomerHistory(@PathVariable("id") final String customerId) {

        final Pair<Customer, List<Event>> rslt = eventService.findEventsByCustomer(customerId);

        // we don't need the customer, it is key A
        for (Event e : rslt.getB()) { e.setCustomer(null); }

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("history", rslt);

        return modelAndView;
    }


    // -------------------------------------------------------- Procedure Stuff

    /**
     * Stores (creates or updates) a procedure.<p/>
     * The criterion to decide if create or update is procedure's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * There is used trick with @see {@link Procedure#setUnitId(Object)}.
     *
     * @param procedure procedure to store
     * @return model & view
     */
    @RequestMapping(value = "/procedure/", method = RequestMethod.POST)
    public ModelAndView storeProcedure(final Procedure procedure) {

        final Procedure created = unitService.storeProcedure(procedure);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("procedure", created);
        return modelAndView;
    }

    /**
     * Gets all procedures of given type in given unit.
     *
     * @param request HTTP request
     * @param unitId unit ID
     * @param procedureType the type of procedure
     * @return model & view
     */
    @RequestMapping(value = "/{id}/procedure/{type}", method = RequestMethod.GET)
    public ModelAndView getProcedure(
            final HttpServletRequest request,
            @PathVariable("id") final String unitId,
            @PathVariable("type") final String procedureType) {

        if (Strings.isNullOrEmpty(procedureType)) { throw new IllegalArgumentException("type cannot be blank"); }
        final Event.Type type = Event.Type.valueOf(procedureType.trim());

        final List<Procedure> procedures = unitService.getProceduresByUnit(unitId, type);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("procedures", procedures);
        return modelAndView;
    }

    /**
     * Deletes given procedure.
     *
     * @param procedureId procedure ID
     */
    @RequestMapping(value = "/procedure/{id}/", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteProcedure(@PathVariable("id") final String procedureId) {
        unitService.deleteProcedure(procedureId);
    }

    // ----------------------------------------------------------- Helper Stuff

}
