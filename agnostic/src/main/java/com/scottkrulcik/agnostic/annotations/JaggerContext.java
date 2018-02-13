package com.scottkrulcik.agnostic.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marker annotation identifying Dagger {@link dagger.Component Component}s as viewing contexts
 * for Jagger.
 */
@Target(ElementType.TYPE)
public @interface JaggerContext {

}
