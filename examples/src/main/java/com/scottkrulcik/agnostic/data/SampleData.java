package com.scottkrulcik.agnostic.data;

import com.google.auto.value.AutoValue;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.scottkrulcik.agnostic.annotations.Restriction;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Simple test class to test the {@link Restrict} and {@link Restriction} annotations.
 */
@AutoValue
public abstract class SampleData {

    @Restrict(label = "creationDate", defaultValue = DefaultCreationDate.class)
    public abstract Date creationDate();

    @Restriction("creationDate")
    boolean isCreationDateVisible() {
        return false;
    }

    @Restrict(label = "name", defaultValue = DefaultName.class, dependencies = {"creationDate"})
    public abstract String name();

    @Restriction("name")
    boolean isNameVisible() {
        return true;
    }


    public abstract SampleData withName(String name);

    public abstract SampleData withCreationDate(Date creationDate);

    protected SampleData sanitize() {
        SampleData sanitized = this;
        if (!isNameVisible()) {
            sanitized = this.withName(new DefaultName().call());
        }
        if (!isCreationDateVisible()) {
            sanitized = this.withCreationDate(new DefaultCreationDate().call());
        }
        return sanitized;
    }

    public static final class DefaultName implements Callable<String> {

        @Override
        public String call() {
            return "Anonymous";
        }
    }

    public static final class DefaultCreationDate implements Callable<Date> {

        @Override
        public Date call() {
            return Date.from(Instant.EPOCH);
        }
    }

}
