package com.scottkrulcik.agnostic.examples.medical.sampledata;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

/**
 * A set of sample data used to populate a {@link DataStore} for testing or evaluation.
 */
@AutoValue
public abstract class DataSet {

    public abstract ImmutableSet<Person> people();
    public abstract ImmutableSet<Record> records();
    public abstract ImmutableSet<ConsentForm> consentForms();

    /**
     * Fills the given data store with the information contained in this data set.
     */
    public final void populateStore(DataStore store) {
        for (Person p : people()) {
            store.add(Person.class, p);
        }

        for (Record r : records()) {
            store.add(Record.class, r);
        }

        for (ConsentForm c : consentForms()) {
            store.add(ConsentForm.class, c);
        }
    }

    public static DataSet create(ImmutableSet<Person> people, ImmutableSet<Record> records, ImmutableSet<ConsentForm> consentForms) {
        return new AutoValue_DataSet(people, records, consentForms);
    }

}
