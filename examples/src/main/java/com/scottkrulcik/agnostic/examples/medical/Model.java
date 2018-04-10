package com.scottkrulcik.agnostic.examples.medical;

import com.google.auto.value.AutoValue;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.scottkrulcik.agnostic.annotations.Restriction;
import com.scottkrulcik.agnostic.annotations.SafeDefault;

import javax.inject.Qualifier;

final class Model {

    @SafeDefault("psychNoteRule")
    public static final String REDACTED = "REDACTED";

    @SafeDefault("patient")
    public static final Person ANON_PATIENT = Person.create("Anonymous");

    @SafeDefault("provider")
    public static final Person ANON_DOC = Person.create("Anonymous");

    @SafeDefault("record")
    public static final Record EMPTY_REC = Record.create(ANON_PATIENT, ANON_DOC, REDACTED, false);

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

        @Restrict("psychNoteRule")
        abstract String condition();

        abstract boolean isPsychNote();

        abstract Record withCondition(String condition);

        Record sanitizeCondition() {
            return withCondition(REDACTED);
        }

        static Record create(Person patient, Person provider, String condition, boolean isPsychNote) {
            return new AutoValue_Model_Record(patient, provider, condition, isPsychNote);
        }

        @Restriction("psychNoteRule")
        public final boolean psychNoteRule(@Doctor Person requester, HardCoded.ConsentFormQuery.Builder query) {
            HardCoded.ConsentFormQuery consentFormQuery =
                query.matching(cf -> cf.provider().equals(requester) && cf.record().equals(this)).build();
            return !this.isPsychNote()
                || requester.equals(this.provider())
                || !consentFormQuery.get().isEmpty();
        }
    }


    // TODO(skrulcik): allow patients to see it too
    @AutoValue
    static abstract class ConsentForm {
        @Restrict("record")
        public abstract Record record();

        public abstract Person provider();

        abstract ConsentForm withRecord(Record record);

        ConsentForm sanitizeRecord() {
            return withRecord(EMPTY_REC);
        }

        @Restriction("record")
        public final boolean formVisible(@Doctor Person doctor) {
            return provider().equals(doctor);
        }

        /**
         * Creates a consent form object stating that {@code patient} permits {@code provider} to
         * view record {@code record}.
         */
        static ConsentForm create(Record record, Person provider) {
            return new AutoValue_Model_ConsentForm(record, provider);
        }
    }

    @Qualifier
    @interface Patient {}

    @Qualifier
    @interface Doctor {}

    private Model() {
    }
}
