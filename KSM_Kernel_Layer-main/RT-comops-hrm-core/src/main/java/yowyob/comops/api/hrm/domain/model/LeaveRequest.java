package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class LeaveRequest extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED", "CANCELLED");

    private final UUID organizationId;
    private final UUID employeeId;
    private final String leaveType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal daysRequested;
    private final String reason;
    private final String status;
    private final UUID approvedBy;
    private final Instant approvedAt;
    private final String rejectionReason;

    private LeaveRequest(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID organizationId, UUID employeeId, String leaveType,
                         LocalDate startDate, LocalDate endDate, BigDecimal daysRequested,
                         String reason, String status, UUID approvedBy, Instant approvedAt,
                         String rejectionReason) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.leaveType = requireText(leaveType, "leaveType");
        this.startDate = Objects.requireNonNull(startDate, "startDate is required");
        this.endDate = Objects.requireNonNull(endDate, "endDate is required");
        this.daysRequested = requirePositive(daysRequested, "daysRequested");
        this.reason = reason;
        this.status = normalizeStatus(status);
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
    }

    public static LeaveRequest create(UUID tenantId, UUID organizationId, UUID employeeId,
                                      String leaveType, LocalDate startDate, LocalDate endDate,
                                      BigDecimal daysRequested, String reason) {
        Instant now = Instant.now();
        return new LeaveRequest(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                leaveType, startDate, endDate, daysRequested, reason, "PENDING", null, null, null);
    }

    public static LeaveRequest rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID organizationId, UUID employeeId, String leaveType,
                                         LocalDate startDate, LocalDate endDate, BigDecimal daysRequested,
                                         String reason, String status, UUID approvedBy, Instant approvedAt,
                                         String rejectionReason) {
        return new LeaveRequest(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                leaveType, startDate, endDate, daysRequested, reason, status, approvedBy, approvedAt,
                rejectionReason);
    }

    public LeaveRequest approve(UUID approvedBy) {
        if (!"PENDING".equals(status)) {
            throw new IllegalArgumentException("Leave request must be PENDING to approve, current: " + status);
        }
        return new LeaveRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                leaveType, startDate, endDate, daysRequested, reason, "APPROVED", approvedBy,
                Instant.now(), null);
    }

    public LeaveRequest reject(UUID rejectedBy, String rejectionReason) {
        if (!"PENDING".equals(status)) {
            throw new IllegalArgumentException("Leave request must be PENDING to reject, current: " + status);
        }
        return new LeaveRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                leaveType, startDate, endDate, daysRequested, reason, "REJECTED", rejectedBy,
                Instant.now(), rejectionReason);
    }

    public LeaveRequest cancel() {
        if (!"PENDING".equals(status) && !"APPROVED".equals(status)) {
            throw new IllegalArgumentException("Leave request must be PENDING or APPROVED to cancel, current: " + status);
        }
        return new LeaveRequest(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                leaveType, startDate, endDate, daysRequested, reason, "CANCELLED", approvedBy,
                approvedAt, null);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String leaveType() { return leaveType; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }
    public BigDecimal daysRequested() { return daysRequested; }
    public String reason() { return reason; }
    public String status() { return status; }
    public UUID approvedBy() { return approvedBy; }
    public Instant approvedAt() { return approvedAt; }
    public String rejectionReason() { return rejectionReason; }

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

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
