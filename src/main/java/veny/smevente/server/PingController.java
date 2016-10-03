package veny.smevente.server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import veny.smevente.misc.AppContext;
import veny.smevente.model.User;

/**
 * Ping/pong controller to check if the application is alive.
 * <p>
 * You can check it like this:
 * <pre>
 * curl -i http://&lt;domain&rt;/rest/ping/
 * e.g. curl -i http://localhost:8888/rest/ping/
 * </pre>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 10.7.2010
 */
@Controller
public class PingController {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(PingController.class.getName());

    /** Dependency. */
    @Autowired
    private AppContext appCtxHelper;

    /**
     * Get the <i>pong</i> response.
     * @return JSON representation of username string
     */
    @RequestMapping(value = "/ping/")
    public ModelAndView ping() {

        final User user = appCtxHelper.getLoggedInUser();
        LOG.info("ping, user=" + user.getUsername());

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("jsonView");
        modelAndView.addObject("pong", user.getUsername());
        return modelAndView;
    }

    /**
     * Get a overview for monitoring.
     * @param request HTTP request
     * @return JSON representation of overview data
     */
    @RequestMapping(value = "/monitor/")
    public ModelAndView monitor(final HttpServletRequest request) {
        LOG.info("monitor, ip=" + request.getLocalAddr());
        final ModelAndView modelAndView = new ModelAndView();
        final Map<String, String> data = new HashMap<String, String>();
        data.put("status", "OK");
        modelAndView.setViewName("jsonView");
        modelAndView.addObject("monitor", data);
        return modelAndView;
    }
}
