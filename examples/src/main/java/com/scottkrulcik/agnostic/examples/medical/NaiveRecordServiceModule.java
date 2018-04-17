package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

import java.util.Set;
import java.util.stream.Collectors;

final class NaiveRecordService extends RecordServiceServer {
    NaiveRecordService(DataStore data, MedicalContext context) {
        super(data, context);
    }

    @Override
    Set<Record> medicalHistory(Person patient) {
        return data.filter(Record.class, r -> r.patient().equals(patient));
    }

    @Override
    Set<Person> patientsWithCondition(String condition) {
        Set<Record> matchingRecords = data.filter(Record.class, r -> r.condition().equals(condition));
        Set<Person> matchingPatients = matchingRecords.stream().map(Record::patient).collect(Collectors.toSet());
        return matchingPatients;
    }
}
