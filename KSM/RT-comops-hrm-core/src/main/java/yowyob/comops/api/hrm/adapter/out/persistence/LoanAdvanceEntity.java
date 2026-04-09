package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "loan_advance")
public record LoanAdvanceEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String loanType,
        BigDecimal amount,
        String currency,
        BigDecimal monthlyDeduction,
        BigDecimal totalRepaid,
        BigDecimal remainingBalance,
        int installmentsCount,
        int installmentsPaid,
        String status,
        UUID approvedBy,
        Instant approvedAt) implements PersistableEntity {
}
