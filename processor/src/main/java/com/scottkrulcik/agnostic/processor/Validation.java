package com.scottkrulcik.agnostic.processor;

import com.google.auto.common.MoreTypes;
import com.scottkrulcik.agnostic.annotations.Restriction;
import com.scottkrulcik.agnostic.annotations.SafeDefault;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import static com.google.auto.common.MoreElements.asExecutable;
import static com.google.auto.common.MoreElements.asVariable;

/**
 * Central location for validating that element kinds, modifiers, and types are appropriate for
 * specific annotations.
 */
final class Validation {

    /**
     * Validates that {@link Restriction} is only applied to public, non-static methods.
     */
    static boolean isRestrictionValid(Element e) {
        return e.getKind().equals(ElementKind.METHOD)
            && e.getModifiers().contains(Modifier.PUBLIC)
            && e.getModifiers().contains(Modifier.FINAL);
    }

    /**
     * Checks that {@link SafeDefault} is only applied to public, static fields.
     */
    static boolean isDefaultValid(Element e) {
        return e.getKind().equals(ElementKind.FIELD)
            && e.getModifiers().contains(Modifier.PUBLIC)
            && e.getModifiers().contains(Modifier.FINAL)
            && e.getModifiers().contains(Modifier.STATIC);
    }

    /**
     * Returns true iff the return type of the accessor is the same as the type of the default field.
     */
    static boolean isValidDefault(Element accessor, Element safeDefault) {
        ExecutableElement method = asExecutable(accessor);
        VariableElement constant = asVariable(safeDefault);
        return MoreTypes.equivalence().equivalent(method.getReturnType(), constant.asType());
    }

    private Validation() {}
}
