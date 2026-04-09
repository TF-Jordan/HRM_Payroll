package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePerformanceReviewRequest(
        @NotNull UUID employeeId,
        @NotNull UUID reviewerId,
        @NotBlank String reviewPeriod) {
}
