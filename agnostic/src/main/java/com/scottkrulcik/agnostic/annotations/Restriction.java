package com.scottkrulcik.agnostic.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
}
