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

import com.google.common.base.Strings;

import veny.smevente.model.Event;
import veny.smevente.model.Patient;
import veny.smevente.model.Procedure;
import veny.smevente.model.User;
import veny.smevente.service.UnitService;
import veny.smevente.service.UserService;

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
//    /** Dependency. */
//    @Autowired
//    private SmsService smsService;
//
    // ------------------------------------------------------------- Unit Stuff

    /**
     * Gets unit info.
     *
     * @param request HTTP request
     * @param unitId unit ID
     * @return model & view
     */
    @RequestMapping(value = "/{id}/info/", method = RequestMethod.GET)
    public ModelAndView getUnitInfo(
            final HttpServletRequest request,
            @PathVariable("id") final String unitId) {

        final ModelAndView modelAndView = new ModelAndView("jsonView");

        // other users if the logged-in user is ADMIN in given unit
        final User user = ControllerHelper.getLoggedInUser(request);
        final List<User> other = userService.getOtherUsersInUnit(unitId, user.getId());
        modelAndView.addObject("unitMembers", other);

        // patients
        final List<Patient> patients = unitService.getPatientsByUnit(unitId);
        modelAndView.addObject("patients", patients);

        // procedures (type=null => all)
        final List<Procedure> procedures = unitService.getProceduresByUnit(unitId, null);
        modelAndView.addObject("procedures", procedures);

        return modelAndView;
    }

    // ---------------------------------------------------------- Patient Stuff

    /**
     * Stores (creates or updates) a patient.<p/>
     * The criterion to decide if create or update is patient's ID value:
     * 'create' if ID is <i>null</i>, otherwise 'update'.
     *
     * There is used trick with @see {@link Patient#setUnitId(Object)}.
     *
     * @param patient patient
     * @return model & view
     */
    @RequestMapping(value = "/patient/", method = RequestMethod.POST)
    public ModelAndView storePatient(final Patient patient) {

        final Patient created = unitService.storePatient(patient);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("patient", created);
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
     * Finds patients in given unit according to name and/or birth number and/or phone number.
     *
     * @param request HTTP request
     * @param unitId ID to search in
     * @param name name to search
     * @param phoneNumber phone number to search
     * @param birthNumber birth number to search
     * @return model & view
     */
    @RequestMapping(value = "/{id}/patient/", method = RequestMethod.GET)
    public ModelAndView findPatients(
            final HttpServletRequest request,
            @PathVariable("id") final String unitId,
            @RequestParam("name") final String name,
            @RequestParam("phoneNumber") final String phoneNumber,
            @RequestParam("birthNumber") final String birthNumber) {

        final String n = (Strings.isNullOrEmpty(name) ? null : name.trim());
        final String pn = (Strings.isNullOrEmpty(phoneNumber) ? null : phoneNumber.trim());
        final String bn = (Strings.isNullOrEmpty(birthNumber) ? null : birthNumber.trim());
        final List<Patient> patients = unitService.findPatients(unitId, n, pn, bn);

        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("patients", patients);
        return modelAndView;
    }

    /**
     * Deletes a patient.
     *
     * @param patientId patient ID
     */
    @RequestMapping(value = "/patient/{id}/", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePatient(@PathVariable("id") final String patientId) {
        unitService.deletePatient(patientId);
    }

//    /**
//     * Gets list of all sent SMSs for given patient ordered by MH Start Time.
//     *
//     * @param patientId ID of the patient
//     * @return list of <code>Sms</code> as JSON
//     */
//    @RequestMapping(value = "/patient/{id}/history/", method = RequestMethod.GET)
//    public ModelAndView getPatientHistory(@PathVariable("id") final Long patientId) {
//
//        final Pair<PatientDto, List<SmsDto>> rslt = smsService.findSmsByPatient(patientId);
//
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("history", rslt);
//
//        return modelAndView;
//    }

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
