package com.scottkrulcik.agnostic.examples;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Multimaps.synchronizedMultimap;

/**
 * Simple container for application data.
 */
public final class DataStore {
    private final Object dataLock = new Object();
    @GuardedBy("dataLock")
    private final Multimap<Class<?>, Object> data = synchronizedMultimap(HashMultimap.create());

    public final <T> void add(Class<T> clazz, T instance) {
        synchronized (dataLock) {
            data.put(clazz, instance);
        }
    }

    public final <T> Set<T> filter(Class<T> clazz, Predicate<T> matcher) {
        synchronized (dataLock) {
            @SuppressWarnings("unchecked")
            Collection<T> unfilteredResults = (Collection<T>) data.get(clazz);
            return unfilteredResults.stream().filter(matcher).collect(Collectors.toSet());
        }
    }
}
