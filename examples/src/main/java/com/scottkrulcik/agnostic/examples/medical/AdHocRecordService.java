package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.base.Preconditions;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class AdHocRecordService extends RecordService {
    AdHocRecordService(DataStore data, MedicalContext context) {
        super(data, context);
    }

    private boolean canSharePsychNote(Record rec) {
        Preconditions.checkArgument(rec.isPsychNote());
        if (rec.provider().equals(context.requester())) {
            return true;
        }
        Predicate<ConsentForm> matchesRecordAndRequester =
            cf -> cf.record().equals(rec) && cf.provider().equals(context.requester());

        Set<ConsentForm> signedForms = data.filter(ConsentForm.class, matchesRecordAndRequester);
        return !signedForms.isEmpty();

    }

    @Override
    Set<Record> medicalHistory(Person patient) {
        return data.filter(Record.class, r ->
            r.patient().equals(patient) && (!r.isPsychNote() || canSharePsychNote(r))
        );
    }

    @Override
    Set<Person> patientsWithCondition(String condition) {
        Set<Record> matchingRecords = data.filter(Record.class, r ->
            r.condition().equals(condition) && (!r.isPsychNote() || canSharePsychNote(r)));
        Set<Person> matchingPatients = matchingRecords.stream().map(Record::patient).collect(Collectors.toSet());
        return matchingPatients;
    }
}
