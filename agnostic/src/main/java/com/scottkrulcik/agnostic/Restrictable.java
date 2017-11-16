package com.scottkrulcik.agnostic;

import com.google.common.reflect.TypeToken;

public interface Restrictable<T extends Restrictable> {
    T defaultValue();

    default TypeToken<T> token() {
        return new TypeToken<T>(getClass()) { };
    }

}
