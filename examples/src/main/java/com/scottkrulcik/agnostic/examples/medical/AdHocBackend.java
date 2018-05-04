package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.scottkrulcik.agnostic.examples.medical.Model.*;

@Module(subcomponents = {AdHocBackend.History.class, AdHocBackend.Search.class})
final class AdHocBackend {

    private static boolean canSharePsychNote(DataStore data, Record rec, Person requester) {
        Preconditions.checkArgument(rec.isPsychNote());
        if (rec.provider().equals(requester)) {
            return true;
        }
        Predicate<ConsentForm> matchesRecordAndRequester =
            cf -> cf.record().equals(rec) && cf.provider().equals(requester);

        Set<ConsentForm> signedForms = data.filter(ConsentForm.class, matchesRecordAndRequester);
        return !signedForms.isEmpty();

    }

    @Module
    static final class HistoryModule {
        @Provides
        static ImmutableSet<Record> history(DataStore data, @Doctor Person doctor, @Patient Person patient) {
            Set<Record> history = data.filter(Record.class, r ->
                r.patient().equals(patient) && (!r.isPsychNote() || canSharePsychNote(data, r, doctor)));
            return copyOf(history);
        }
    }

    @Module
    static final class SearchModule {
        @Provides
        static ImmutableSet<Person> searchResults(DataStore data, @Doctor Person doctor, @Condition String condition) {
            Set<Record> matchingRecords = data.filter(Record.class, r ->
                r.condition().equals(condition) && (!r.isPsychNote() || canSharePsychNote(data, r, doctor)));
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

}
