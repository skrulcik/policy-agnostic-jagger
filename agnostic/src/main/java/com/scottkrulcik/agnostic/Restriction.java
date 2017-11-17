package com.scottkrulcik.agnostic;

/**
 * Limits the visibility of an object.
 */
@FunctionalInterface
public interface Restriction<T> {

    /**
     *
     * @param context The context the restricted instance will be viewed in.
     * @return {@code true} if the restricted object is
     */
    boolean isVisible(ViewingContext context, T instance);
}
