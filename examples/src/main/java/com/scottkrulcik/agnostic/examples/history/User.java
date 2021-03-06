package com.scottkrulcik.agnostic.examples.history;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class User {
    public static User create(String name) {
        return new AutoValue_User(name, new SearchHistory());
    }

    public abstract String name();
    public abstract SearchHistory history();
}
