package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateContractRequest(
        @NotBlank String contractType,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull @DecimalMin("1") BigDecimal baseSalary,
        @NotBlank String currency) {
}
