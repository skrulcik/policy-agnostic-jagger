package com.scottkrulcik.agnostic.examples.coursemanager.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Assignment {
    public static Assignment create(String name, Teacher author) {
        return new com.scottkrulcik.agnostic.examples.coursemanager.model.AutoValue_Assignment(name, author);
    }

    public abstract String name();
    public abstract Teacher author();
}

