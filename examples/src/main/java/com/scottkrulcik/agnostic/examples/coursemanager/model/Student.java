package com.scottkrulcik.agnostic.examples.coursemanager.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Student {
    public Student create(String name, Teacher teacher) {
        return new com.scottkrulcik.agnostic.examples.coursemanager.model.AutoValue_Student(name, teacher);
    }

    public abstract String name();
    public abstract Teacher teacher();
}

