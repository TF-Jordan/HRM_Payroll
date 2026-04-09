package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CompleteReviewRequest(
        @NotNull @DecimalMin("0") BigDecimal overallRating,
        String comments) {
}
