package com.scottkrulcik.agnostic.examples;

import static com.google.common.collect.Multimaps.synchronizedMultimap;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.scottkrulcik.agnostic.DAO;
import com.scottkrulcik.agnostic.SanitizingFactory;

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

    public <T> DAO<T> rawDAO(Class<T> clazz) {
        final DataStore snapshot = this;
        return new DAO<T>(){
            @Override
            public void add(T instance) {
                snapshot.add(clazz, instance);
            }

            @Override
            public Set<T> filter(Predicate<T> matcher) {
                return snapshot.filter(clazz, matcher);
            }
        };
    }

    public <T> DAO<T> sanitizedDAO(Class<T> clazz, SanitizingFactory<T> factory) {
        final DataStore snapshot = this;
        return new DAO<T>(){
            @Override
            public void add(T instance) {
                snapshot.add(clazz, instance);
            }

            @Override
            public Set<T> filter(Predicate<T> matcher) {
                return snapshot.filter(clazz, matcher).stream()
                    .map(factory::wrap)
                    .filter(matcher)
                    .collect(Collectors.toSet());
            }
        };
    }

}
