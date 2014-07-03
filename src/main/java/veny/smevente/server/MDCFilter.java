package veny.smevente.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Servlet Filter to store username currently logged in user into Log4j MDC context.
 * Inspired by {@link https://blog.trifork.com/2013/06/06/
 * adding-user-info-to-log-entries-in-a-multi-user-app-using-mapped-diagnostic-context/}
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 17.5.2011
 */
public class MDCFilter implements Filter {

    /** {@inheritDoc} */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /** {@inheritDoc} */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain)
        throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            MDC.put("username", authentication.getName());
        }
        try {
            chain.doFilter(req, resp);
        } finally {
            if (authentication != null) {
                MDC.remove("username");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void destroy() {
    }

}
