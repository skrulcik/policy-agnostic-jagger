package com.scottkrulcik.agnostic;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Privacy policy for the application, determining if {@link Label labeled} sensitive values can
 * be viewed under specific {@link ViewingContext viewing contexts}.
 */
public final class Policy {

    private final Multimap<Class<?>, Object> restrictions = ArrayListMultimap.create();

    public <T extends Restrictable<T>> T concretize(ViewingContext context,  T object) {
        return concretize(context, Facet.faceted(object), object.token());
    }

    public <T> T concretize(ViewingContext context, Facet<T> facet, Class<ArrayList<Restriction<T>>>
        clazz) {
        if (canSee(context, facet.high(), clazz)) {
            return facet.high();
        } else {
            return facet.low();
        }
    }

    private <T> boolean canSee(ViewingContext context,  T instance, Class<ArrayList<Restriction<T>>>
        clazz) {
        List<Restriction<T>> applicableRestrictions = getRestrictions(clazz);
        for (Restriction<T> restriction : applicableRestrictions) {
            if (!restriction.isVisible(context, instance)) {
                return false;
            }
        }
        return true;
    }

    public <T> void addRestriction(Class<Restriction<T>> clazz, Restriction<T> restriction) {
        // TODO(krulcik): Multiple restrictions
        restrictions.put(clazz, restriction);
    }

    private <T> List<Restriction<T>> getRestrictions(Class<ArrayList<Restriction<T>>> clazz) {
        return clazz.cast(restrictions.get(clazz));
    }

}
