package com.scottkrulcik.agnostic.examples.medical;

import com.google.auto.value.AutoValue;
import com.scottkrulcik.agnostic.DAO;
import com.scottkrulcik.agnostic.annotations.Restrict;
import com.scottkrulcik.agnostic.annotations.Restriction;
import com.scottkrulcik.agnostic.annotations.SafeDefault;

import javax.inject.Qualifier;

public final class Model {

    @SafeDefault("psychNoteRule")
    public static final String REDACTED = "REDACTED";

    @SafeDefault("patient")
    public static final Person ANON_PATIENT = Person.create("Anonymous");

    @SafeDefault("provider")
    public static final Person ANON_DOC = Person.create("Anonymous");

    @SafeDefault("record")
    public static final Record EMPTY_REC = Record.create(ANON_PATIENT, ANON_DOC, REDACTED, false);

    @AutoValue
    public static abstract class Person {
        abstract String name();

        public static Person create(String name) {
            return new AutoValue_Model_Person(name);
        }
    }

    @AutoValue
    public static abstract class Record {
        abstract Person patient();

        abstract Person provider();

        @Restrict("psychNoteRule")
        abstract String condition();

        abstract boolean isPsychNote();

        public static Record create(Person patient, Person provider, String condition, boolean isPsychNote) {
            return new AutoValue_Model_Record(patient, provider, condition, isPsychNote);
        }

        @Restriction("psychNoteRule")
        public final boolean psychNoteRule(@Doctor Person requester, DAO<ConsentForm> dao) {
            if (!this.isPsychNote() || requester.equals(this.provider()))
                return true;
            return !dao.filter(cf -> cf.provider().equals(requester) && cf.record().equals(this)).isEmpty();
        }
    }


    // TODO(skrulcik): allow patients to see it too
    @AutoValue
    public static abstract class ConsentForm {
        @Restrict("record")
        public abstract Record record();

        public abstract Person provider();

        @Restriction("record")
        public final boolean formVisible(@Doctor Person doctor) {
            return provider().equals(doctor);
        }

        /**
         * Creates a consent form object stating that {@code patient} permits {@code provider} to
         * view record {@code record}.
         */
        public static ConsentForm create(Record record, Person provider) {
            return new AutoValue_Model_ConsentForm(record, provider);
        }
    }

    @Qualifier
    @interface Patient {}

    @Qualifier
    @interface Doctor {}

    @Qualifier
    @interface Condition {}

    private Model() {
    }
}
