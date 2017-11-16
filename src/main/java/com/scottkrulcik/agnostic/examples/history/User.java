package com.scottkrulcik.agnostic.examples.history;

import com.google.auto.value.AutoValue;
import com.scottkrulcik.agnostic.Restrictable;

@AutoValue
public abstract class User implements Restrictable<User> {
    public static User create(String name) {
        return new AutoValue_User(name, new SearchHistory());
    }

    public abstract String name();
    public abstract SearchHistory history();

    public User defaultValue() {
        return new AutoValue_User("NO_NAME", new SearchHistory());
    }
}
