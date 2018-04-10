package com.scottkrulcik.agnostic.processor;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static com.scottkrulcik.agnostic.processor.AnnotationUtils.CLASS_LIKE_ELEMENT_KINDS;
import static javax.lang.model.element.ElementKind.METHOD;

final class Naming {

    static final String LABEL_FIELD = "value";
    static final String DEPENDENCIES_FIELD = "dependencies";

    static String sanitizerName(Element originalClass) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(originalClass.getKind()));
        return originalClass.getSimpleName() + "SanitizerModule";
    }

    static String contextComponentName(Element originalClass) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(originalClass.getKind()));
        return originalClass.getSimpleName() + "Component";
    }

    static String getSimpleName(TypeMirror typeMirror) {
        List<String> tokens = Splitter.on("\\.").splitToList(typeMirror.toString());
        return tokens.get(tokens.size() - 1);
    }

    static String rawMethodName(Element originalMethod) {
        Preconditions.checkArgument(originalMethod.getKind().equals(METHOD));
        String lowerCamelName = originalMethod.getSimpleName().toString();
        char firstCharacter = lowerCamelName.charAt(0);
        String upperCamelName = Character.toUpperCase(firstCharacter) + lowerCamelName.substring(1);
        return "raw" + upperCamelName;
    }

    private Naming() {}
}
