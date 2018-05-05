package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.SanitizingFactory;
import com.scottkrulcik.agnostic.annotations.ContextScope;
import com.scottkrulcik.agnostic.examples.medical.Model.ConsentForm;
import com.scottkrulcik.agnostic.examples.medical.Model.Doctor;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;

import javax.inject.Inject;

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

    @ContextScope
    public static final class Factory implements SanitizingFactory<ConsentForm> {
        private final Person doctor;

        @Inject
        public Factory(@Doctor Person doctor) {
            this.doctor = doctor;
        }

        @Override
        public final JaggerConsentForm wrap(ConsentForm raw) {
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
