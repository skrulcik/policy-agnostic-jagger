package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;
import com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.alice;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.bob;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.docC;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.docD;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.docE;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.psychRec1;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.psychRec2;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.rec1;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.rec2;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.rec3;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.rec4;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public final class MedicalExampleTest {
    HealthService service;

    @Before
    public void setUp() {
        service = DaggerRecordService_AdHoc.builder()
            .dataStoreModule(new DataStoreModule(DataSet1.INSTANCE))
            .build().healthService();
    }

    /**
     * Helper function to avoid boilerplate around making a history request.
     */
    private Set<Record> fetchHistory(Person doctor, Person patient) {
        return service.medicalHistory(doctor, patient).history();
    }

    /**
     * Helper function to avoid boilerplate around making a condition search.
     */
    private Set<Person> searchCondition(Person doctor, String condition) {
        return service.patientsWithCondition(doctor, condition).patients();
    }

    /**
     * Tests that both doctors should be able to see all normal medical records for patients.
     */
    @Test
    public void testNormalHistory() {
        for (Person doc : ImmutableSet.of(docC, docD)) {
            Set<Record> aliceRecords = fetchHistory(doc, alice);
            Set<Record> bobRecords = fetchHistory(doc, bob);

            assertThat(aliceRecords, hasItems(rec1, rec2));
            assertThat(bobRecords, hasItems(rec3, rec4));
        }
    }


    @Test
    public void testNoHistory() {
        assertThat(fetchHistory(docC, docC), empty());
        assertThat(fetchHistory(docC, docD), empty());
    }

    @Test
    public void testPsychNoteHistory() {
        // Consent signed, and author
        assertThat(fetchHistory(docD, bob), hasItem(psychRec2));
        // Consent unsigned, and author
        assertThat(fetchHistory(docD, alice), hasItem(psychRec1));

        // Consent signed, and not author
        assertThat(fetchHistory(docC, bob), hasItem(psychRec2));
        // Consent unsigned, and not author
        assertThat(fetchHistory(docC, alice), not(hasItem(psychRec1)));

        // Consent unsigned, and not author
        assertThat(fetchHistory(docE, bob), not(hasItem(psychRec2)));
        assertThat(fetchHistory(docE, alice), not(hasItem(psychRec1)));
    }

    @Test
    public void testNormalSearch() {
        Set<Person> fluPatients = searchCondition(docE, "flu");
        Set<Person> bronchitisPatients = searchCondition(docE, "bronchitis");

        assertThat(fluPatients, is(ImmutableSet.of(alice, bob)));
        assertThat(bronchitisPatients, is(ImmutableSet.of(bob)));
    }

    @Test
    public void testEmptySearch() {
        Set<Person> esotericDiseasePatients = searchCondition(docE, "esotericDisease");
        assertThat(esotericDiseasePatients, empty());
    }

    @Test
    public void testPsychNoteSearch() {
        assertThat(searchCondition(docC, "broken heart"), empty());
        assertThat(searchCondition(docC, "drug addiction"), is(singleton(bob)));

        assertThat(searchCondition(docD, "broken heart"), is(singleton(alice)));
        assertThat(searchCondition(docD, "drug addiction"), is(singleton(bob)));

        assertThat(searchCondition(docE, "broken heart"), empty());
        assertThat(searchCondition(docE, "drug addiction"), empty());
    }

}
