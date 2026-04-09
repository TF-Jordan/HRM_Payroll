package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.PerformanceReview;

import java.math.BigDecimal;
import java.util.UUID;

public record PerformanceReviewResponse(
        UUID id,
        UUID organizationId,
        UUID employeeId,
        UUID reviewerId,
        String reviewPeriod,
        BigDecimal overallRating,
        String comments,
        String status) {

    public static PerformanceReviewResponse from(PerformanceReview review) {
        return new PerformanceReviewResponse(review.id(), review.organizationId(), review.employeeId(),
                review.reviewerId(), review.reviewPeriod(), review.overallRating(), review.comments(),
                review.status());
    }
}
