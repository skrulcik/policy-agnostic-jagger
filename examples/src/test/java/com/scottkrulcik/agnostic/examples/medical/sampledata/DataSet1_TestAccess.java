package com.scottkrulcik.agnostic.examples.medical.sampledata;

import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.Model.Record;

/**
 * Test providing public access to the variables of {@link DataSet1} to allow it to refer to
 * specific entities, while allowing the same dummy data set to be used in live demos.
 */
public final class DataSet1_TestAccess {
    private static DataSet1 DELEGATE = DataSet1.INSTANCE;

    public static Person alice = DELEGATE.alice;
    public static Person bob = DELEGATE.bob;
    public static Person docC = DELEGATE.docC;
    public static Person docD = DELEGATE.docD;
    public static Person docE = DELEGATE.docE;

    public static Record rec1 = DELEGATE.rec1;
    public static Record rec2 = DELEGATE.rec2;
    public static Record rec3 = DELEGATE.rec3;
    public static Record rec4 = DELEGATE.rec4;

    public static Record psychRec1 = DELEGATE.psychRec1;
    public static Record psychRec2 = DELEGATE.psychRec2;

    public static ConsentForm consentPsych2 = ConsentForm.create(psychRec2, docC);

}
