package com.scottkrulcik.agnostic;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Facet<T, L extends Label> {
    static <T,L extends Label> Facet<T,L> create(Class<L> label, T high, T low) {
        return new AutoValue_Facet<>(label, high, low);
    }

    abstract Class<L> label();
    abstract T high();
    abstract T low();
}
