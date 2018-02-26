package com.scottkrulcik.agnostic.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.scottkrulcik.agnostic.annotations.JaggerContext;
import com.scottkrulcik.agnostic.annotations.Raw;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Provides;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Qualifier;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationMirror;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationValue;
import static javax.lang.model.element.ElementKind.METHOD;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(javax.annotation.processing.Processor.class)
public class RestrictionProcessor extends BasicAnnotationProcessor {

    // TODO(skrulcik): Consider whether this should be maintained across runs
    private final Set<String> allLabels = new HashSet<>();
    private final MutableGraph<String> labelDeps = GraphBuilder.directed().allowsSelfLoops(true).build();

    private final class CollectLabels implements ProcessingStep {

        private static final String LABEL_FIELD = "label";
        private static final String DEPENDENCIES_FIELD = "dependencies";

        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return Collections.singleton(Restrict.class);
        }

        @Override
        public Set<? extends Element> process(
            SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            Set<Element> elements = elementsByAnnotation.get(Restrict.class);
            assert elements.size() == elementsByAnnotation.size();
            Set<Element> unprocessable = new HashSet<>();

            for (Element e : elements) {
                AnnotationMirror restrictAnnotation = getAnnotationMirror(e, Restrict.class);
                if (restrictAnnotation == null) {
                    unprocessable.add(e);
                    continue;
                }
                // Get the raw versions of annotation values, and check before casting
                AnnotationValue rawLabel = getAnnotationValue(restrictAnnotation, LABEL_FIELD);
                AnnotationValue rawDeps = getAnnotationValue(restrictAnnotation, DEPENDENCIES_FIELD);
                if (rawLabel == null) {
                    unprocessable.add(e);
                    continue;
                }

                String label = (String) rawLabel.getValue();
                allLabels.add(label);

                // TODO(skrulcik): investigate why this is a NPE instead of proper default of {}
                try {
                    List<String> dependencies = AnnotationUtils.asList(rawDeps);
                    for (String dep : dependencies) {
                        labelDeps.addNode(dep);
                        labelDeps.putEdge(label, dep);
                    }
                } catch (NullPointerException ignore) {
                    // No dependencies, not a problem
                }
            }
            // Do not return un-processable elements, because if they are not well-formed now, they
            // won't be in the future
            if (!unprocessable.isEmpty()) {
                processingEnv.getMessager().printMessage(Kind.WARNING,
                    "The following restricted fields were unprocessable: " + unprocessable);
            }
            return Collections.emptySet();
        }
    }

    private static final EnumSet<ElementKind> CLASS_LIKE_ELEMENT_KINDS =
        EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE);

    private static String sanitizerName(Element originalClass) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(originalClass.getKind()));
        return originalClass.getSimpleName() + "SanitizerModule";
    }

    private static String contextComponentName(Element originalClass) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(originalClass.getKind()));
        return originalClass.getSimpleName() + "Component";
    }

    private static String getSimpleName(TypeMirror typeMirror) {
        String[] tokens = typeMirror.toString().split("\\.");
        return tokens[tokens.length - 1];
    }

    private static String rawMethodName(Element originalMethod) {
        Preconditions.checkArgument(originalMethod.getKind().equals(METHOD));
        String lowerCamelName = originalMethod.getSimpleName().toString();
        char firstCharacter = lowerCamelName.charAt(0);
        String upperCamelName = Character.toUpperCase(firstCharacter) + lowerCamelName.substring(1);
        return "raw" + upperCamelName;
    }

    /**
     * Returns the instance methods defined within the given class.
     */
    private static List<ExecutableElement> instanceMethods(Element classElement) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(classElement.getKind()));

        return classElement.getEnclosedElements().stream()
            .filter(e -> e.getKind().equals(METHOD))
            .map(e -> (ExecutableElement) e)
            .collect(Collectors.toList());
    }

    private void writeToFile(JavaFile outputFile) {
        Filer filer = processingEnv.getFiler();
        Messager messager = processingEnv.getMessager();
        try {
            messager.printMessage(Kind.NOTE,
                "+ generating " + outputFile.typeSpec.name);
            outputFile.writeTo(filer);
        } catch(IOException e) {
            e.printStackTrace();
            messager.printMessage(Kind.NOTE,
                "Error writing to java file " + outputFile.typeSpec.name);
        }
    }

    private final class CreateSanitizerModules implements ProcessingStep {

        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return Collections.singleton(JaggerContext.class);
        }

        private void createSanitizerModule(Filer filer, Messager messager, Element originalClass) {
            messager.printMessage(Kind.NOTE, "Creating sanitizer for " + originalClass); // SCOTT DEBUG ONLY
            TypeSpec.Builder sanitizerModule = TypeSpec.classBuilder(sanitizerName(originalClass))
                .addAnnotation(dagger.Module.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            originalClass.getEnclosedElements().stream().filter(e -> e
                .getKind().equals(METHOD)).forEach(element ->
                {
                    ExecutableElement method = (ExecutableElement) element;

                    // Create a provider method name of the form providesTypeName
                    String methodName = "provides" + getSimpleName(method.getReturnType());

                    // Determine the type of the method parameter (we'll add the Raw)
                    TypeName paramType = TypeName.get(method.getReturnType());

                    ParameterSpec providerParam = ParameterSpec.builder(paramType, "rawValue")
                        .addAnnotation(Raw.class)
                        .build();

                    MethodSpec.Builder sanitizeMethod = MethodSpec.methodBuilder(methodName)
                        .addAnnotation(Provides.class)
                        .addParameter(providerParam);
                    for (String dep : allLabels) {
                        TypeSpec qualifier = TypeSpec.annotationBuilder("Qualify" + dep)
                            .addAnnotation(Qualifier.class)
                            .build();
                        ClassName qualifierName = ClassName.get(getEnclosingPackage(originalClass).getQualifiedName().toString(),
                            sanitizerName(originalClass), "Qualify" + dep);
                        sanitizerModule.addType(qualifier);

                        ParameterSpec labelParam = ParameterSpec.builder(TypeName.BOOLEAN, dep)
                            .addAnnotation(qualifierName)
                            .build();
                        sanitizeMethod.addParameter(labelParam);

                        sanitizerModule.addMethod(MethodSpec.methodBuilder("provides" + dep)
                            .addAnnotation(Provides.class)
                            .addAnnotation(qualifierName)
                            .returns(TypeName.BOOLEAN)
                            .addStatement("return false")
                            .build());
                    }
                    sanitizerModule.addMethod(sanitizeMethod
                        .addStatement("return $N.sanitize()", providerParam)
                        .returns(paramType)
                        .build());
                }
            );

            // Write the faceted version of the class into a java source file
            String originalPackage = getEnclosingPackage(originalClass).toString();
            JavaFile outputFile = JavaFile.builder(originalPackage, sanitizerModule.build())
                .build();
            writeToFile(outputFile);
        }

        private void createComponent(Filer filer, Messager messager, Element originalClass) {
            // Create a context component interface, and inner Builder interface
            String packageName = getEnclosingPackage(originalClass).getQualifiedName().toString();
            String contextComponentName = contextComponentName(originalClass);
            ClassName contextBuilderName =
                ClassName.get(packageName, contextComponentName, "Builder");

            TypeSpec.Builder contextComponent =
                TypeSpec.interfaceBuilder(contextComponentName)
                .addSuperinterface(TypeName.get(originalClass.asType()));
            TypeSpec.Builder contextBuilder = TypeSpec.interfaceBuilder(contextBuilderName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(Component.Builder.class);


            AnnotationSpec componentAnnotation = AnnotationSpec.builder(Component.class)
                .addMember("modules", sanitizerName(originalClass) + ".class")
                .build();
            contextComponent.addAnnotation(componentAnnotation);

            for (ExecutableElement method : instanceMethods(originalClass)) {
                TypeName originalReturnType = TypeName.get(method.getReturnType());
                ParameterSpec rawParam =
                    ParameterSpec.builder(originalReturnType, method.getSimpleName().toString())
                    .addAnnotation(Raw.class)
                    .build();

                String rawMethodName = rawMethodName(method);
                MethodSpec builderMethod = MethodSpec.methodBuilder(rawMethodName)
                    .addAnnotation(BindsInstance.class)
                    .addParameter(rawParam)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(contextBuilderName)
                    .build();
                contextBuilder.addMethod(builderMethod);
            }

            // add a build method for the inner Builder interface
            MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(TypeName.get(originalClass.asType()))
                .build();
            contextBuilder.addMethod(buildMethod);
            // add the builder itself to the component
            contextComponent.addType(contextBuilder.build());

            JavaFile contextComponentFile =
                JavaFile.builder(packageName, contextComponent.build()).build();
            writeToFile(contextComponentFile);
        }

        @Override
        public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            Filer filer = processingEnv.getFiler();
            Messager messager = processingEnv.getMessager();
            messager.printMessage(Kind.NOTE, "Processing " + elementsByAnnotation);

            for (Element originalClass : elementsByAnnotation.get(JaggerContext.class)) {
                createSanitizerModule(filer, messager, originalClass);
                createComponent(filer, messager, originalClass);
            }

            return Collections.emptySet();
        }
    }

    private static PackageElement getEnclosingPackage(Element originalClass) {
        Preconditions.checkArgument(CLASS_LIKE_ELEMENT_KINDS.contains(originalClass.getKind()));
        Element enclosingElement = originalClass.getEnclosingElement();
        while (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            enclosingElement = enclosingElement.getEnclosingElement();
        }
        return (PackageElement) enclosingElement;
    }

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return Arrays.asList(new CollectLabels(), new CreateSanitizerModules());
    }

    @Override
    protected void postRound(RoundEnvironment roundEnv) {
        super.postRound(roundEnv);
        if (roundEnv.processingOver()) {
            processingEnv.getMessager().printMessage(Kind.NOTE,
                "Labels: " + allLabels);
            processingEnv.getMessager().printMessage(Kind.NOTE,
                "Label Dependencies: " + labelDeps);
        }
    }

    private static TypeElement asElement(Types typeUtils, TypeMirror mirror) {
        return (TypeElement) typeUtils.asElement(mirror);
    }
}
