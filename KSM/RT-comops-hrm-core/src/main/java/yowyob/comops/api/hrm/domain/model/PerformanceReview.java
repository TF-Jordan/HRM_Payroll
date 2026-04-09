package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PerformanceReview extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "IN_PROGRESS", "COMPLETED", "CANCELLED");

    private final UUID organizationId;
    private final UUID employeeId;
    private final UUID reviewerId;
    private final String reviewPeriod;
    private final BigDecimal overallRating;
    private final String comments;
    private final String status;

    private PerformanceReview(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                              UUID organizationId, UUID employeeId, UUID reviewerId,
                              String reviewPeriod, BigDecimal overallRating, String comments,
                              String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.reviewerId = Objects.requireNonNull(reviewerId, "reviewerId is required");
        this.reviewPeriod = requireText(reviewPeriod, "reviewPeriod");
        this.overallRating = overallRating;
        this.comments = comments;
        this.status = normalizeStatus(status);
    }

    public static PerformanceReview create(UUID tenantId, UUID organizationId, UUID employeeId,
                                           UUID reviewerId, String reviewPeriod) {
        Instant now = Instant.now();
        return new PerformanceReview(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                reviewerId, reviewPeriod, null, null, "DRAFT");
    }

    public static PerformanceReview rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                              UUID organizationId, UUID employeeId, UUID reviewerId,
                                              String reviewPeriod, BigDecimal overallRating, String comments,
                                              String status) {
        return new PerformanceReview(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                reviewerId, reviewPeriod, overallRating, comments, status);
    }

    public PerformanceReview complete(BigDecimal overallRating, String comments) {
        if (!"DRAFT".equals(status) && !"IN_PROGRESS".equals(status)) {
            throw new IllegalArgumentException("Review must be DRAFT or IN_PROGRESS to complete, current: " + status);
        }
        return new PerformanceReview(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, reviewerId, reviewPeriod, overallRating, comments, "COMPLETED");
    }

    public PerformanceReview cancel() {
        if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            throw new IllegalArgumentException("Review cannot be cancelled, current: " + status);
        }
        return new PerformanceReview(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                employeeId, reviewerId, reviewPeriod, overallRating, comments, "CANCELLED");
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public UUID reviewerId() { return reviewerId; }
    public String reviewPeriod() { return reviewPeriod; }
    public BigDecimal overallRating() { return overallRating; }
    public String comments() { return comments; }
    public String status() { return status; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }
}
