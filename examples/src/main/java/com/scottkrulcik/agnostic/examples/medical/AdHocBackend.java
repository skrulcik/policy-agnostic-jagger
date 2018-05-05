package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.DAO;
import com.scottkrulcik.agnostic.annotations.ContextScope;
import com.scottkrulcik.agnostic.examples.medical.Model.Condition;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Doctor;
import com.scottkrulcik.agnostic.examples.medical.Model.Patient;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

import javax.inject.Provider;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.copyOf;

@Module(subcomponents = {AdHocBackend.History.class, AdHocBackend.Search.class})
final class AdHocBackend {

    private static boolean canSharePsychNote(DAO<ConsentForm> consentForms, Record rec, Person requester) {
        Preconditions.checkArgument(rec.isPsychNote());
        if (rec.provider().equals(requester)) {
            return true;
        }

        Predicate<ConsentForm> matchesRecordAndRequester =
            cf -> cf.record().equals(rec) && cf.provider().equals(requester);

        Set<ConsentForm> signedForms = consentForms.filter(matchesRecordAndRequester);
        return !signedForms.isEmpty();

    }

    @Module
    static final class HistoryModule {
        @Provides
        static ImmutableSet<Record> history(DAO<Record> records, DAO<ConsentForm> consentForms, @Doctor Person doctor, @Patient Person patient) {
            Set<Record> history = records.filter(
                r -> r.patient().equals(patient) && (!r.isPsychNote() || canSharePsychNote(consentForms, r, doctor)));
            return copyOf(history);
        }
    }

    @Module
    static final class SearchModule {
        @Provides
        static ImmutableSet<Person> searchResults(DAO<Record> records, DAO<ConsentForm> consentForms, @Doctor Person doctor, @Condition String condition) {
            Set<Record> matchingRecords = records.filter(r ->
                r.condition().equals(condition) && (!r.isPsychNote() || canSharePsychNote(consentForms, r, doctor)));
            Set<Person> matchingPatients = matchingRecords.stream().map(Record::patient).collect(Collectors.toSet());
            return copyOf(matchingPatients);
        }
    }

    @ContextScope
    @Subcomponent(modules = {HistoryModule.class, UnsafeDAOModule.class})
    interface History extends HealthService.HistoryEndpoint {
        @Subcomponent.Builder
        interface Builder extends HealthService.HistoryEndpoint.Builder {
        }
    }

    @ContextScope
    @Subcomponent(modules = {SearchModule.class, UnsafeDAOModule.class})
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
