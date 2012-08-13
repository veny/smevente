package veny.smevente.server;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import veny.smevente.service.SmsService;

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
    private SmsService smsService;

    /**
     * SMS cron task.
     *
     * @param request HTTP request
     * @param response HTTP response
     */
    @RequestMapping(value = "/sms/")
    public void smsCronTask(final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("cron task: bulk SMS sending, clientAddr=" + request.getRemoteHost());
        smsService.bulkSend();
        response.setStatus(200);
    }

    // ----------------------------------------------------------- Helper Stuff

}
