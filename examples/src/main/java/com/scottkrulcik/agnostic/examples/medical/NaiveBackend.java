package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

import javax.inject.Provider;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.scottkrulcik.agnostic.examples.medical.Model.Condition;
import static com.scottkrulcik.agnostic.examples.medical.Model.Patient;

@Module(subcomponents = {NaiveBackend.History.class, NaiveBackend.Search.class})
final class NaiveBackend {

    @Module
    static final class HistoryModule {
        @Provides
        static ImmutableSet<Record> history(DataStore data, @Patient Person patient) {
            return copyOf(data.filter(Record.class, r -> r.patient().equals(patient)));
        }
    }

    @Module
    static final class SearchModule {
        @Provides
        static ImmutableSet<Person> searchResults(DataStore data, @Condition String condition) {
            Set<Record> matchingRecords = data.filter(Record.class, r -> r.condition().equals(condition));
            Set<Person> matchingPatients = matchingRecords.stream().map(Record::patient).collect(Collectors.toSet());
            return copyOf(matchingPatients);
        }
    }

    @Subcomponent(modules = HistoryModule.class)
    interface History extends HealthService.HistoryEndpoint {
        @Subcomponent.Builder
        interface Builder extends HealthService.HistoryEndpoint.Builder {
        }
    }

    @Subcomponent(modules = SearchModule.class)
    interface Search extends HealthService.SearchEndpoint {
        @Subcomponent.Builder
        interface Builder extends HealthService.SearchEndpoint.Builder {
        }
    }

    @Provides
    static HealthService.HistoryEndpoint.Builder polymorphicHistory(Provider<History.Builder> provider) {
        return provider.get();
    }

    @Provides
    static HealthService.SearchEndpoint.Builder polymorphicSearch(Provider<Search.Builder> provider) {
        return provider.get();
    }
}
