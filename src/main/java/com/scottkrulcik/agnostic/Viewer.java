package com.scottkrulcik.agnostic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Inherited
@Target({ElementType.TYPE_USE})
public @interface Viewer {

}
