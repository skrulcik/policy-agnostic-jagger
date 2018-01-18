package com.scottkrulcik.agnostic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/**
 * Qualifier to specify that the raw, true-valued instance of an object before it is sanitized.
 * Such objects will be used as inputs to a sanitizer module, which will provide the sanitized
 * object without a qualifier.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Raw {
}
