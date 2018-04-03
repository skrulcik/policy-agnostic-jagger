package com.scottkrulcik.agnostic.annotations;

import com.scottkrulcik.agnostic.LabelDefinition;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates {@link com.google.auto.value.AutoValue AutoValue} methods that should be restricted
 * under the application policy. The set of all restrictions in a program make up its "policy".
 *
 * Restrictions have to important values: a {@code label} and {@code defaultValue}. The
 * {@link LabelDefinition label} is a predicate that defines a rule for
 * access to data. The provided {@link Callable callable} is used to create a default value that
 * is shown if the rule defined in the label is not satisfied.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Restrict {

    /**
     * Name, or label, to call this restriction by elsewhere.
     */
    String value();
}
