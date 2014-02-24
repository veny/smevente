package veny.smevente.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import veny.smevente.dao.orientdb.Schema;
import veny.smevente.service.EventService;

/**
 * Controller of Cron Task HTTP request.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 6.8.2010
 */
@Controller
@RequestMapping(value = "/sac")
public class CronTaskController {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(CronTaskController.class.getName());

    /** Dependency. */
    @Autowired
    private EventService eventService;

    /** Dependency. */
    @Autowired
    private Schema schema;

    /**
     * Event cron task.
     *
     * @param request HTTP request
     * @param response HTTP response
     */
    @RequestMapping(value = "/cron/sms/", method = RequestMethod.GET)
    public void sendEventCronTask(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("cron task: bulk event sending, clientAddr=" + request.getRemoteHost());
        eventService.bulkSend();
        response.setStatus(200);
    }


    /**
     * Creates new DB schema.
     *
     * @param request HTTP request
     * @param response HTTP response
     */
    @RequestMapping(value = "/schema/create/", method = RequestMethod.GET)
    public void schemaCreate(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("schema create, clientAddr=" + request.getRemoteHost());
        schema.create();
        response.setStatus(200);
    }

    /**
     * Initializes schema with a basic data set.
     *
     * @param request HTTP request
     * @param response HTTP response
     */
    @RequestMapping(value = "/data/sample/", method = RequestMethod.GET)
    public void sampleData(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("sample data, clientAddr=" + request.getRemoteHost());
        schema.sampleData();
        response.setStatus(200);
    }
    // ----------------------------------------------------------- Helper Stuff

}
