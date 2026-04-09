package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PayrollEntry extends BaseEntity {

    private final UUID payrollRunId;
    private final UUID employeeId;
    private final UUID contractId;
    private final BigDecimal baseSalary;
    private final BigDecimal grossSalary;
    private final BigDecimal netSalary;
    private final BigDecimal totalDeductions;
    private final BigDecimal totalEmployerCharges;
    private final String currency;

    private PayrollEntry(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                         UUID payrollRunId, UUID employeeId, UUID contractId, BigDecimal baseSalary,
                         BigDecimal grossSalary, BigDecimal netSalary, BigDecimal totalDeductions,
                         BigDecimal totalEmployerCharges, String currency) {
        super(id, tenantId, createdAt, updatedAt);
        this.payrollRunId = Objects.requireNonNull(payrollRunId, "payrollRunId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.contractId = Objects.requireNonNull(contractId, "contractId is required");
        this.baseSalary = Objects.requireNonNull(baseSalary, "baseSalary is required");
        this.grossSalary = Objects.requireNonNull(grossSalary, "grossSalary is required");
        this.netSalary = Objects.requireNonNull(netSalary, "netSalary is required");
        this.totalDeductions = Objects.requireNonNull(totalDeductions, "totalDeductions is required");
        this.totalEmployerCharges = Objects.requireNonNull(totalEmployerCharges, "totalEmployerCharges is required");
        this.currency = requireText(currency, "currency").toUpperCase();
    }

    public static PayrollEntry create(UUID tenantId, UUID payrollRunId, UUID employeeId, UUID contractId,
                                      BigDecimal baseSalary, String currency) {
        Instant now = Instant.now();
        return new PayrollEntry(UUID.randomUUID(), tenantId, now, now, payrollRunId, employeeId, contractId,
                baseSalary, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, currency);
    }

    public static PayrollEntry rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                         UUID payrollRunId, UUID employeeId, UUID contractId,
                                         BigDecimal baseSalary, BigDecimal grossSalary, BigDecimal netSalary,
                                         BigDecimal totalDeductions, BigDecimal totalEmployerCharges,
                                         String currency) {
        return new PayrollEntry(id, tenantId, createdAt, updatedAt, payrollRunId, employeeId, contractId,
                baseSalary, grossSalary, netSalary, totalDeductions, totalEmployerCharges, currency);
    }

    public PayrollEntry withCalculatedAmounts(BigDecimal grossSalary, BigDecimal netSalary,
                                              BigDecimal totalDeductions, BigDecimal totalEmployerCharges) {
        return new PayrollEntry(id(), tenantId(), createdAt(), Instant.now(), payrollRunId, employeeId,
                contractId, baseSalary, grossSalary, netSalary, totalDeductions, totalEmployerCharges, currency);
    }

    public UUID payrollRunId() { return payrollRunId; }
    public UUID employeeId() { return employeeId; }
    public UUID contractId() { return contractId; }
    public BigDecimal baseSalary() { return baseSalary; }
    public BigDecimal grossSalary() { return grossSalary; }
    public BigDecimal netSalary() { return netSalary; }
    public BigDecimal totalDeductions() { return totalDeductions; }
    public BigDecimal totalEmployerCharges() { return totalEmployerCharges; }
    public String currency() { return currency; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
