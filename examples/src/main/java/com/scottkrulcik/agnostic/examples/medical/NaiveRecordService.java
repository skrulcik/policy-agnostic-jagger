package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.copyOf;

@Module
@Singleton
final class NaiveRecordService {

    @Provides
    @Singleton
    HistoryEndpoint historyEndpoint(DataStore data) {
        return new HistoryEndpoint() {
            @Override
            public Response handleRequest(Request request) {
                Person patient = request.providesPatient();
                Set<Record> history = data.filter(Record.class, r -> r.patient().equals(patient));
                return HistoryEndpoint.Response.create(copyOf(history));
            }
        };
    }

    @Provides
    SearchEndpoint searchEndpoint(DataStore data) {
        return new SearchEndpoint() {
            @Override
            public Response handleRequest(Request request) {
                String condition = request.providesCondition();
                Set<Record> matchingRecords = data.filter(Record.class, r -> r.condition().equals(condition));
                Set<Person> matchingPatients = matchingRecords.stream().map(Record::patient).collect(Collectors.toSet());
                return Response.create(copyOf(matchingPatients));
            }
        };
    }

}
