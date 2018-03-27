package com.scottkrulcik.agnostic.examples.medical;

import com.scottkrulcik.agnostic.examples.medical.Model.Person;

interface MedicalContext {
    Person requester();
}
