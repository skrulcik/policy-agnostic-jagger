package com.scottkrulcik.agnostic.examples.coursemanager.model;

import java.util.Optional;

/**
 * The current viewing context.
 */
public final class Context {
    private Teacher currentUser;

    public Optional<Teacher> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public void setCurrentUser(Teacher currentUser) {
        this.currentUser = currentUser;
    }
}
