package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeRequest(
        @NotNull UUID actorId,
        UUID agencyId,
        @NotBlank String registrationNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String email,
        String phoneNumber,
        String gender,
        LocalDate birthDate,
        @NotNull LocalDate hireDate,
        String department,
        String jobTitle,
        String cnpsNumber) {
}
