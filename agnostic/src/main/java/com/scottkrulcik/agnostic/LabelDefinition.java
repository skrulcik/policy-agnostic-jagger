package com.scottkrulcik.agnostic;

import java.util.Set;
import java.util.function.Predicate;

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

    public abstract Set<Predicate<T>> restrictions();
}
