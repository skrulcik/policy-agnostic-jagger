package com.scottkrulcik.agnostic.examples.medical.sampledata;

import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

/**
 * Simple data set with 2 patients, 3 doctors, 4 normal records, and 2 psych note records.
 *
 * <p>The implementation is a weird sort of delegate-singleton, even though most data sets should be
 * instances. The fields are package-private, so they may be exposed publicly in a testing class see
 * {@code DataSet1_TestAccess} to see how the existence of these fields makes it possible to access
 * them for testing. The singleton is done with a static field rather than an enum because of
 * related constraints.
 */
public class DataSet1 extends DataSet {
    public static final DataSet1 INSTANCE = new DataSet1();

    final Person alice = Person.create("alice");
    final Person bob = Person.create("bob");
    final Person docC = Person.create("docC");
    final Person docD = Person.create("docD");
    final Person docE = Person.create("docE");

    final Record rec1 = Record.create(alice, docC, "flu", false);
    final Record rec2 = Record.create(alice, docC, "broken bone", false);
    final Record rec3 = Record.create(bob, docC, "bronchitis", false);
    final Record rec4 = Record.create(bob, docC, "flu", false);

    final Record psychRec1 = Record.create(alice, docD, "broken heart", true);
    final Record psychRec2 = Record.create(bob, docD, "drug addiction", true);

    final ConsentForm consentPsych2 = ConsentForm.create(psychRec2, docC);

    private final DataSet delegate =
        DataSet.create(ImmutableSet.of(alice, bob, docC, docD, docE),
            ImmutableSet.of(rec1, rec2, rec3, rec4, psychRec1, psychRec2),
            ImmutableSet.of(consentPsych2));

    private DataSet1() {}

    @Override
    public ImmutableSet<Person> people() {
        return delegate.people();
    }

    @Override
    public ImmutableSet<Record> records() {
        return delegate.records();
    }

    @Override
    public ImmutableSet<ConsentForm> consentForms() {
        return delegate.consentForms();
    }

    @Override
    public boolean equals(Object other) {
        return delegate.equals(other);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
