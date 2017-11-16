package com.scottkrulcik.agnostic.examples.history;

import com.google.common.util.concurrent.ListenableFuture;
import com.scottkrulcik.agnostic.Facet;
import com.scottkrulcik.agnostic.ProductionExecutorModule;
import dagger.producers.ProductionComponent;

@ProductionComponent(modules = {EmptyHistoryModule.class, ProductionExecutorModule.class},
    dependencies = User.class)
public interface AdComponent {
    ListenableFuture<SearchHistory> history();
}
