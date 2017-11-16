package com.scottkrulcik.agnostic.examples.history;

import com.scottkrulcik.agnostic.Facet;
import com.scottkrulcik.agnostic.High;
import com.scottkrulcik.agnostic.Low;
import dagger.producers.ProducerModule;
import dagger.producers.Produces;

@ProducerModule
public class EmptyHistoryModule {

    @Produces
    @Low
    static SearchHistory emptyHistory() {
        return new SearchHistory();
    }

    @Produces
    @High
    static SearchHistory userHistory(User user) {
        return user.history();
    }

    @Produces
    static SearchHistory produceHistoryFacet(@High SearchHistory high, @Low SearchHistory
        low) {
        return low;
    }
}
