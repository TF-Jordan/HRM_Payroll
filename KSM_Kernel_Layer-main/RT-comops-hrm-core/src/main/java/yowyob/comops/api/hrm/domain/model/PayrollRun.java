package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.hrm.domain.exception.InvalidPayrollStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PayrollRun extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "CALCULATED", "VALIDATED", "PAID", "CANCELLED");

    private final UUID organizationId;
    private final UUID agencyId;
    private final String period;
    private final String status;
    private final Instant calculatedAt;
    private final UUID validatedBy;
    private final Instant validatedAt;
    private final Instant paidAt;
    private final BigDecimal totalGross;
    private final BigDecimal totalNet;
    private final BigDecimal totalEmployerCharges;
    private final String currency;
    private final int employeeCount;

    private PayrollRun(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                       UUID organizationId, UUID agencyId, String period, String status,
                       Instant calculatedAt, UUID validatedBy, Instant validatedAt, Instant paidAt,
                       BigDecimal totalGross, BigDecimal totalNet, BigDecimal totalEmployerCharges,
                       String currency, int employeeCount) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = agencyId;
        this.period = requireText(period, "period");
        this.status = normalizeStatus(status);
        this.calculatedAt = calculatedAt;
        this.validatedBy = validatedBy;
        this.validatedAt = validatedAt;
        this.paidAt = paidAt;
        this.totalGross = Objects.requireNonNull(totalGross, "totalGross is required");
        this.totalNet = Objects.requireNonNull(totalNet, "totalNet is required");
        this.totalEmployerCharges = Objects.requireNonNull(totalEmployerCharges, "totalEmployerCharges is required");
        this.currency = requireText(currency, "currency").toUpperCase();
        this.employeeCount = employeeCount;
    }

    public static PayrollRun create(UUID tenantId, UUID organizationId, UUID agencyId,
                                    String period, String currency) {
        Instant now = Instant.now();
        return new PayrollRun(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, period,
                "DRAFT", null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                currency, 0);
    }

    public static PayrollRun rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                       UUID organizationId, UUID agencyId, String period, String status,
                                       Instant calculatedAt, UUID validatedBy, Instant validatedAt,
                                       Instant paidAt, BigDecimal totalGross, BigDecimal totalNet,
                                       BigDecimal totalEmployerCharges, String currency, int employeeCount) {
        return new PayrollRun(id, tenantId, createdAt, updatedAt, organizationId, agencyId, period,
                status, calculatedAt, validatedBy, validatedAt, paidAt, totalGross, totalNet,
                totalEmployerCharges, currency, employeeCount);
    }

    public PayrollRun markCalculated(BigDecimal totalGross, BigDecimal totalNet,
                                     BigDecimal totalEmployerCharges, int employeeCount) {
        if (!"DRAFT".equals(status)) {
            throw new InvalidPayrollStateException(id(), status, "DRAFT");
        }
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                period, "CALCULATED", Instant.now(), null, null, null, totalGross, totalNet,
                totalEmployerCharges, currency, employeeCount);
    }

    public PayrollRun validate(UUID validatedBy) {
        if (!"CALCULATED".equals(status)) {
            throw new InvalidPayrollStateException(id(), status, "CALCULATED");
        }
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                period, "VALIDATED", calculatedAt, validatedBy, Instant.now(), null, totalGross, totalNet,
                totalEmployerCharges, currency, employeeCount);
    }

    public PayrollRun markPaid() {
        if (!"VALIDATED".equals(status)) {
            throw new InvalidPayrollStateException(id(), status, "VALIDATED");
        }
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                period, "PAID", calculatedAt, validatedBy, validatedAt, Instant.now(), totalGross, totalNet,
                totalEmployerCharges, currency, employeeCount);
    }

    public PayrollRun cancel() {
        if ("PAID".equals(status) || "CANCELLED".equals(status)) {
            throw new InvalidPayrollStateException(id(), status, "DRAFT, CALCULATED or VALIDATED");
        }
        return new PayrollRun(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                period, "CANCELLED", calculatedAt, validatedBy, validatedAt, paidAt, totalGross, totalNet,
                totalEmployerCharges, currency, employeeCount);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String period() { return period; }
    public String status() { return status; }
    public Instant calculatedAt() { return calculatedAt; }
    public UUID validatedBy() { return validatedBy; }
    public Instant validatedAt() { return validatedAt; }
    public Instant paidAt() { return paidAt; }
    public BigDecimal totalGross() { return totalGross; }
    public BigDecimal totalNet() { return totalNet; }
    public BigDecimal totalEmployerCharges() { return totalEmployerCharges; }
    public String currency() { return currency; }
    public int employeeCount() { return employeeCount; }

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
