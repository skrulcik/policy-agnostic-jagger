package com.scottkrulcik.agnostic.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.scottkrulcik.agnostic.LabelDefinition;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

/**
 * Marks a type as being a faceted value guarded by the given label.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Faceted {

    /**
     * One or more labels guarding this facet.
     */
    public Class<? extends LabelDefinition<?>> label();

    /**
     * A function that produces the default value for this class.
     *
     * It is assumed that the callable will always return the same instance. If parameter could
     * be an instance it would be, but {@link Callable} is just a workaround.
     */
    public Class<? extends Callable<?>> low();

}
