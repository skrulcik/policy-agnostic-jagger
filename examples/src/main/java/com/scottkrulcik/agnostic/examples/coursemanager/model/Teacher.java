package com.scottkrulcik.agnostic.examples.coursemanager.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Teacher {

    public static Teacher create(String name) {
        return new com.scottkrulcik.agnostic.examples.coursemanager.model.AutoValue_Teacher(name);
    }

    public abstract String name();
}

