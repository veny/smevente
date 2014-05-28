package veny.smevente.server;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A helper to process some application operations when the application context is loaded.
 * Following operations will be performed<ul>
 * <li>SSL parameters are configured
 * </ul>
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 * {@link http://anydoby.com/jblog/article.htm?id=33}
 */
public class ApplicationContextHelper implements ApplicationContextAware {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ApplicationContextHelper.class.getName());

    /** {@inheritDoc} */
    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
//        final WebApplicationContext wctx = (WebApplicationContext) ctx;

//        System.setProperty("javax.net.ssl.trustStore",
//                wctx.getServletContext().getRealPath("WEB-INF/sms-services.keystore"));
//        System.setProperty("javax.net.ssl.trustStorePassword", "smevente73");

        // server works in UTC, all date-times will be converted according to user's time zone
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));

        LOG.info("ApplicationContextHelper initialized ok");
    }

}
