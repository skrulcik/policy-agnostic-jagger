package com.scottkrulcik.agnostic.examples.history;

import dagger.producers.ProducerModule;
import dagger.producers.Produces;

@ProducerModule
public class EmptyHistoryModule {
    @Produces SearchHistory emptyHistory() {
        return new SearchHistory();
    }
}
