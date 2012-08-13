package veny.smevente.server;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import veny.smevente.dao.jpa.gae.MedicalHelpCategoryDaoGae;
import veny.smevente.dao.jpa.gae.PatientDaoGae;
import veny.smevente.dao.jpa.gae.UserDaoGae;
import veny.smevente.model.gae.MedicalHelpCategory;
import veny.smevente.model.gae.Patient;
import veny.smevente.model.gae.User;

/**
 * Controller to upgrade entities of domain model.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.5.2011
 */
@Controller
@RequestMapping(value = "/upgrade")
public class UpgradeController {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(UpgradeController.class.getName());

    /** Dependency. */
    @Autowired
    private UserDaoGae userDao;
    /** Dependency. */
    @Autowired
    private PatientDaoGae patientDao;
    /** Dependency. */
    @Autowired
    private MedicalHelpCategoryDaoGae mhcDao;

    // ---------------------------------------------------------- Version 1_2_2

    /**
     * Updates <code>User</code> to version 1.
     * Sets <code>deleted</code> to <i>false</i>.
     * @param request HTTP request
     * @param response HTTP response
     *        (because of problem "Could not resolve view with name 'upgrade/user/v/1' in servlet ...")
     * @since 10.5.2011
     */
    @RequestMapping(value = "/user/v/1", method = RequestMethod.GET)
    public void userV1(final HttpServletRequest request, final HttpServletResponse response) {
        DataController.assertRoot(request);

        final List<User> all = userDao.getAll(true);
        for (User entity : all) {
            entity.setVersion(1);
            entity.setDeleted(false);
            userDao.persist(entity);
            LOG.info("entity updated, id=" + entity.getId());
        }
        LOG.info("updated " + all.size() + " entities");
    }

    /**
     * Updates MHC to version 2.
     * Sets <code>deleted</code> attribute according to status.
     * @param request HTTP request
     * @param response HTTP response
     *        (because of problem "Could not resolve view with name 'upgrade/user/v/1' in servlet ...")
     * @since 10.5.2011
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/mhc/v/2", method = RequestMethod.GET)
    public void mhcV2(final HttpServletRequest request, final HttpServletResponse response) {
        DataController.assertRoot(request);

        final List<MedicalHelpCategory> all = mhcDao.getAll(true);
        for (MedicalHelpCategory entity : all) {
            entity.setVersion(2);
            entity.setDeleted((entity.getStatus() & 16) > 0); // 16 was a bit mask of 'deleted'
            mhcDao.persist(entity);
            LOG.info("entity updated, id=" + entity.getId());
        }
        LOG.info("updated " + all.size() + " entities");
    }

    /**
     * Updates <code>Patient</code> to version 1.
     * Sets <code>deleted</code> attribute according to status.
     * @param request HTTP request
     * @param response HTTP response
     *        (because of problem "Could not resolve view with name 'upgrade/user/v/1' in servlet ...")
     * @since 10.5.2011
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/patient/v/1", method = RequestMethod.GET)
    public void patientV1(final HttpServletRequest request, final HttpServletResponse response) {
        DataController.assertRoot(request);

//        final List<Patient> all = patientDao.findLesserVersion(1);
        final List<Patient> all = patientDao.getAll(true);
        LOG.finer("found " + all.size() + " entities");
        int cnt = 0;
        for (Patient entity : all) {
            if (1 == entity.getVersion()) { continue; }
            entity.setVersion(1);
            entity.setDeleted((entity.getStatus() & 16) > 0); // 16 was a bit mask of 'deleted'
            patientDao.persist(entity);
            LOG.info("entity updated, id=" + entity.getId() + ", cnt=" + (++cnt));
        }
        LOG.info("updated " + all.size() + " entities");
    }

    // ----------------------------------------------------------- Helper Stuff

}
