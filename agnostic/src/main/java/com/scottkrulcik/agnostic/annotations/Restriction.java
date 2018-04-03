package com.scottkrulcik.agnostic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A restriction is a predicate whose result defines whether the label is {@code true} for the
 * given context.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Restriction {

    /**
     * The name of the label that this predicate guards.
     */
    String value();


    /**
     * Labels that this label depends on. Label A depends on label B if a field guarded by label
     * B is used by label A. Eventually, this should be determined by static analysis, rather than
     * by the programmer.
     */
    String[] dependencies() default {};
}
