package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanAdvanceRequest(
        @NotNull UUID employeeId,
        @NotBlank String loanType,
        @NotNull @DecimalMin("1") BigDecimal amount,
        @NotBlank String currency,
        @NotNull @DecimalMin("1") BigDecimal monthlyDeduction,
        @Min(1) int installmentsCount) {
}
