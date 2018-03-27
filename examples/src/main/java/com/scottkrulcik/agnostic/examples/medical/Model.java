package com.scottkrulcik.agnostic.examples.medical;

import com.google.auto.value.AutoValue;

final class Model {

    @AutoValue
    static abstract class Person {
        abstract String name();

        static Person create(String name) {
            return new AutoValue_Model_Person(name);
        }
    }

    @AutoValue
    static abstract class Record {
        abstract Person patient();

        abstract Person provider();

        abstract String condition();

        abstract boolean isPsychNote();

        static Record create(Person patient, Person provider, String condition, boolean isPsychNote) {
            return new AutoValue_Model_Record(patient, provider, condition, isPsychNote);
        }
    }

    @AutoValue
    static abstract class ConsentForm {
        abstract Record record();

        abstract Person provider();

        /**
         * Creates a consent form object stating that {@code patient} permits {@code provider} to
         * view record {@code record}.
         */
        static ConsentForm create(Record record, Person provider) {
            return new AutoValue_Model_ConsentForm(record, provider);
        }
    }

    private Model() {
    }
}
