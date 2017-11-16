package com.scottkrulcik.agnostic.examples.guests;

import com.google.auto.value.AutoValue;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
abstract class Person {

    private static final AtomicLong idGen = new AtomicLong(0);

    static Person create(String name) {
        return new AutoValue_Person(idGen.getAndIncrement(), name);
    }

    abstract long id();
    abstract String name();
}
