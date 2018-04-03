package com.scottkrulcik.agnostic.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.scottkrulcik.agnostic.annotations.Default;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.scottkrulcik.agnostic.annotations.Restriction;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.checkState;
import static com.scottkrulcik.agnostic.processor.AnnotationUtils.getAnnotationMirror;

final class CollectLabels implements BasicAnnotationProcessor.ProcessingStep {

    // TODO(skrulcik): Consider how multiple rounds should interact here
    private final Set<String> allLabels = new HashSet<>();
    private final MutableGraph<String> labelDeps = GraphBuilder.directed().allowsSelfLoops(true).build();
    private final Map<String, PolicyRule.Builder> policyRules = new HashMap<>();

    private final ProcessingEnvironment processingEnv;

    private static final String LABEL_FIELD = "value";
    private static final String DEPENDENCIES_FIELD = "dependencies";

    CollectLabels(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(Restrict.class, Restriction.class, Default.class);
    }

    @Override
    public Set<? extends Element> process(
        SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

        Set<Element> elements = elementsByAnnotation.get(Restrict.class);
        assert elements.size() == elementsByAnnotation.size();
        Set<Element> unprocessable = new HashSet<>();

        for (Element accessor : elements) {
            AnnotationMirror restrictAnnotation = getAnnotationMirror(accessor, Restrict.class);
            if (restrictAnnotation == null) {
                unprocessable.add(accessor);
                continue;
            }
            // Get the raw versions of annotation values, and check before casting
            AnnotationValue rawLabel = getAnnotationValue(restrictAnnotation, LABEL_FIELD);
            if (rawLabel == null) {
                unprocessable.add(accessor);
                continue;
            }

            String label = (String) rawLabel.getValue();
            allLabels.add(label);
            // TODO(skrulcik): Handle labels better between runs, this warning is falsely triggered
            if (policyRules.containsKey(label)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Attempting to re-use the same label (" + label + "), behavior may be undefined.");
            } else {
                policyRules.put(label, PolicyRule.builder(label).setAccessor(accessor));
            }

        }

        for (Element predicate : elementsByAnnotation.get(Restriction.class)) {
            checkState(isRestrictionValid(predicate), "Restriction cannot be applied here", processingEnv);
            AnnotationMirror restrictionAnnotation = getAnnotationMirror(predicate, Restriction.class);
            if (restrictionAnnotation == null) {
                unprocessable.add(predicate);
                continue;
            }
            // Get the raw versions of annotation values, and check before casting
            AnnotationValue rawLabel = getAnnotationValue(restrictionAnnotation, LABEL_FIELD);
            String label = (String) rawLabel.getValue();
            PolicyRule.Builder rule = policyRules.get(label);
            if (rule == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Restriction label " + label + " does not guard any fields.");
            } else {
                rule.setPredicate(predicate);
            }
            // TODO(skrulcik): investigate why this is a NPE instead of proper default of {}
            AnnotationValue rawDeps = getAnnotationValue(restrictionAnnotation, DEPENDENCIES_FIELD);
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

        for (Element field : elementsByAnnotation.get(Default.class)) {
            checkState(isDefaultValid(field),
                "@Default only applies to public static fields. Not " + field.getSimpleName(),
                processingEnv);
            AnnotationMirror restrictionAnnotation = getAnnotationMirror(field, Default.class);
            if (restrictionAnnotation == null) {
                unprocessable.add(field);
                continue;
            }
            // Get the raw versions of annotation values, and check before casting
            AnnotationValue rawLabel = getAnnotationValue(restrictionAnnotation, LABEL_FIELD);
            String label = (String) rawLabel.getValue();
            PolicyRule.Builder rule = policyRules.get(label);
            if (rule == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Default value for \"" + label + "\" does not have a corresponding field.");
            } else {
                rule.setSafeDefault(field);
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

    public ImmutableGraph<PolicyRule> getPolicyRules() {
        // Create a new map between label strings and fully-formed policy rules (which can't be
        // built until their underlying fields have been properly set). Shadow to avoid naming
        // confusions.
        Map<String, PolicyRule> policyRules = new HashMap<>(this.policyRules.size());
        this.policyRules.forEach((k,v) -> policyRules.put(k, v.build()));

        // Create a new graph that combines the concrete rules and the dependencies from labelDeps
        MutableGraph<PolicyRule> policyDeps =
            GraphBuilder.directed()
                .allowsSelfLoops(true)
                .expectedNodeCount(labelDeps.nodes().size())
                .build();

        // Add all of the policy rules to the graph first, then fill in the dependencies
        policyRules.values().forEach(policyDeps::addNode);
        for (String label : policyRules.keySet()) {
            checkState(policyRules.containsKey(label),
                "No policy rule for label \"" + label + "\"",
                processingEnv);
            PolicyRule labelRule = policyRules.get(label);
            for (String dependency : labelDeps.successors(label)) {
                checkState(policyRules.containsKey(dependency),
                    "No policy rule for label \"" + dependency + "\"",
                    processingEnv);
                policyDeps.putEdge(labelRule, policyRules.get(dependency));
            }
        }
        return ImmutableGraph.copyOf(policyDeps);
    }

    /**
     * Validates that {@link Restriction} is only applied to public, non-static methods.
     */
    private static boolean isRestrictionValid(Element e) {
        return e.getKind().equals(ElementKind.METHOD)
            && e.getModifiers().contains(Modifier.PUBLIC)
            && e.getModifiers().contains(Modifier.FINAL)
            && !e.getModifiers().contains(Modifier.STATIC);
    }

    /**
     * Checks that {@link Default} is only applied to public, static fields.
     */
    private static boolean isDefaultValid(Element e) {
        return e.getKind().equals(ElementKind.FIELD)
            && e.getModifiers().contains(Modifier.PUBLIC)
            && e.getModifiers().contains(Modifier.FINAL)
            && e.getModifiers().contains(Modifier.STATIC);
    }


    @AutoValue
    static abstract class PolicyRule {
        abstract String label();

        abstract Element accessor();

        abstract Element predicate();

        abstract Element safeDefault();

        static Builder builder(String label) {
            return new AutoValue_CollectLabels_PolicyRule.Builder().setLabel(label);
        }

        @AutoValue.Builder
        static abstract class Builder {
            abstract Builder setLabel(String label);

            abstract Builder setAccessor(Element accessor);

            abstract Builder setPredicate(Element predicate);

            abstract Builder setSafeDefault(Element safeDefault);

            abstract PolicyRule build();
        }

    }

}


