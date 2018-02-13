package com.scottkrulcik.agnostic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Qualifier;

/**
 * {@code Label}s identify how values should be restricted by the security policy.
 *
 * All annotations annotated by {@code Label} are expected to include a field named {@code
 * restrictions} that contains the set of restrictions associated with that label.
 *
 * TODO(skrulcik): Evaluate using Annotation to avoid "newInstance"
 */
public abstract class LabelDefinition<T> {
    public LabelDefinition() {}

    // TODO(skrulcik): Create a result annotation per-label
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface Result { }

    public abstract Set<Predicate<T>> restrictions();

    public abstract Set<Class<? extends LabelDefinition<?>>> dependencies();
}
