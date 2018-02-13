package com.scottkrulcik.agnostic.data;

import com.scottkrulcik.agnostic.annotations.JaggerContext;
import com.scottkrulcik.agnostic.annotations.Raw;
import dagger.BindsInstance;
import dagger.Component;


@JaggerContext
@Component(modules = Jagger_SampleContextSanitizerModule.class)
public interface SampleContext {
    SampleData data();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder rawData(@Raw SampleData data);
        SampleContext build();
    }
}
