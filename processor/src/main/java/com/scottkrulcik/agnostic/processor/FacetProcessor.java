package com.scottkrulcik.agnostic.processor;

import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationMirror;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationValue;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.scottkrulcik.agnostic.ViewingContext;
import com.scottkrulcik.agnostic.annotations.Faceted;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;


/**
 * Creates a faceted version of classes annotated with {@link Faceted}.
 */
@AutoService(javax.annotation.processing.Processor.class)
public class FacetProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Faceted.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Return {@code true} if the given originalMethod can be processed. Even though methods are the level
     * of annotation, we want to process their enclosing classes.
     *
     * Enclosing classes must not be final so they can be extended. TODO(skrulcik): Evaluate if
     * inheritance is the right decision here
     *
     * For simplicity, we are requiring that the annotations appear on methods of the top-level
     * class in a file (no nested or anonymous classes).
     *
     * TODO(skrulcik): Reduce Faceted restrictions, allow nested classes
     */
    private boolean isProcessableElement(Element element) {
        return element.getKind().isClass() &&
            ((TypeElement) typeUtils.asElement(element.asType())).getNestingKind().equals(
                NestingKind.TOP_LEVEL) &&
            !element.getModifiers().contains(Modifier.FINAL);
    }


    /**
     * Given a target class, return all of the {@link Faceted faceted} methods that it contains.
     */
    private Set<FacetedMethod> getEnclosedFacetedMethods(Element targetClass) {
        Preconditions.checkArgument(isProcessableElement(targetClass));

        return targetClass.getEnclosedElements().stream().filter(e -> {
            return e.getKind() == ElementKind.METHOD && e.getAnnotation(Faceted.class) != null;
        }).map(e -> FacetedMethod.create((ExecutableElement) e))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private static String facetedName(Element originalElement) {
        return originalElement.getSimpleName() + "Faceted";
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        // First, iterate through all of the methods with the Faceted annotation and retrieve
        // their enclosing classes. We must operate on their parent classes because we can only
        // gneerate new classes, not just new methods.
        Set<Element> facetedClasses = new HashSet<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Faceted.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                messager
                    .printMessage(Diagnostic.Kind.ERROR, "Only methods can be faceted", element);
                return true;
            }

            // Get the class that the method is inside of, because we can only create entire new
            // classes, not just new methods
            Element enclosingClass = element.getEnclosingElement();
            if (!isProcessableElement(enclosingClass)) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                    String.format("Cannot process %s enclosing %s.",
                        enclosingClass, element), element);
                return true;
            }
            facetedClasses.add(enclosingClass);
        }

        // Now actually go through the faceted classes, generating a faceted method for each
        // annotated method
        for (Element originalClass : facetedClasses) {
            System.out.println("Processing " + originalClass); // SCOTT DEBUG ONLY

            TypeSpec.Builder faceted = TypeSpec.classBuilder(facetedName(originalClass))
                .superclass(TypeName.get(originalClass.asType()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for (FacetedMethod method : getEnclosedFacetedMethods(originalClass)) {
                System.out.println(" +- " + method);
                faceted.addField(method.labelField);
                faceted.addField(method.lowField);
                faceted.addMethod(method.wrapperSpec(typeUtils));
            }

            System.out.println(" +---------");

            // Write the faceted version of the class into a java source file
            String originalPackage = ((PackageElement)originalClass.getEnclosingElement())
                .getQualifiedName().toString();
            JavaFile output = JavaFile.builder(originalPackage, faceted.build()).build();
            try {
                output.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Kind.ERROR, "Error writing to java file", originalClass);
            }

        }

        return true;
    }

    private static  TypeElement asElement(Types typeUtils, TypeMirror mirror) {
        return (TypeElement) typeUtils.asElement(mirror);
    }

    /**
     * Internal representation of a method annotated with the {@link Faceted} annotation. This
     * makes it easier to access the main components of the facet.
     *
     * TODO(skrulcik): Consider restructuring, possibly changing when specs and names are created
     * TOOD(skrulcik): Consider removing callable field and using method reference
     */
    private static final class FacetedMethod {
        static final String CTX = "context";

        final ExecutableElement originalMethod;
        final TypeMirror labelClass;
        final TypeMirror lowClass;
        final FieldSpec labelField;
        final FieldSpec lowField;

        FacetedMethod(ExecutableElement element, TypeMirror label,
            TypeMirror defaultValue, FieldSpec labelField, FieldSpec lowField) {
            this.originalMethod = element;
            this.labelClass = label;
            this.lowClass = defaultValue;
            this.labelField = labelField;
            this.lowField = lowField;
        }

        static FacetedMethod create(ExecutableElement element) {
            AnnotationMirror facetMirror = getAnnotationMirror(element, Faceted.class);
            assert facetMirror != null : "Cannot create FacetedMethod from unannotated method";
            AnnotationValue label = getAnnotationValue(facetMirror, "label");
            AnnotationValue lowCallable = getAnnotationValue(facetMirror, "low");
            assert label != null && lowCallable != null :
                "Both label and low are required for Faceted annotations";

            TypeName labelName = TypeName.get((TypeMirror) label.getValue());
            FieldSpec labelField = FieldSpec.builder(labelName, labelFieldName(element))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T();", labelName)
                .build();

            TypeName lowName = TypeName.get((TypeMirror) lowCallable.getValue());
            FieldSpec lowField = FieldSpec.builder(lowName, lowFieldName(element))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T();", lowName)
                .build();

            return new FacetedMethod(element,
                (TypeMirror) label.getValue(),
                (TypeMirror) lowCallable.getValue(),
                labelField,
                lowField);
        }

        private static String labelFieldName(Element originalMethod) {
            return facetedName(originalMethod) + "Label";
        }

        private static String lowFieldName(Element originalMethod) {
            return facetedName(originalMethod) + "LowSecurity";
        }

        MethodSpec wrapperSpec(Types typeUtils) {
            // TODO(skrulcik): Super fragile! Consider parameters and thrown exceptions
            TypeElement implementationType = asElement(typeUtils, labelClass);
            TypeElement parametrized = asElement(typeUtils, implementationType.getSuperclass());
            // TODO(skrulcik): Figure out how to get around this hard-coded ViewingContext part
            ClassName contextName = ClassName.get(ViewingContext.class);
            ClassName predicateName = ClassName.get(Predicate.class);
            TypeName predicateType = ParameterizedTypeName.get(predicateName, contextName);

            return MethodSpec.methodBuilder(facetedName(originalMethod))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.get(originalMethod.getReturnType()))
                // TODO(skrulcik): Support more than // viewing context
                .addParameter(ViewingContext.class, CTX)
                .beginControlFlow("for ($T $L : $N.restrictions())", predicateType,
                    "restriction", labelField)
                .beginControlFlow("if (!restriction.test($L))", CTX)
                .addStatement("return $N.call()", lowField)
                .endControlFlow()
                .endControlFlow()
                // Assumes no parameters in original method
                .addStatement("return $L", originalMethod)
                .addException(Exception.class)
                .build();
        }

        @Override
        public String toString() {
            return "FacetedMethod{" +
                "originalMethod=" + originalMethod +
                ", labelClass=" + labelClass +
                ", lowClass=" + lowClass +
                '}';
        }
    }
}
