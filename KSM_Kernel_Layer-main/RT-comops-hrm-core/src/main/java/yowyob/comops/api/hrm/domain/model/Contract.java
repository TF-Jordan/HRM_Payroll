package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.hrm.domain.exception.InvalidContractStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Contract extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "ACTIVE", "EXPIRED", "TERMINATED");
    private static final Set<String> ALLOWED_TYPES = Set.of("CDI", "CDD", "STAGE", "INTERIM", "CONSULTANT");

    private final UUID organizationId;
    private final UUID employeeId;
    private final String contractType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal baseSalary;
    private final String currency;
    private final String status;

    private Contract(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID employeeId, String contractType,
                     LocalDate startDate, LocalDate endDate, BigDecimal baseSalary,
                     String currency, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.contractType = normalizeType(contractType);
        this.startDate = Objects.requireNonNull(startDate, "startDate is required");
        this.endDate = endDate;
        this.baseSalary = requirePositive(baseSalary, "baseSalary");
        this.currency = normalizeCurrency(currency);
        this.status = normalizeStatus(status);
    }

    public static Contract create(UUID tenantId, UUID organizationId, UUID employeeId,
                                  String contractType, LocalDate startDate, LocalDate endDate,
                                  BigDecimal baseSalary, String currency) {
        Instant now = Instant.now();
        return new Contract(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                contractType, startDate, endDate, baseSalary, currency, "DRAFT");
    }

    public static Contract rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, UUID employeeId, String contractType,
                                     LocalDate startDate, LocalDate endDate, BigDecimal baseSalary,
                                     String currency, String status) {
        return new Contract(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                contractType, startDate, endDate, baseSalary, currency, status);
    }

    public Contract activate() {
        if (!"DRAFT".equals(status)) {
            throw new InvalidContractStateException(id(), status, "DRAFT");
        }
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                contractType, startDate, endDate, baseSalary, currency, "ACTIVE");
    }

    public Contract terminate() {
        if (!"ACTIVE".equals(status)) {
            throw new InvalidContractStateException(id(), status, "ACTIVE");
        }
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                contractType, startDate, endDate, baseSalary, currency, "TERMINATED");
    }

    public Contract expire() {
        if (!"ACTIVE".equals(status)) {
            throw new InvalidContractStateException(id(), status, "ACTIVE");
        }
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                contractType, startDate, endDate, baseSalary, currency, "EXPIRED");
    }

    public Contract update(String contractType, LocalDate startDate, LocalDate endDate,
                           BigDecimal baseSalary, String currency) {
        if (!"DRAFT".equals(status)) {
            throw new InvalidContractStateException(id(), status, "DRAFT");
        }
        return new Contract(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                contractType, startDate, endDate, baseSalary, currency, status);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String contractType() { return contractType; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }
    public BigDecimal baseSalary() { return baseSalary; }
    public String currency() { return currency; }
    public String status() { return status; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeType(String value) {
        String normalized = requireText(value, "contractType").toUpperCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("contractType must be one of " + ALLOWED_TYPES);
        }
        return normalized;
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static String normalizeCurrency(String value) {
        return requireText(value, "currency").toUpperCase();
    }

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
