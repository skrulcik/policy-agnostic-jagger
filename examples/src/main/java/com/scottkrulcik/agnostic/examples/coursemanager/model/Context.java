package com.scottkrulcik.agnostic.examples.coursemanager.model;

import com.scottkrulcik.agnostic.examples.DataStore;

import java.util.Optional;

/**
 * The current viewing context.
 */
public final class Context {
    private Teacher currentUser;
    private final DataStore dataStore = new DataStore();

    public DataStore getDataStore() {
        return dataStore;
    }

    public Optional<Teacher> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public void setCurrentUser(Teacher currentUser) {
        this.currentUser = currentUser;
    }
}
