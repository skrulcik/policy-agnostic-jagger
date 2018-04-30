package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.DataStore;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Hard-coded version of the code that would be generated for the medical records application.
 */
final class HardCoded {

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
        private final Model.Person doctor;
        private final Model.Person patient;

        HistoryRequestModule(Model.Person doctor, Model.Person patient) {
            this.doctor = doctor;
            this.patient = patient;
        }

        @Provides
        @Model.Patient
        Model.Person providePatient() {
            return patient;
        }

        @Provides
        @Model.Doctor
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
        boolean providesFormVisibleQualifier(@Model.Doctor Model.Person doctor) {
            return rawConsentForm.formVisible(doctor);
        }

        @Provides
        Model.ConsentForm providesConsentForm(@FormVisibleQualifier boolean formVisible) {
            return formVisible ? rawConsentForm : null;
        }
    }

    interface Query<T> {
        Set<T> get();
    }

    interface Sanitizer<T> {
        T sanitize(T instance);
    }

    @Module
    static final class ForRecordModule {
        @Provides
        Set<Model.ConsentForm> provideQueryResult(DataStore dataStore, Sanitizer<Model.ConsentForm> sanitizer, Predicate<Model.ConsentForm> filter) {
            return dataStore.filter(Model.ConsentForm.class, filter).stream()
                .map(sanitizer::sanitize)
                .filter(filter)
                .collect(Collectors.toSet());
        }
    }

    @Subcomponent(modules = ForRecordModule.class)
    interface ConsentFormQuery extends Query<Model.ConsentForm> {
        @Override
        Set<Model.ConsentForm> get();

        @Subcomponent.Builder
        interface Builder {
            @BindsInstance
            Builder matching(Predicate<Model.ConsentForm> filter);
            ConsentFormQuery build();
        }
    }


    static final class ConsentFormSanitizer implements Sanitizer<Model.ConsentForm> {
        private final Model.Person doctor;

        ConsentFormSanitizer(@Model.Doctor Model.Person doctor) {
            this.doctor = doctor;
        }

        @Override
        public Model.ConsentForm sanitize(Model.ConsentForm instance) {
            if (instance.formVisible(doctor)) {
                return instance;
            }
            return instance.sanitizeRecord();
        }
    }

    @Module
    static final class RecordSanitizerModule {
        final Model.Record baseRecord;

        RecordSanitizerModule(Model.Record record) {
            baseRecord = record;
        }

    }

}
