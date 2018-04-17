package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Provides a singleton instance of {@link DataStore} populated with example medical records data.
 */
@Module
@Singleton
final class DataStoreModule {

    private final DataStore dataStore = new DataStore();

    DataStoreModule() {
    }

    DataStoreModule(DataSet existingData) {
        existingData.populateStore(dataStore);
    }

    @Provides
    @Singleton
    DataStore dataStore() {
        return dataStore;
    }

}
