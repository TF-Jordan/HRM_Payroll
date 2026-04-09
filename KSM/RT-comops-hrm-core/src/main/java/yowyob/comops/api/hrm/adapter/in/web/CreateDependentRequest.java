package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDependentRequest(
        @NotNull UUID employeeId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String relationship,
        LocalDate birthDate,
        String gender) {
}
