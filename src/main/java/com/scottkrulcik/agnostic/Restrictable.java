package com.scottkrulcik.agnostic;

import java.util.ArrayList;

public interface Restrictable<T extends Restrictable> {
    T defaultValue();

    @SuppressWarnings("unchecked")
    default Class<ArrayList<Restriction<T>>> token() {
        ArrayList<Restriction<T>> bullshit = new ArrayList<>();
        return (Class<ArrayList<Restriction<T>>>) bullshit.getClass();
    }
}
