package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateEmployeeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String email,
        String phoneNumber,
        String gender,
        LocalDate birthDate,
        String department,
        String jobTitle,
        UUID agencyId,
        String cnpsNumber) {
}
