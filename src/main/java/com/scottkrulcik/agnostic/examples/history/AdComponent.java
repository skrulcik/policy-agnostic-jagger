package com.scottkrulcik.agnostic.examples.history;

import dagger.producers.ProductionComponent;

@ProductionComponent
public interface AdComponent {
    SearchHistory history();
}
