package com.scottkrulcik.agnostic;

import com.scottkrulcik.agnostic.LabelDefinition;
import com.scottkrulcik.agnostic.ViewingContext;
import com.scottkrulcik.agnostic.annotations.Faceted;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Simple test class to test the {@link Faceted} annotation.
 */
public class SimpleData {

    static final class DefaultName implements Callable<String> {

        @Override
        public String call() {
            return "Anonymous";
        }
    }

    static final class DefaultCreationDate implements Callable<Date> {

        @Override
        public Date call() throws Exception {
            return Date.from(Instant.EPOCH);
        }
    }

    static final class AlwaysYesLabel extends LabelDefinition<ViewingContext> {

        @Override
        public Set<Predicate<ViewingContext>> restrictions() {
            return Collections.emptySet();
        }
    }

    static final class AlwaysNoLabel extends LabelDefinition<ViewingContext> {

        @Override
        public Set<Predicate<ViewingContext>> restrictions() {
            return Collections.singleton(vc -> false);
        }
    }

    private final String name;
    private final Date creationDate;

    public SimpleData() {
        // TODO(skrulcik): Remove hard-coded name once constructors are implemented
        this.name = "Scott";
        this.creationDate = new Date();
    }

    @Faceted(label = AlwaysYesLabel.class, low = DefaultName.class)
    public String getName() {
        return name;
    }

    @Faceted(label = AlwaysNoLabel.class, low = DefaultCreationDate.class)
    public Date getCreationDate() {
        return creationDate;
    }

}
