package com.scottkrulcik.agnostic.examples.medical;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.Endpoint;
import dagger.Module;
import dagger.Provides;

abstract class HistoryEndpoint implements Endpoint<HistoryEndpoint.Request, HistoryEndpoint.Response> {

    @Module
    static final class Request implements Endpoint.Request {
        private final Model.Person doctor;
        private final Model.Person patient;

        Request(Model.Person doctor, Model.Person patient) {
            this.doctor = doctor;
            this.patient = patient;
        }

        @Provides
        @Model.Doctor
        Model.Person providesDoctor() {
            return doctor;
        }

        @Provides
        @Model.Patient
        Model.Person providesPatient() {
            return patient;
        }
    }

    @AutoValue
    static abstract class Response implements Endpoint.Response {
        abstract ImmutableSet<Model.Record> history();

        static Response create(ImmutableSet<Model.Record> history) {
            return new AutoValue_HistoryEndpoint_Response(history);
        }
    }
}
