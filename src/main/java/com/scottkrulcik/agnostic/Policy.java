package com.scottkrulcik.agnostic;


import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Privacy policy for the application, determining if {@link Label labeled} sensitive values can
 * be viewed under specific {@link ViewingContext viewing contexts}.
 */
public final class Policy {

    private final Map<Class<?>, Object> restrictions = new HashMap<>();

    public <T> T concretize(ViewingContext context, Facet<T> facet, Class<Set<Restriction<T>>>
        clazz) {
        if (canSee(context, facet.high(), clazz)) {
            return facet.high();
        } else {
            return facet.low();
        }
    }

    private <T> boolean canSee(ViewingContext context,  T instance, Class<Set<Restriction<T>>>
        clazz) {
        Set<Restriction<T>> applicableRestrictions =
            clazz.cast(this.restrictions.getOrDefault(clazz, emptySet()));
        for (Restriction<T> restriction : applicableRestrictions) {
            if (!restriction.isVisible(context, instance)) {
                return false;
            }
        }
        return true;
    }

    public <T> void addRestriction(Class<Set<Restriction<T>>> clazz, Restriction<T> restriction) {
        restrictions.put(clazz, restriction);
    }

    private <T> Set<Restriction<T>> getRestrictions(Class<Set<Restriction<T>>> clazz) {
        return clazz.cast(restrictions.get(clazz));
    }

}
