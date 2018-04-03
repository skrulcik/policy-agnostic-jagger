package com.scottkrulcik.agnostic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Labels the default value for a specific label.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface SafeDefault {

    /**
     * The name of the label that this predicate guards.
     */
    String value();
}
