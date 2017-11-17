package com.scottkrulcik.agnostic;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import java.util.Set;

/**
 * Privacy policy for the application, determining if sensitive values can be viewed under specific
 * {@link ViewingContext viewing contexts}.
 */
public final class Policy {

    private final Multimap<TypeToken<?>, Object> restrictions =
        HashMultimap
        .create();

    public <T> T concretize(ViewingContext context, Facet<T> facet,
        TypeToken<T> token) {

        if (canSee(context, facet.high(), token)) {
            return facet.high();
        } else {
            return facet.low();
        }
    }

    private <T> boolean canSee(ViewingContext context,  T
        instance,
        TypeToken<T>
        token) {
        Set<? extends Restriction<T>> applicableRestrictions = getRestrictions(token);
        for (Restriction<T> restriction : applicableRestrictions) {
            if (!restriction.isVisible(context, instance)) {
                return false;
            }
        }
        return true;
    }

    public <T> void addRestriction(TypeToken<T> token, Restriction<T>
        restriction) {
        restrictions.put(token, restriction);
    }

    /**
     * Retrieves the set of restrictions associated with this object the given type.
     *
     * The generics and casting for this method are a bit crazy. There is one
     * suppressed warning about an unchecked cast, but we can be sure of the safety of the cast
     * because restrictions are being retrieved from the type token map. According to this
     * <a href="https://groups.google.com/d/msg/guava-discuss/FJ486g9y2O0/6NAvrp8AawUJ">answer</a>
     * to a post in the Guava users group, avoiding such a cast is impossible.
     *
     * TODO(skrulcik): Figure out how to deal with generics: restrictions on Pet should apply to
     * both Dog and Cat.
     *
     * @param token The token corresponding to {@code T}.
     * @param <T> The type to retrieve restrictions from.
     * @return The set of restrictions applicable to {@code T}.
     */
    private <T> Set<? extends Restriction<T>> getRestrictions(TypeToken<T>
        token) {
        TypeToken<Set<Restriction<T>>> listToken = new TypeToken<Set<Restriction<T>>>() {};
        @SuppressWarnings("unchecked")
        Set<Restriction<T>> restrictionSet =
            (Set<Restriction<T>>) listToken.getRawType().cast(restrictions.get(token));
        return restrictionSet;
    }

}
