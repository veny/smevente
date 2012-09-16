package veny.smevente.server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import veny.smevente.shared.SmeventeException;
import eu.maydu.gwt.validation.client.InvalidValueSerializable;
import eu.maydu.gwt.validation.client.ValidationException;

/**
 * Resolves server-side exceptions and transforms them to JSON format.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 11.8.2010
 */
public class JsonExceptionResolver extends SimpleMappingExceptionResolver {

    /** JSON mapper. Can be reused. */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Exposes an exception as JSON using the ModelAndView with the given viewName.
     * @param viewName the view name to be used
     * @param ex exception to be transformed
     * @return transformed ExceptionDTO instance
     */
    @Override
    protected ModelAndView getModelAndView(final String viewName, final Exception ex) {
        ModelAndView mv = new ModelAndView(viewName);

        // generate the ID
        final String id = "" + System.currentTimeMillis() + "-" + Thread.currentThread().hashCode();

        // log the exception
        logger.info("exposing exception " + id, ex);

        // add exception to the model
        mv.addObject("text", mapToJson(ex, id));
        return mv;
    }

    /**
     * Creates JSON representation from the given Exception instance.
     * @param ex the source Exception
     * @param id the ID
     * @return the ExceptionDTO instance
     */
    private String mapToJson(final Exception ex, final String id) {

        final List<InvalidValueSerializable> invalidValues;
        if (ex instanceof ValidationException) {
            invalidValues = ((ValidationException) ex).getInvalidValues();
        } else {
            invalidValues = null;
        }

        final StringWriter output = new StringWriter();
        JsonGenerator generator = null;
        try {
            generator = objectMapper.getJsonFactory().createJsonGenerator(output);
            generator.writeStartObject();
            generator.writeObjectFieldStart("exception");
            generator.writeStringField("className", ex.getClass().getName());
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            generator.writeStringField("message", msg);
            generator.writeStringField("id", id);

            if (invalidValues != null) {
                generator.writeArrayFieldStart("invalidValues");

                for (InvalidValueSerializable invalidValue : invalidValues) {
                    generator.writeStartObject();
                    generator.writeStringField("message", invalidValue.getMessage());
                    generator.writeStringField("propertyName", invalidValue.getPropertyName());
                    generator.writeEndObject();
                }
                generator.writeEndArray();
            }

            generator.writeEndObject();
            generator.writeEndObject();
        } catch (IOException e) {
            throw new SmeventeException("cannot create JsonGenerator", e);
        } finally {
            if (null != generator) {
                try {
                    generator.close();
                } catch (IOException e) {
                    logger.warn("cannot close JsonGenerator", e);
                }
            }
        }

        final String result = output.toString();
        try {
            output.close();
        } catch (IOException e) {
            logger.warn("cannot close StringWriter", e);
        }
        return result;
    }

}
