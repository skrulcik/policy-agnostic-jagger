package com.scottkrulcik.agnostic.processor;

import com.scottkrulcik.agnostic.annotations.Faceted;
import java.util.concurrent.Callable;

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

    private String name;

    public SimpleData(String name) {
        this.name = name;
    }

    @Faceted(label = DummyLabel.class, low = DefaultName.class)
    public String getName() {
        return name;
    }

}
