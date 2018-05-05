package com.scottkrulcik.agnostic.examples.medical;

import javax.inject.Inject;

import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Doctor;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;

final class JaggerConsentForm extends ConsentForm {

    private final Factory creator;
    private final ConsentForm delegate;

    JaggerConsentForm(
            ConsentForm delegate,
            Factory creator) {
        if (creator == null) {
            throw new NullPointerException("Null creator");
        }
        this.creator = creator;
        if (delegate == null) {
            throw new NullPointerException("Null delegate");
        }
        this.delegate = delegate;
    }

    @Override
    public Model.Record record() {
        if (formVisible(creator.doctor))
            return delegate.record();
        return Model.EMPTY_REC;
    }

    @Override
    public Model.Person provider() {
        return delegate.provider();
    }

    @Override
    public String toString() {
        return "Jagger" + delegate.toString();
    }

    public static final class Factory {
        private final Person doctor;

        @Inject
        public Factory(@Doctor Person doctor) {
            this.doctor = doctor;
        }

        public final ConsentForm wrap(ConsentForm raw) {
            return new JaggerConsentForm(raw, this);
        }
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

}
