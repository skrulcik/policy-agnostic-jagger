package com.scottkrulcik.agnostic.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

// TODO(skrulcik): Is it possible to use regular exceptions for intermediate steps, and
// report all exceptions in the top-level processor?
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(javax.annotation.processing.Processor.class)
public class RestrictionProcessor extends BasicAnnotationProcessor {

    // Delay initialization until initSteps, where processingEnv will be initialized
    private CollectLabels collectLabels;
    private CreateSanitizerModules createSanitizerModules;
    // Field to verify expected state conditions regarding the processing environment
    private ProcessingEnvironment env1;

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        checkState(collectLabels == null && createSanitizerModules == null, "processing steps should not be initialized before initSteps");

        if (env1 == null) {
            env1 = processingEnv;
        } else {
            checkState(env1 == processingEnv, "expected the same environment");
        }

        collectLabels = new CollectLabels(processingEnv);
        createSanitizerModules = new CreateSanitizerModules(processingEnv, collectLabels, this::writeToFile);
        return Arrays.asList(collectLabels, createSanitizerModules);
    }

    @Override
    protected void postRound(RoundEnvironment roundEnv) {
        super.postRound(roundEnv);
        if (roundEnv.processingOver()) {
            processingEnv.getMessager().printMessage(Kind.NOTE,
                "Labels: " + collectLabels.getAllLabels());
            processingEnv.getMessager().printMessage(Kind.NOTE,
                "Label Dependencies: " + collectLabels.getLabelDeps());
            processingEnv.getMessager().printMessage(Kind.NOTE,
                "Policy Dependencies: " + collectLabels.getPolicyRules());
        }
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
}
