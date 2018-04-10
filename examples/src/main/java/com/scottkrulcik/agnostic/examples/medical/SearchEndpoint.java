package com.scottkrulcik.agnostic.examples.medical;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.scottkrulcik.agnostic.Endpoint;
import dagger.Module;
import dagger.Provides;

abstract class SearchEndpoint implements Endpoint<SearchEndpoint.Request, SearchEndpoint.Response> {

    // TODO(skrulcik): Request and Response types can have generated "Module" inner classes
    @Module
    static final class Request implements Endpoint.Request {
        private final Model.Person doctor;
        private final String condition;

        Request(Model.Person doctor, String condition) {
            this.doctor = doctor;
            this.condition = condition;
        }

        @Provides
        @Model.Condition
        String providesCondition() {
            return condition;
        }

        @Provides
        @Model.Doctor
        Model.Person providesDoctor() {
            return doctor;
        }
    }

    @AutoValue
    static abstract class Response implements Endpoint.Response {
        abstract ImmutableSet<Model.Person> patients();

        static Response create(ImmutableSet<Model.Person> history) {
            return new AutoValue_SearchEndpoint_Response(history);
        }
    }
}
