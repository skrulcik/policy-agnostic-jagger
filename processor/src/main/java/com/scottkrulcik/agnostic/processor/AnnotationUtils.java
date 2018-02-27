package com.scottkrulcik.agnostic.processor;

import com.google.common.base.Preconditions;
import com.sun.tools.javac.code.Attribute;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.METHOD;

/**
 * Common utilities for processing annotations.
 * <p>
 * The original methods of this class were from a
 * <a href="https://stackoverflow.com/a/10167558/3857959">Stack Overflow anser</a> regarding
 * retrieving annotation values during the processing step.
 */
final class AnnotationUtils {

    static final EnumSet<ElementKind> CLASS_LIKE_ELEMENT_KINDS =
        EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE);

    @Nullable
    static AnnotationMirror getAnnotationMirror(Element typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    static <T> List<T> asList(AnnotationValue annotationValue) {
        Preconditions.checkNotNull(annotationValue);
        Preconditions.checkArgument(annotationValue.getValue() instanceof List);
        List<Attribute.Constant> annotationConstants = (List<Attribute.Constant>) annotationValue.getValue();
        List<T> list = new ArrayList<>(annotationConstants.size());
        for (Attribute.Constant constant : annotationConstants) {
            list.add((T) constant.value);
        }
        return list;
    }

    /**
     * Returns the instance methods defined within the given class.
     */
    static List<ExecutableElement> instanceMethods(Element classElement) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(classElement.getKind()));

        return classElement.getEnclosedElements().stream()
            .filter(e -> e.getKind().equals(METHOD))
            .map(e -> (ExecutableElement) e)
            .collect(Collectors.toList());
    }

    /**
     * Returns the package that the given class is in.
     */
    static PackageElement getEnclosingPackage(Element originalClass) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(originalClass.getKind()));
        Element enclosingElement = originalClass.getEnclosingElement();
        while (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            enclosingElement = enclosingElement.getEnclosingElement();
        }
        return (PackageElement) enclosingElement;
    }

    /**
     * Converts a {@link TypeMirror} into a {@link TypeElement}.
     *
     * @param typeUtils {@code TypeUtils} from processingEnv.
     * @param mirror {@code TypeMirror} to convert.
     * @return {@code mirror} as a {@code TypeElement}.
     */
    private static TypeElement asElement(Types typeUtils, TypeMirror mirror) {
        return (TypeElement) typeUtils.asElement(mirror);
    }

    private AnnotationUtils() {
    }
}
