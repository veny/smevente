package veny.smevente.misc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an entity that can be soft deleted
 * (not physically removed but flagged as deleted).
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 15.4.2011
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SoftDelete {

    /**
     * Name of an attribute representing a flag that the object has been deleted.
     */
    String attribute() default "deleted";

}
