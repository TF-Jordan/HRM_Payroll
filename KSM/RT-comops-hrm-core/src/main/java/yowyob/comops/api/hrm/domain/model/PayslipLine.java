package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PayslipLine extends BaseEntity {

    private final UUID payrollEntryId;
    private final String code;
    private final String label;
    private final String lineType;
    private final BigDecimal base;
    private final BigDecimal rate;
    private final BigDecimal employeeAmount;
    private final BigDecimal employerAmount;
    private final int sortOrder;

    private PayslipLine(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        UUID payrollEntryId, String code, String label, String lineType,
                        BigDecimal base, BigDecimal rate, BigDecimal employeeAmount,
                        BigDecimal employerAmount, int sortOrder) {
        super(id, tenantId, createdAt, updatedAt);
        this.payrollEntryId = Objects.requireNonNull(payrollEntryId, "payrollEntryId is required");
        this.code = requireText(code, "code");
        this.label = requireText(label, "label");
        this.lineType = requireText(lineType, "lineType");
        this.base = base;
        this.rate = rate;
        this.employeeAmount = Objects.requireNonNull(employeeAmount, "employeeAmount is required");
        this.employerAmount = Objects.requireNonNull(employerAmount, "employerAmount is required");
        this.sortOrder = sortOrder;
    }

    public static PayslipLine create(UUID tenantId, UUID payrollEntryId, String code, String label,
                                     String lineType, BigDecimal base, BigDecimal rate,
                                     BigDecimal employeeAmount, BigDecimal employerAmount, int sortOrder) {
        Instant now = Instant.now();
        return new PayslipLine(UUID.randomUUID(), tenantId, now, now, payrollEntryId, code, label,
                lineType, base, rate, employeeAmount, employerAmount, sortOrder);
    }

    public static PayslipLine rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        UUID payrollEntryId, String code, String label, String lineType,
                                        BigDecimal base, BigDecimal rate, BigDecimal employeeAmount,
                                        BigDecimal employerAmount, int sortOrder) {
        return new PayslipLine(id, tenantId, createdAt, updatedAt, payrollEntryId, code, label,
                lineType, base, rate, employeeAmount, employerAmount, sortOrder);
    }

    public UUID payrollEntryId() { return payrollEntryId; }
    public String code() { return code; }
    public String label() { return label; }
    public String lineType() { return lineType; }
    public BigDecimal base() { return base; }
    public BigDecimal rate() { return rate; }
    public BigDecimal employeeAmount() { return employeeAmount; }
    public BigDecimal employerAmount() { return employerAmount; }
    public int sortOrder() { return sortOrder; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
