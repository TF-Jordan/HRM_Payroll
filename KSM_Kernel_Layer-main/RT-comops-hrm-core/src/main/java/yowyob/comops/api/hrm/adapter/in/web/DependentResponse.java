package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.Dependent;

import java.time.LocalDate;
import java.util.UUID;

public record DependentResponse(
        UUID id,
        UUID employeeId,
        String firstName,
        String lastName,
        String relationship,
        LocalDate birthDate,
        String gender) {

    public static DependentResponse from(Dependent dependent) {
        return new DependentResponse(dependent.id(), dependent.employeeId(), dependent.firstName(),
                dependent.lastName(), dependent.relationship(), dependent.birthDate(), dependent.gender());
    }
}
