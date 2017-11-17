package com.scottkrulcik.agnostic.examples.coursemanager.model;

import com.google.auto.value.AutoValue;
import java.util.Date;
import jdk.nashorn.internal.ir.Assignment;

@AutoValue
public abstract class Submission {
    public static Submission create(Assignment assignment, Student author, int grade) {
        return new com.scottkrulcik.agnostic.examples.coursemanager.model.AutoValue_Submission(new Date(), assignment, author, grade);
    }

    public abstract Date submissionDate();
    public abstract Assignment assignment();
    public abstract Student author();
    public abstract int grade();
}

