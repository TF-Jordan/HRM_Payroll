package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreatePayrollRunRequest(
        UUID agencyId,
        @NotBlank String period,
        @NotBlank String currency) {
}
