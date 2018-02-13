package com.scottkrulcik.agnostic.data;

import com.google.auto.value.AutoValue;
import com.scottkrulcik.agnostic.LabelDefinition;
import com.scottkrulcik.agnostic.ViewingContext;
import com.scottkrulcik.agnostic.annotations.Faceted;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.scottkrulcik.agnostic.annotations.Restriction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import javax.inject.Qualifier;

/**
 * Simple test class to test the {@link Faceted} annotation.
 */
@AutoValue
public abstract class SampleData {

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

    public static final class AlwaysYesLabel extends LabelDefinition<ViewingContext> {

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        public @interface Result { }

        @Override
        public Set<Predicate<ViewingContext>> restrictions() {
            return Collections.emptySet();
        }

        @Override
        public Set<Class<? extends LabelDefinition<?>>> dependencies() {
            return Collections.emptySet();
        }
    }


    public static final class AlwaysNoLabel extends LabelDefinition<ViewingContext> {

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        public @interface Result { }

        @Override
        public Set<Predicate<ViewingContext>> restrictions() {
            return Collections.singleton(vc -> false);
        }

        @Override
        public Set<Class<? extends LabelDefinition<?>>> dependencies() {
            return Collections.emptySet();
        }
    }

    @Restrict(label = "name", defaultValue = DefaultName.class)
    public abstract String name();

    @Restrict(label = "creationDate", defaultValue = DefaultCreationDate.class)
    public abstract Date creationDate();

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

    @Restriction("creationDate")
    boolean isCreationDateVisible() {
        return false;
    }

    @Restriction("name")
    boolean isNameVisible() {
        return true;
    }

}
