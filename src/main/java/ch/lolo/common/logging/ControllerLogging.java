package ch.lolo.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional per-controller override for inbound logging flags.
 * <p>
 * Priority: method-level annotation > class-level annotation > app.logging defaults.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerLogging {

    boolean logRequest() default true;

    boolean logResponse() default true;

    boolean logErrors() default true;
}

