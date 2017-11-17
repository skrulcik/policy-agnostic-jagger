package com.scottkrulcik.agnostic;

import dagger.Module;
import dagger.Provides;
import dagger.producers.Production;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All programs using Dagger Producers need an production executor module. This module should
 * provides the executor for all faceted execution to occur on.
 *
 * At the moment, we don't have well-defined concurrency semantics for policy agnostic
 * programming, so we only ever supply a single-threaded executor.
 *
 * TODO(skrulcik): Enforce that this is the only executor used for PAP.
 */
@Module
public final class ProductionExecutorModule {
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();

    @Provides
    @Production
    static Executor provideExecutor() {
        return exec;
    }
}
