package veny.smevente.server;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

/**
 * Simple view displays everything from the model.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 11.8.2010
 */
public class SimpleView extends AbstractView {

    /**
     * Default content type. Can be defined as bean property.
     */
    public static final String DEFAULT_CONTENT_TYPE = "application/json";

    /**
     * Construct a new {@code SimpleView}, setting the content type to {@code application/json}.
     */
    public SimpleView() {
        setContentType(DEFAULT_CONTENT_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected void renderMergedOutputModel(
        final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {

        response.setContentType(getContentType());
        response.getWriter().write(model.get("text").toString());
    }

}
