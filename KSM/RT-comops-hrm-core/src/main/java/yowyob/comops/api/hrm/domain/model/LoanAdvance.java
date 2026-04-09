package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class LoanAdvance extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING", "APPROVED", "ACTIVE", "FULLY_REPAID", "REJECTED", "CANCELLED");

    private final UUID organizationId;
    private final UUID employeeId;
    private final String loanType;
    private final BigDecimal amount;
    private final String currency;
    private final BigDecimal monthlyDeduction;
    private final BigDecimal totalRepaid;
    private final BigDecimal remainingBalance;
    private final int installmentsCount;
    private final int installmentsPaid;
    private final String status;
    private final UUID approvedBy;
    private final Instant approvedAt;

    private LoanAdvance(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        UUID organizationId, UUID employeeId, String loanType, BigDecimal amount,
                        String currency, BigDecimal monthlyDeduction, BigDecimal totalRepaid,
                        BigDecimal remainingBalance, int installmentsCount, int installmentsPaid,
                        String status, UUID approvedBy, Instant approvedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.loanType = requireText(loanType, "loanType");
        this.amount = requirePositive(amount, "amount");
        this.currency = requireText(currency, "currency").toUpperCase();
        this.monthlyDeduction = requirePositive(monthlyDeduction, "monthlyDeduction");
        this.totalRepaid = Objects.requireNonNull(totalRepaid, "totalRepaid is required");
        this.remainingBalance = Objects.requireNonNull(remainingBalance, "remainingBalance is required");
        this.installmentsCount = installmentsCount;
        this.installmentsPaid = installmentsPaid;
        this.status = normalizeStatus(status);
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    public static LoanAdvance create(UUID tenantId, UUID organizationId, UUID employeeId,
                                     String loanType, BigDecimal amount, String currency,
                                     BigDecimal monthlyDeduction, int installmentsCount) {
        Instant now = Instant.now();
        return new LoanAdvance(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                loanType, amount, currency, monthlyDeduction, BigDecimal.ZERO, amount,
                installmentsCount, 0, "PENDING", null, null);
    }

    public static LoanAdvance rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        UUID organizationId, UUID employeeId, String loanType,
                                        BigDecimal amount, String currency, BigDecimal monthlyDeduction,
                                        BigDecimal totalRepaid, BigDecimal remainingBalance,
                                        int installmentsCount, int installmentsPaid, String status,
                                        UUID approvedBy, Instant approvedAt) {
        return new LoanAdvance(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                loanType, amount, currency, monthlyDeduction, totalRepaid, remainingBalance,
                installmentsCount, installmentsPaid, status, approvedBy, approvedAt);
    }

    public LoanAdvance approve(UUID approvedBy) {
        if (!"PENDING".equals(status)) {
            throw new IllegalArgumentException("Loan must be PENDING to approve, current: " + status);
        }
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                loanType, amount, currency, monthlyDeduction, totalRepaid, remainingBalance,
                installmentsCount, installmentsPaid, "APPROVED", approvedBy, Instant.now());
    }

    public LoanAdvance activate() {
        if (!"APPROVED".equals(status)) {
            throw new IllegalArgumentException("Loan must be APPROVED to activate, current: " + status);
        }
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                loanType, amount, currency, monthlyDeduction, totalRepaid, remainingBalance,
                installmentsCount, installmentsPaid, "ACTIVE", approvedBy, approvedAt);
    }

    public LoanAdvance applyRepayment(BigDecimal repaymentAmount) {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalArgumentException("Loan must be ACTIVE to apply repayment, current: " + status);
        }
        BigDecimal nextTotalRepaid = totalRepaid.add(repaymentAmount);
        BigDecimal nextRemainingBalance = amount.subtract(nextTotalRepaid);
        int nextInstallmentsPaid = installmentsPaid + 1;
        String nextStatus = nextRemainingBalance.signum() <= 0 ? "FULLY_REPAID" : "ACTIVE";
        if (nextRemainingBalance.signum() < 0) {
            nextRemainingBalance = BigDecimal.ZERO;
        }
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                loanType, amount, currency, monthlyDeduction, nextTotalRepaid, nextRemainingBalance,
                installmentsCount, nextInstallmentsPaid, nextStatus, approvedBy, approvedAt);
    }

    public LoanAdvance reject() {
        if (!"PENDING".equals(status)) {
            throw new IllegalArgumentException("Loan must be PENDING to reject, current: " + status);
        }
        return new LoanAdvance(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                loanType, amount, currency, monthlyDeduction, totalRepaid, remainingBalance,
                installmentsCount, installmentsPaid, "REJECTED", null, null);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String loanType() { return loanType; }
    public BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    public BigDecimal monthlyDeduction() { return monthlyDeduction; }
    public BigDecimal totalRepaid() { return totalRepaid; }
    public BigDecimal remainingBalance() { return remainingBalance; }
    public int installmentsCount() { return installmentsCount; }
    public int installmentsPaid() { return installmentsPaid; }
    public String status() { return status; }
    public UUID approvedBy() { return approvedBy; }
    public Instant approvedAt() { return approvedAt; }

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
