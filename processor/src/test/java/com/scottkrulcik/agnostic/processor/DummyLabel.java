package com.scottkrulcik.agnostic.processor;

import com.google.common.base.Predicates;
import com.scottkrulcik.agnostic.LabelDefinition;
import com.scottkrulcik.agnostic.ViewingContext;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class DummyLabel extends LabelDefinition<ViewingContext> {

        @Override
        public Set<Predicate<ViewingContext>> restrictions() {
            return Collections.singleton(Predicates.alwaysTrue());
        }
    }
