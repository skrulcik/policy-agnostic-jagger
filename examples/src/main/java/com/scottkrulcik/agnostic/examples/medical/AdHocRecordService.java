package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.base.Preconditions;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.copyOf;

@Module
@Singleton
final class AdHocRecordServiceModule {

    private boolean canSharePsychNote(DataStore data, Record rec, Person requester) {
        Preconditions.checkArgument(rec.isPsychNote());
        if (rec.provider().equals(requester)) {
            return true;
        }
        Predicate<ConsentForm> matchesRecordAndRequester =
            cf -> cf.record().equals(rec) && cf.provider().equals(requester);

        Set<ConsentForm> signedForms = data.filter(ConsentForm.class, matchesRecordAndRequester);
        return !signedForms.isEmpty();

    }

    @Provides
    @Singleton
    HistoryEndpoint historyEndpoint(DataStore data) {
        return new HistoryEndpoint() {
            @Override
            public Response handleRequest(Request request) {
                Person patient = request.providesPatient();
                Person doctor = request.providesDoctor();
                Set<Record> history = data.filter(Record.class, r ->
                    r.patient().equals(patient) && (!r.isPsychNote() || canSharePsychNote(data, r, doctor))
                );
                return HistoryEndpoint.Response.create(copyOf(history));
            }
        };
    }

    @Provides
    @Singleton
    SearchEndpoint searchEndpoint(DataStore data) {
        return new SearchEndpoint() {
            @Override
            public Response handleRequest(Request request) {
                String condition = request.providesCondition();
                Person doctor = request.providesDoctor();
                Set<Record> matchingRecords = data.filter(Record.class, r ->
                    r.condition().equals(condition) && (!r.isPsychNote() || canSharePsychNote(data, r, doctor)));
                Set<Person> matchingPatients = matchingRecords.stream().map(Record::patient).collect(Collectors.toSet());
                return Response.create(copyOf(matchingPatients));
            }
        };
    }
}
