package com.scottkrulcik.agnostic.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.SetMultimap;
import com.scottkrulcik.agnostic.annotations.JaggerContext;
import com.scottkrulcik.agnostic.annotations.Raw;
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
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Qualifier;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getEnclosingPackage;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.instanceMethods;
import static com.scottkrulcik.agnostic.processor.Naming.contextComponentName;
import static com.scottkrulcik.agnostic.processor.Naming.getSimpleName;
import static com.scottkrulcik.agnostic.processor.Naming.rawMethodName;
import static com.scottkrulcik.agnostic.processor.Naming.sanitizerName;

final class CreateSanitizerModules implements BasicAnnotationProcessor.ProcessingStep {

    private final CollectLabels collectLabels;
    private final Consumer<JavaFile> fileWriter;
    private final ProcessingEnvironment processingEnv;

    CreateSanitizerModules(ProcessingEnvironment processingEnv, CollectLabels collectLabels, Consumer<JavaFile> fileWriter) {
        this.collectLabels = collectLabels;
        this.fileWriter = fileWriter;
        this.processingEnv = processingEnv;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return Collections.singleton(JaggerContext.class);
    }

    private void createSanitizerModule(Filer filer, Messager messager, Element originalClass) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Creating sanitizer for " + originalClass); // SCOTT DEBUG ONLY
        TypeSpec.Builder sanitizerModule = TypeSpec.classBuilder(sanitizerName(originalClass))
            .addAnnotation(dagger.Module.class)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        originalClass.getEnclosedElements().stream().filter(e -> e
            .getKind().equals(ElementKind.METHOD)).forEach(element ->
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
                for (String dep : collectLabels.getAllLabels()) {
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
        fileWriter.accept(outputFile);
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
        fileWriter.accept(contextComponentFile);
    }

    @Override
    public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        Filer filer = processingEnv.getFiler();
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "Processing " + elementsByAnnotation);

        for (Element originalClass : elementsByAnnotation.get(JaggerContext.class)) {
            createSanitizerModule(filer, messager, originalClass);
            createComponent(filer, messager, originalClass);
        }

        return Collections.emptySet();
    }
}


