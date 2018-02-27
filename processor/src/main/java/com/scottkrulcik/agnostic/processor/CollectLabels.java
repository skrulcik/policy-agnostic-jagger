package com.scottkrulcik.agnostic.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.scottkrulcik.agnostic.annotations.Restrict;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationMirror;

final class CollectLabels implements BasicAnnotationProcessor.ProcessingStep {

    // TODO(skrulcik): Consider how multiple rounds should interact here
    private final Set<String> allLabels = new HashSet<>();
    private final MutableGraph<String> labelDeps = GraphBuilder.directed().allowsSelfLoops(true).build();
    private final ProcessingEnvironment processingEnv;

    private static final String LABEL_FIELD = "label";
    private static final String DEPENDENCIES_FIELD = "dependencies";

    CollectLabels(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

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
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "The following restricted fields were unprocessable: " + unprocessable);
        }
        return Collections.emptySet();
    }

    /**
     * Returns the set of all label strings in the entire compiled program.
     */
    public Set<String> getAllLabels() {
        return allLabels;
    }

    /**
     * Returns (possibly cyclic) directed graph representing the dependencies between label
     * guards.
     */
    public MutableGraph<String> getLabelDeps() {
        return labelDeps;
    }

}


