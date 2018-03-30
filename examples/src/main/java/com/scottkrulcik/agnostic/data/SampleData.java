package com.scottkrulcik.agnostic.data;

import com.google.auto.value.AutoValue;
import com.scottkrulcik.agnostic.annotations.Default;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.scottkrulcik.agnostic.annotations.Restriction;

import java.time.Instant;
import java.util.Date;

/**
 * Simple test class to test the {@link Restrict} and {@link Restriction} annotations.
 */
@AutoValue
public abstract class SampleData {
    @Default("name")
    public static final String ANONYMOUS = "Anonymous";
    @Default("creationDate")
    public static final Date DEFAULT_DATE = Date.from(Instant.EPOCH);

    @Restrict(label = "creationDate")
    public abstract Date creationDate();

    @Restriction("creationDate")
    public final boolean isCreationDateVisible() {
        return false;
    }

    @Restrict(label = "name", dependencies = {"creationDate"})
    public abstract String name();

    @Restriction("name")
    public final boolean isNameVisible() {
        return true;
    }


    public abstract SampleData withName(String name);

    public abstract SampleData withCreationDate(Date creationDate);

    protected SampleData sanitize() {
        SampleData sanitized = this;
        if (!isNameVisible()) {
            sanitized = this.withName(ANONYMOUS);
        }
        if (!isCreationDateVisible()) {
            sanitized = this.withCreationDate(DEFAULT_DATE);
        }
        return sanitized;
    }

}
