package com.scottkrulcik.agnostic.processor;

import java.util.Map;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

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

   private AnnotationUtils() {}
}
