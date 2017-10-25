package com.scottkrulcik.agnostic.examples.history;

import com.google.common.util.concurrent.ListenableFuture;
import com.scottkrulcik.agnostic.Facet;
import com.scottkrulcik.agnostic.PolicyExecutorModule;
import dagger.producers.ProductionComponent;

@ProductionComponent(modules = {EmptyHistoryModule.class, PolicyExecutorModule.class},
    dependencies = User.class)
public interface AdComponent {
    ListenableFuture<Facet<SearchHistory>> history();
}
