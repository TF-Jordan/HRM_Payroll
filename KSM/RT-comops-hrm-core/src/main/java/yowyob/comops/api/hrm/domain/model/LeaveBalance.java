package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LeaveBalance extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final String leaveType;
    private final int year;
    private final BigDecimal accrued;
    private final BigDecimal taken;
    private final BigDecimal adjustment;

    private LeaveBalance(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID organizationId, UUID employeeId, String leaveType, int year,
                         BigDecimal accrued, BigDecimal taken, BigDecimal adjustment) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.leaveType = requireText(leaveType, "leaveType");
        this.year = year;
        this.accrued = Objects.requireNonNull(accrued, "accrued is required");
        this.taken = Objects.requireNonNull(taken, "taken is required");
        this.adjustment = Objects.requireNonNull(adjustment, "adjustment is required");
    }

    public static LeaveBalance create(UUID tenantId, UUID organizationId, UUID employeeId,
                                      String leaveType, int year) {
        Instant now = Instant.now();
        return new LeaveBalance(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                leaveType, year, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static LeaveBalance rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID organizationId, UUID employeeId, String leaveType, int year,
                                         BigDecimal accrued, BigDecimal taken, BigDecimal adjustment) {
        return new LeaveBalance(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                leaveType, year, accrued, taken, adjustment);
    }

    public LeaveBalance accrue(BigDecimal days) {
        return new LeaveBalance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                leaveType, year, accrued.add(days), taken, adjustment);
    }

    public LeaveBalance take(BigDecimal days) {
        return new LeaveBalance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                leaveType, year, accrued, taken.add(days), adjustment);
    }

    public LeaveBalance adjust(BigDecimal days) {
        return new LeaveBalance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                leaveType, year, accrued, taken, adjustment.add(days));
    }

    public BigDecimal available() {
        return accrued.add(adjustment).subtract(taken);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String leaveType() { return leaveType; }
    public int year() { return year; }
    public BigDecimal accrued() { return accrued; }
    public BigDecimal taken() { return taken; }
    public BigDecimal adjustment() { return adjustment; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
