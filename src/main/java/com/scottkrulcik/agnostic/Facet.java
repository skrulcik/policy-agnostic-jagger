package com.scottkrulcik.agnostic;


import com.google.auto.value.AutoValue;
import java.util.function.Function;
import java.util.function.Predicate;

@AutoValue
public abstract class Facet<T> {

    static <T> Facet<T> create(T high, T low) {
        return new AutoValue_Facet<>(high, low);
    }

    static <T, I> Function<Facet<I>, Facet<T>> wrap(Function<I, T> f) {
        return (inputFacet) -> new Facet<T>() {
            @Override
            public T high() {
                return f.apply(inputFacet.high());
            }

            @Override
            public T low() {
                return f.apply(inputFacet.low());
            }
        };
    }

    public abstract T high();
    public abstract T low();
}
