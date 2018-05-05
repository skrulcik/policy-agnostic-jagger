package com.scottkrulcik.agnostic;

public interface SanitizingFactory<T> {
    T wrap(T raw);
}
