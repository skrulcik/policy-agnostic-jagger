package com.scottkrulcik.agnostic.processor;

import com.google.common.base.Preconditions;
import com.sun.tools.javac.code.Attribute;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Common utilities for processing annotations.
 *
 * The original methods of this class were from a
 * <a href="https://stackoverflow.com/a/10167558/3857959">Stack Overflow anser</a> regarding
 * retrieving annotation values during the processing step.
 */
final class AnnotationUtils {

   @Nullable
   static AnnotationMirror getAnnotationMirror(Element typeElement, Class<?> clazz) {
      String clazzName = clazz.getName();
      for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
         if(m.getAnnotationType().toString().equals(clazzName)) {
            return m;
         }
      }
      return null;
   }

   @Nullable
   static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
      Map<? extends ExecutableElement, ? extends AnnotationValue> valueMap = annotationMirror.getElementValues();
      for(ExecutableElement element : valueMap.keySet()) {
         if(element.getSimpleName().toString().equals(key)) {
            return valueMap.get(element);
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

   private AnnotationUtils() {}
}
