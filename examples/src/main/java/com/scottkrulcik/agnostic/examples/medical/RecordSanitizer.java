package com.scottkrulcik.agnostic.examples.medical;

import javax.inject.Inject;

import com.scottkrulcik.agnostic.examples.medical.HardCoded.ConsentFormQuery;
import com.scottkrulcik.agnostic.examples.medical.HardCoded.Sanitizer;

/**
 * Hard-coded version of the code that would be generated for the medical records application.
 */
final class RecordSanitizer implements Sanitizer<Model.Record> {
        private final Model.Person doctor;
        private final ConsentFormQuery.Builder queryBuilder;

        @Inject
        RecordSanitizer(@Model.Doctor Model.Person doctor, ConsentFormQuery.Builder builder) {
            this.doctor = doctor;
            queryBuilder = builder;
        }

        @Override
        public Model.Record sanitize(Model.Record instance) {
            if (instance.psychNoteRule(doctor, queryBuilder)) {
                return instance;
            }
            return instance.sanitizeCondition();
        }
    }


