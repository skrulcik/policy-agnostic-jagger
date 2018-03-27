package com.scottkrulcik.agnostic.examples.medical;


import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class MedicalExampleTest {
    RecordService service;
    MutableContext testContext;

    private Person alice = Person.create("alice");
    private Person bob = Person.create("bob");
    private Person docC = Person.create("dr. charlie");
    private Person docD = Person.create("dr. diane");
    private Person docE = Person.create("dr. evil");

    private Record rec1 = Record.create(alice, docC, "flu", false);
    private Record rec2 = Record.create(alice, docC, "broken bone", false);
    private Record rec3 = Record.create(bob, docC, "bronchitis", false);
    private Record rec4 = Record.create(bob, docC, "flu", false);

    private Record psychRec1 = Record.create(alice, docD, "broken heart", true);
    private Record psychRec2 = Record.create(bob, docD, "drug addiction", true);

    private ConsentForm consentPsych2 = ConsentForm.create(psychRec2, docC);


    @Before
    public void setUp() {
        DataStore store = new DataStore();
        populateStore(store);
        testContext = new MutableContext();
        service = new AdHocRecordService(store, testContext);
    }

    private static final class MutableContext implements MedicalContext {
        private Model.Person requester = null;

        @Override
        public Model.Person requester() {
            return requester;
        }
    }

    private void populateStore(DataStore store) {
        for (Person p : Arrays.asList(alice, bob, docC, docD, docE)) {
            store.add(Person.class, p);
        }

        for (Record r : Arrays.asList(rec1, rec2, rec3, rec4, psychRec1, psychRec2)) {
            store.add(Record.class, r);
        }

        for (ConsentForm c : Arrays.asList(consentPsych2)) {
            store.add(ConsentForm.class, c);
        }
    }

    /**
     * Tests that both doctors should be able to see all normal medical records for patients.
     */
    @Test
    public void testNormalHistory() {
        for (Person requester : ImmutableSet.of(docC, docD)) {
            testContext.requester = requester;
            Set<Record> aliceRecords = service.medicalHistory(alice);
            Set<Record> bobRecords = service.medicalHistory(bob);

            assertThat(aliceRecords, hasItems(rec1, rec2));
            assertThat(bobRecords, hasItems(rec3, rec4));
        }
    }


    @Test
    public void testNoHistory() {
        testContext.requester = docC;
        assertThat(service.medicalHistory(docC), empty());
        assertThat(service.medicalHistory(docD), empty());
    }

    @Test
    public void testPsychNoteHistory() {
        testContext.requester = docD;
        // Consent signed, and author
        assertThat(service.medicalHistory(bob), hasItem(psychRec2));
        // Consent unsigned, and author
        assertThat(service.medicalHistory(alice), hasItem(psychRec1));

        testContext.requester = docC;
        // Consent signed, and not author
        assertThat(service.medicalHistory(bob), hasItem(psychRec2));
        // Consent unsigned, and not author
        assertThat(service.medicalHistory(alice), not(hasItem(psychRec1)));

        testContext.requester = docE;
        // Consent unsigned, and not author
        assertThat(service.medicalHistory(bob), not(hasItem(psychRec2)));
        assertThat(service.medicalHistory(alice), not(hasItem(psychRec1)));
    }

    @Test
    public void testNormalSearch() {
        testContext.requester = docE;
        Set<Person> fluPatients = service.patientsWithCondition("flu");
        Set<Person> bronchitisPatients = service.patientsWithCondition("bronchitis");

        assertThat(fluPatients, is(ImmutableSet.of(alice, bob)));
        assertThat(bronchitisPatients, is(ImmutableSet.of(bob)));
    }

    @Test
    public void testEmptySearch() {
        testContext.requester = docE;
        Set<Person> esotericDiseasePatients = service.patientsWithCondition("esotericDisease");
        assertThat(esotericDiseasePatients, empty());
    }

    @Test
    public void testPsychNoteSearch() {
        testContext.requester = docC;
        assertThat(service.patientsWithCondition("broken heart"), empty());
        assertThat(service.patientsWithCondition("drug addiction"), is(singleton(bob)));

        testContext.requester = docD;
        assertThat(service.patientsWithCondition("broken heart"), is(singleton(alice)));
        assertThat(service.patientsWithCondition("drug addiction"), is(singleton(bob)));

        testContext.requester = docE;
        assertThat(service.patientsWithCondition("broken heart"), empty());
        assertThat(service.patientsWithCondition("drug addiction"), empty());
    }

}
