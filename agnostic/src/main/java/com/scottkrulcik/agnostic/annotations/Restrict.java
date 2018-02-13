package com.scottkrulcik.agnostic.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.scottkrulcik.agnostic.LabelDefinition;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

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
    String label();

    /**
     * Labels that this label depends on. Label A depends on label B if a field guarded by label
     * B is used by label A.
     */
    String[] dependencies() default {};

    /**
     * A function that produces the default value for this class.
     *
     * It is assumed that the callable will always return the same instance. If parameter could
     * be an instance it would be, but {@link Callable} is just a workaround.
     */
    Class<? extends Callable<?>> defaultValue();
}
