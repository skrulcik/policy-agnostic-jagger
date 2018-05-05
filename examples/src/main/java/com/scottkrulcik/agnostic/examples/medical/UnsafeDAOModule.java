package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.DAO;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

@Module
final class UnsafeDAOModule {
    @Provides
    @Reusable
    static DAO<ConsentForm> consentFormDAO(DataStore data) {
        return data.rawDAO(ConsentForm.class);
    }

    @Provides
    @Reusable
    static DAO<Record> recordDAO(DataStore data) {
        return data.rawDAO(Record.class);
    }

    @Provides
    @Reusable
    static DAO<Person> personDAO(DataStore data) {
        return data.rawDAO(Person.class);
    }
}
