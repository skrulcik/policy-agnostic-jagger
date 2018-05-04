package com.scottkrulcik.agnostic.examples.medical;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Top level component for the EMR management/analysis tool. Each component attempts to provide the
 * same service, but with different authorization techniques. In production, the usual setup would
 * only have a two top-level components: a {@code Test} configuration extending {@code Production}.
 *
 * <p>Authorization types:</p>
 * <ul>
 *     <li>{@code Naive} - No authorization</li>
 *     <li>{@code AdHocBackend} - Authorization is dispersed throughout</li>
 *     <li>{@code Jagger} - Authorization enforced by Jagger</li>
 * </ul>
 */
interface RecordService {

    interface HealthService {}

    @Component(modules = {
        DataStoreModule.class,
        NaiveBackend.class,
    })
    @Singleton
    interface Naive extends HealthService {
    }

    @Component(modules = {
        DataStoreModule.class,
        AdHocBackend.class
    })
    @Singleton
    interface AdHoc extends HealthService {
    }

    // TODO(skrulcik): implement JaggerRecordServiceModule
    // @Component
    // interface Jagger extends Naive { }
}
