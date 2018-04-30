package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.DataStore;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import static com.scottkrulcik.agnostic.examples.medical.Model.Doctor;
import static com.scottkrulcik.agnostic.examples.medical.Model.EMPTY_REC;
import static com.scottkrulcik.agnostic.examples.medical.Model.Patient;
import static com.scottkrulcik.agnostic.examples.medical.Model.Person;
import static com.scottkrulcik.agnostic.examples.medical.Model.REDACTED;
import static com.scottkrulcik.agnostic.examples.medical.Model.Record;

/**
 * Hard-coded version of the code that would be generated for the medical records application.
 */
final class HardCoded {

    // ----------------------------------------------------------------------------
    // Context modules (supply context information used by the policy)
    // ----------------------------------------------------------------------------

    @Module
    static final class DataStoreModule {

        private final DataStore dataStore;

        DataStoreModule(DataStore store) {
            dataStore = store;
        }

        @Provides
        @Singleton
        DataStore provideDataStore() {
            return dataStore;
        }
    }

    @Module
    static final class HistoryRequestModule {
        private final Person doctor;
        private final Person patient;

        HistoryRequestModule(Person doctor, Person patient) {
            this.doctor = doctor;
            this.patient = patient;
        }

        @Provides
        @Patient
        Model.Person providePatient() {
            return patient;
        }

        @Provides
        @Doctor
        Model.Person provideDoctor() {
            return doctor;
        }
    }

    @Module
    static final class ConsentFormModule {
        private final Model.ConsentForm rawConsentForm;

        ConsentFormModule(Model.ConsentForm form) {
            rawConsentForm = form;
        }

        @Qualifier
        @interface FormVisibleQualifier {}

        @Provides
        @FormVisibleQualifier
        boolean providesFormVisibleQualifier(@Doctor Person doctor) {
            return rawConsentForm.formVisible(doctor);
        }

        @Provides
        ConsentForm providesConsentForm(@FormVisibleQualifier boolean formVisible) {
            return formVisible ? rawConsentForm : null;
        }
    }

    // ----------------------------------------------------------------------------
    // Factory classes, which always create safe objects
    // ----------------------------------------------------------------------------

    // TODO(skrulcik): add scopes to factory injections
    public static final class RecordFactory {

        private final @Doctor Person requester;
        private final HardCoded.ConsentFormQuery.Builder query;

        @Inject
        RecordFactory(@Doctor Person requester, HardCoded.ConsentFormQuery.Builder query) {
            this.requester = requester;
            this.query = query;
        }

        public Record wrap(Record raw) {
            return new FacetedRecord(raw, requester, query);
        }

        private static final class FacetedRecord extends Record {
            private final Record delegate;
            private final @Doctor Person requester;
            private final HardCoded.ConsentFormQuery.Builder query;

            public FacetedRecord(Record delegate, @Doctor Person requester, HardCoded.ConsentFormQuery.Builder query) {
                this.delegate = delegate;
                this.requester = requester;
                this.query = query;
            }

            @Override
            public Person patient() {
                return delegate.patient();
            }

            @Override
            public Person provider() {
                return delegate.provider();
            }

            @Override
            public String condition() {
                if (psychNoteRule(requester, query)) {
                    return delegate.condition();
                }
                return REDACTED;
            }

            @Override
            public boolean isPsychNote() {
                return delegate.isPsychNote();
            }
        }
    }

    public static final class ConsentFormFactory {
        private final @Doctor Person doctor;

        public ConsentFormFactory(Person doctor) {
            this.doctor = doctor;
        }

        public final ConsentForm wrap(ConsentForm raw) {
            return new FacetedConsentForm(raw, doctor);
        }

        private static class FacetedConsentForm extends ConsentForm {

            private final ConsentForm delegate;
            private final @Doctor Person doctor;

            private FacetedConsentForm(ConsentForm delegate, Person doctor) {
                this.delegate = delegate;
                this.doctor = doctor;
            }

            public Record record() {
                if (delegate.formVisible(doctor)) {
                    return delegate.record();
                }
                return EMPTY_REC;
            }

            public Person provider() {
                return delegate.provider();
            }
        }
    }

    // ----------------------------------------------------------------------------
    // Query modules - used during policy evaluation
    // ----------------------------------------------------------------------------


    @Module
    static final class QueryWrapperModel {
        @Provides
        Set<ConsentForm> provideQueryResult(DataStore dataStore, ConsentFormFactory factory, Predicate<ConsentForm> filter) {
            return dataStore.filter(ConsentForm.class, filter).stream()
                .map(factory::wrap)
                .filter(filter)
                .collect(Collectors.toSet());
        }
    }

    @Subcomponent(modules = QueryWrapperModel.class)
    interface ConsentFormQuery {
        Set<Model.ConsentForm> consentForms();

        @Subcomponent.Builder
        interface Builder {
            @BindsInstance
            Builder matching(Predicate<ConsentForm> filter);
            ConsentFormQuery build();
        }
    }

}
