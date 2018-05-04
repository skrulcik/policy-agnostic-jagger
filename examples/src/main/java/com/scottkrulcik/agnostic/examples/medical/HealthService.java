package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.collect.ImmutableSet;
import dagger.BindsInstance;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.scottkrulcik.agnostic.examples.medical.Model.Condition;
import static com.scottkrulcik.agnostic.examples.medical.Model.Doctor;
import static com.scottkrulcik.agnostic.examples.medical.Model.Patient;
import static com.scottkrulcik.agnostic.examples.medical.Model.Person;
import static com.scottkrulcik.agnostic.examples.medical.Model.Record;

/**
 * Service definition for a simple EMR management/analysis tool. This class has a lot of boilerplate,
 * but its goals are to do the following:
 *
 * <ol>
 *     <li>Define available endpoints</li>
 *     <li>Define Request and Response types</li>
 *     <li>Define Components/graphs for each endpoint</li>
 *     <li>Define a Backend interface to swap different backends.</li>
 * </ol>
 */
final class HealthService {
    private final Provider<HistoryEndpoint.Builder> history;
    private final Provider<SearchEndpoint.Builder> search;

    @Inject
    HealthService(Provider<HistoryEndpoint.Builder> history, Provider<SearchEndpoint.Builder> search) {
        this.history = history;
        this.search = search;
    }

    interface HistoryEndpoint {
        ImmutableSet<Record> history();
        interface Builder {
            @BindsInstance @Doctor
            Builder setDoctor(Person request);
            @BindsInstance @Patient
            Builder setPatient(Person request);
            HistoryEndpoint run();
        }
    }

    interface SearchEndpoint {
        ImmutableSet<Person> patients();
        interface Builder {
            @BindsInstance @Doctor
            Builder setDoctor(Person request);
            @BindsInstance @Condition
            Builder setCondition(String condition);
            SearchEndpoint run();
        }
    }

    /**
     * Fetches the medical history for the given patient, consisting of all of their records.
     */
    HistoryEndpoint medicalHistory(Person doctor, Person patient) {
        return history.get()
            .setDoctor(doctor)
            .setPatient(patient)
            .run();
    }

    /**
     * Fetches the set of patients who are afflicted with a specific condition.
     */
    SearchEndpoint patientsWithCondition(Person doctor, String condition) {
        return search.get()
            .setDoctor(doctor)
            .setCondition(condition)
            .run();
    }
}
