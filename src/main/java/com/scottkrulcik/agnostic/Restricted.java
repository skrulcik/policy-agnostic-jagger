package com.scottkrulcik.agnostic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Restricted {
    Class<? extends Restriction>[] value();
}
