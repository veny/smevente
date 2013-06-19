package veny.smevente.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import veny.smevente.service.EventService;

/**
 * Controller of Cron Task HTTP request.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 6.8.2010
 */
@Controller
@RequestMapping(value = "/cron/task")
public class CronTaskController {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(CronTaskController.class.getName());

    /** Dependency. */
    @Autowired
    private EventService eventService;

    /**
     * Event cron task.
     *
     * @param request HTTP request
     * @param response HTTP response
     */
    @RequestMapping(value = "/sms/", method = RequestMethod.GET)
    public void sendEventCronTask(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("cron task: bulk event sending, clientAddr=" + request.getRemoteHost());
        eventService.bulkSend();
        response.setStatus(200);
    }

    // ----------------------------------------------------------- Helper Stuff

}
