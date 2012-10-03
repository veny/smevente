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

import veny.smevente.model.Patient;
import veny.smevente.model.Procedure;
import veny.smevente.model.User;
import veny.smevente.service.UnitService;
import veny.smevente.service.UserService;

import com.google.gwt.thirdparty.guava.common.base.Strings;

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
        final User user = DataController.getLoggedInUser(request);
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
    public ModelAndView storePatient(final Patient patient/*, @RequestParam("unitId") final String unitId*/) {

        final Patient created = unitService.storePatient(patient);
        final ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("patient", created);
        return modelAndView;
    }

//    /**
//     * Get patients by ID.
//     *
//     * @param patientId patient ID
//     * @return model & view
//     */
//    @RequestMapping(value = "/patient/{id}/", method = RequestMethod.GET)
//    public ModelAndView getPatients(@PathVariable("id") final Long patientId) {
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

        ModelAndView modelAndView = new ModelAndView("jsonView");
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
//
//    // ---------------------------------------------- MedicalHelpCategory Stuff
//
//    /**
//     * Creates a new category.
//     * @param request HTTP request
//     * @param response HTTP response
//     * @param mhc category
//     * @param unitId unit ID
//     * @return model & view
//     */
//    @RequestMapping(value = "/mhc/", method = RequestMethod.POST)
//    public ModelAndView createMedicalHelpCategory(
//        final HttpServletRequest request,
//        final HttpServletResponse response,
//        final MedicalHelpCategoryDto mhc,
//        @RequestParam("unitId") final Long unitId) {
//
//        mhc.setUnitId(unitId);
//        final MedicalHelpCategoryDto created = unitService.createMedicalHelpCategory(mhc);
//        final ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("medicalHelpCategory", created);
//
//        return modelAndView;
//    }
//
//    /**
//     * Updates given Medical Help Category.
//     * @param response HTTP response
//     * @param mhc medical help category
//     * @param unitId unit ID
//     */
//    @RequestMapping(value = "/mhc/", method = RequestMethod.PUT)
//    public void updateMedicalHelpCategory(
//        final HttpServletResponse response,
//        final MedicalHelpCategoryDto mhc,
//        @RequestParam("unitId") final Long unitId) {
//
//        mhc.setUnitId(unitId);
//        unitService.updateMedicalHelpCategory(mhc);
//        response.setStatus(200);
//    }
//
//    /**
//     * Gets all Medical Help Categories.
//     * @param request HTTP request
//     * @param unitId unit ID
//     * @param categoryType the type of category
//     * @return model & view
//     */
//    @RequestMapping(value = "/{id}/mhc/{type}", method = RequestMethod.GET)
//    public ModelAndView getMedicalHelpCategory(
//            final HttpServletRequest request,
//            @PathVariable("id") final Long unitId,
//            @PathVariable("type") final Short categoryType) {
//
//        Collection<MedicalHelpCategoryDto> mhcs = unitService.getMedicalHelpCategoriesByUnit(unitId, categoryType);
//        ModelAndView modelAndView = new ModelAndView("jsonView");
//        modelAndView.addObject("medicalHelpCategories", mhcs);
//        return modelAndView;
//    }
//
//    /**
//     * Deletes a category.
//     * @param response HTTP response
//     * @param mhcId category ID
//     */
//    @RequestMapping(value = "/mhc/{id}/", method = RequestMethod.DELETE)
//    public void deleteMedicalHelpCategory(
//            final HttpServletResponse response,
//            @PathVariable("id") final Long mhcId) {
//        unitService.deleteMedicalHelpCategory(mhcId);
//        response.setStatus(200);
//    }

    // ----------------------------------------------------------- Helper Stuff

}
