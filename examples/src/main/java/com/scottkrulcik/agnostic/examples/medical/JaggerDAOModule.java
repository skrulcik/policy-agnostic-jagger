package com.scottkrulcik.agnostic.examples.medical;

import javax.inject.Singleton;

import com.scottkrulcik.agnostic.DAO;
import com.scottkrulcik.agnostic.annotations.ContextScope;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

import dagger.Module;
import dagger.Provides;

@Module
final class JaggerDAOModule {
    @Provides
    @ContextScope
    static DAO<ConsentForm> consentFormDAO(DataStore data, JaggerConsentForm.Factory sanitizingFactory) {
        return data.sanitizedDAO(ConsentForm.class, sanitizingFactory);
    }

    @Provides
    @ContextScope
    static DAO<Record> recordDAO(DataStore data, JaggerRecord.Factory sanitizingFactory) {
        return data.sanitizedDAO(Record.class, sanitizingFactory);
    }

    @Provides
    @Singleton
    static DAO<Person> personDAO(DataStore data) {
        return data.rawDAO(Person.class);
    }
}
