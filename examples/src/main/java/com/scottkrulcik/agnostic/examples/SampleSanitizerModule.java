package com.scottkrulcik.agnostic.examples;

import com.scottkrulcik.agnostic.SimpleData;
import com.scottkrulcik.agnostic.SimpleData.AlwaysNoLabel;
import com.scottkrulcik.agnostic.SimpleData.AlwaysYesLabel;
import com.scottkrulcik.agnostic.SimpleData.DefaultCreationDate;
import com.scottkrulcik.agnostic.SimpleData.DefaultName;
import com.scottkrulcik.agnostic.annotations.Raw;
import dagger.Module;
import java.util.Date;

@Module
public class SampleSanitizerModule {
    private static final String DEFAULT_NAME = new DefaultName().call();
    private static final Date DEFAULT_DATE = new DefaultCreationDate().call();

    public SimpleData provideSimpleData(@Raw SimpleData simpleData,
        @AlwaysNoLabel.Result boolean alwaysNo, @AlwaysYesLabel.Result boolean alwaysYes) {
        if (!alwaysNo) {
            simpleData = simpleData.withName(DEFAULT_NAME);
        }
        if (!alwaysYes) {
            simpleData = simpleData.withCreationDate(DEFAULT_DATE);
        }
        return simpleData;
    }

}
