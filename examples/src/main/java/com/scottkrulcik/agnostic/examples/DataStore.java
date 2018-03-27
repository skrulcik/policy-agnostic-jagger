package com.scottkrulcik.agnostic.examples;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Simple container for application data.
 */
public final class DataStore {
    private final Multimap<Class<?>, Object> data = HashMultimap.create();

    public <T> void add(Class<T> clazz, T instance) {
        data.put(clazz, instance);
    }

    public <T> Set<T> filter(Class<T> clazz, Predicate<T> matcher) {
        @SuppressWarnings("unchecked")
        Collection<T> unfilteredResults = (Collection<T>) data.get(clazz);
        return unfilteredResults.stream().filter(matcher).collect(Collectors.toSet());
    }
}
