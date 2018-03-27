package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

import java.util.Set;

abstract class RecordService {
    final DataStore data;
    final MedicalContext context;

    RecordService(DataStore data, MedicalContext context) {
        this.data = data;
        this.context = context;
    }


    /**
     * Fetches the medical history for the given patient, consisting of all of their records.
     *
     * @param patient patient whose records must be fetched.
     * @return A set containing the (unordered) medical records pertaining to {@code patient}.
     */
    abstract Set<Record> medicalHistory(Person patient);


    /**
     * Fetches the set of patients who are afflicted with a specific condition.
     *
     * @param condition the condition to search for.
     * @return A set containing patients who have records indicating they were treated for
     * conditions in the past.
     */
    abstract Set<Person> patientsWithCondition(String condition);
}
