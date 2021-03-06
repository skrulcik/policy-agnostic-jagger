#!python3
# Extra rules:
# - p0 has no psych records
# - data from DataSet1 is the same

import random

TOP = '''
package com.scottkrulcik.agnostic.examples.medical.sampledata;

import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

/** Test data generated by generate_tests.py. */
public class GeneratedDataSet extends DataSet {
    public static final GeneratedDataSet INSTANCE = new GeneratedDataSet();
'''
BOTTOM = '''

    private GeneratedDataSet() {}

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
'''

PERSON_FMT = '  final Person %s = Person.create("%s");';
RECORD_FMT = '  final Record {} = Record.create({}, {}, "{}", {});'
CONSENT_FMT = '  final ConsentForm {} = ConsentForm.create({}, {});'
DEFAULT_FMT = '''
    private final ImmutableSet<Person> people = ImmutableSet.of{};
    private final ImmutableSet<Record> records = ImmutableSet.of{};
    private final ImmutableSet<ConsentForm> forms = ImmutableSet.of{};
    private final DataSet delegate =
        DataSet.create(people, records, forms);
'''

PATH = "examples/src/main/java/com/scottkrulcik/agnostic/examples/medical/sampledata/GeneratedDataSet.java"
SEED = 1543
NUM_PATIENTS = 100
NUM_DOCS = 5
NUM_NORMAL_RECORDS = 1000
NUM_PSYCH_RECORDS = 250
NUM_CONDITIONS = 20
PSYCH_RECORD_DISCLOSURE = 0.3 # Probability that psych record is dislosed to everyone
NUM_PSYCH_CONDITIONS = 10

random.seed(a=SEED)

# Generate Patients
patients = ["p" + str(i) for i in range(NUM_PATIENTS)]

# Generate Doctors
doctors = ["d" + str(i) for i in range(NUM_DOCS)]

# Generate Records
normal_conditions = ["c" + str(i) for i in range(NUM_CONDITIONS)]
records = []
for i in range(NUM_NORMAL_RECORDS):
    recordName = "r" + str(i)
    patient = random.choice(patients)
    doctor = random.choice(doctors)
    condition = random.choice(normal_conditions)
    records.append((recordName, patient, doctor, condition, "false"))


psych_conditions = ["pc" + str(i) for i in range(NUM_PSYCH_CONDITIONS)]
psych_records = []
consent_forms = []
for i in range(NUM_PSYCH_RECORDS):
    recordName = "pr" + str(i)
    # Extra rules:
    # - p0 has no psych records
    patient = random.choice(patients[2:])
    doctor = random.choice(doctors)
    condition = random.choice(psych_conditions)
    psych_records.append((recordName, patient, doctor, condition, "true"))
    if random.random() < PSYCH_RECORD_DISCLOSURE:
        for doctor in doctors:
            consent_forms.append(("cf" + str(len(consent_forms)), psych_records[i][0], doctor))

# Hard-coded relationships, same as DataSet1
patients.extend(['alice', 'bob'])
doctors.extend(['docC', 'docD', 'docE'])
records.extend([
    ('rec1', 'alice', 'docC', "flu"         , 'false'),
    ('rec2', 'alice', 'docC', "broken bone" , 'false'),
    ('rec3', 'bob'  , 'docC', "bronchitis"  , 'false'),
    ('rec4', 'bob'  , 'docC', "flu"         , 'false'),
    ('psychRec1', 'alice', 'docD', "broken heart", 'true'),
    ('psychRec2', 'bob', 'docD', "drug addiction", 'true'),
])
consent_forms.append(('consentPsych2', 'psychRec2', 'docC'))


def formatPeople(people):
    lines = []
    for person in people:
        lines.append(PERSON_FMT % (person, person))
    return lines

def formatRecords(records):
    lines = []
    for r in records:
        lines.append(RECORD_FMT.format(*r))
    return lines

def formatConsentForms(forms):
    lines = []
    for f in forms:
        lines.append(CONSENT_FMT.format(*f))
    return lines

def getDefaults():
    personNames = (name for name in (patients + doctors))
    recordNames = (record[0] for record in (records + psych_records))
    formNames = (form[0] for form in consent_forms)
    return DEFAULT_FMT.format(tuple(personNames), tuple(recordNames), tuple(formNames)).replace("'","")


if __name__ == "__main__":
    with open(PATH, 'w') as output:
        output.write(TOP)
        output.writelines(formatPeople(patients))
        output.write('\n')
        output.writelines(formatPeople(doctors))
        output.write('\n')
        output.writelines(formatRecords(records))
        output.write('\n')
        output.writelines(formatRecords(psych_records))
        output.write('\n')
        output.writelines(formatConsentForms(consent_forms))
        output.write('\n')
        output.write(getDefaults())
        output.write('\n')
        output.write(BOTTOM)
