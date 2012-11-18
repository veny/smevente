package veny.smevente.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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

    /**
     * Get the <i>pong</i> response.
     * @param request HTTP request
     * @param response HTTP response
     * @return JSON representation of username string
     */
    @RequestMapping(value = "/ping/")
    public ModelAndView ping(final HttpServletRequest request, final HttpServletResponse response) {

        final User user = DataController.getLoggedInUser(request);
        LOG.info("ping, user=" + user.getUsername());

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("jsonView");
        modelAndView.addObject("pong", user.getUsername());
        return modelAndView;
    }

}
