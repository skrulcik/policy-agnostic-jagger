package com.scottkrulcik.agnostic.processor;

import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationMirror;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationValue;
import static java.lang.Character.toUpperCase;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.scottkrulcik.agnostic.annotations.JaggerContext;
import com.scottkrulcik.agnostic.annotations.Raw;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import dagger.Provides;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(javax.annotation.processing.Processor.class)
public class RestrictionProcessor extends BasicAnnotationProcessor {

    private static final String LABEL_FIELD = "label";

    /**
     * Holds the most recent type mirror for the given canonical type string. I observed that
     * {@link TypeMirror}s are not equal across runs, even using the
     * {@link com.google.auto.common.MoreTypes MoreTypes} equivalence. This is the best way I can
     * think of to keep track of type mirrors without duplicating.
     */
    private final Map<String, TypeMirror> canonicalLabels = new HashMap<>();
    // TODO(skrulcik): Consider whether this should be maintained across runs
    private final SetMultimap<String, String> labelDeps = HashMultimap.create();

    private final class CollectLabels implements ProcessingStep {

        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return Collections.singleton(Restrict.class);
        }

        @Override
        public Set<? extends Element> process(
            SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            Set<Element> elements = elementsByAnnotation.get(Restrict.class);
            for (Element e : elements) {
                AnnotationMirror restrictAnnotation = getAnnotationMirror(e, Restrict.class);
                TypeMirror label = (TypeMirror) getAnnotationValue(restrictAnnotation,
                    LABEL_FIELD).getValue();
                canonicalLabels.put(label.toString(), label);
            }
            return Collections.emptySet();
        }
    }

    private static String sanitizerName(Element originalElement) {
        return "Jagger_" + originalElement.getSimpleName() + "SanitizerModule";
    }

    private final class CreateSanitizerModules implements ProcessingStep {

        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return Collections.singleton(JaggerContext.class);
        }

        @Override
        public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            System.out.println("Processing " + elementsByAnnotation);
            Filer filer = processingEnv.getFiler();
            Messager messager = processingEnv.getMessager();

            for (Element originalClass : elementsByAnnotation.get(JaggerContext.class)) {
                System.out.println("Creating sanitizer for " + originalClass); // SCOTT DEBUG ONLY
                TypeSpec.Builder sanitizerModule = TypeSpec.classBuilder(sanitizerName
                    (originalClass))
                    .addAnnotation(dagger.Module.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

                originalClass.getEnclosedElements().stream().filter(e -> e
                    .getKind().equals(ElementKind.METHOD)).forEach(element ->
                    {
                        ExecutableElement method = (ExecutableElement) element;

                        // Create a provider method name
                        String methodName = method.getSimpleName().toString();
                        char c = methodName.charAt(0);
                        methodName = "provides" + toUpperCase(c) + methodName.substring(1);

                        // Determine the type of the method parameter (we'll add the Raw)
                        // TODO(skrulcik): Validate that exactly one @Raw parameter exists
                        TypeName paramType = TypeName.get(method.getReturnType());

                        ParameterSpec providerParam = ParameterSpec.builder(paramType, "rawValue")
                            .addAnnotation(Raw.class)
                            .build();

                        sanitizerModule.addMethod(MethodSpec.methodBuilder(methodName)
                            .addAnnotation(Provides.class)
                            .addParameter(providerParam)
                            .addStatement("return $N.sanitize()", providerParam)
                            .returns(paramType)
                            .build());
                    }
                );

                // Write the faceted version of the class into a java source file
                String originalPackage = ((PackageElement) originalClass.getEnclosingElement())
                    .getQualifiedName().toString();
                JavaFile output = JavaFile.builder(originalPackage, sanitizerModule.build()).build();
                try {
                    System.out.println("Writing file to filer");
                    output.writeTo(filer);
                    System.out.println("Finished writing to " + originalPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                    messager.printMessage(Kind.ERROR, "Error writing to java file", originalClass);
                }
            }

            return Collections.emptySet();
        }
    }

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        System.out.println("INIT PROCESSOR");
        return Collections.singletonList(new CreateSanitizerModules());
    }

    @Override
    protected void postRound(RoundEnvironment roundEnv) {
        super.postRound(roundEnv);
        if (roundEnv.processingOver()) {
            System.out.println("Here are the labels: >>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println(canonicalLabels);
        }
    }

    private static TypeElement asElement(Types typeUtils, TypeMirror mirror) {
        return (TypeElement) typeUtils.asElement(mirror);
    }
}
