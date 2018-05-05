package com.scottkrulcik.agnostic;

import java.util.Set;
import java.util.function.Predicate;

public interface DAO<T> {
    void add(T instance);
    Set<T> filter(Predicate<T> matcher);
}
