package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "payroll_entry")
public record PayrollEntryEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID payrollRunId,
        UUID employeeId,
        UUID contractId,
        BigDecimal baseSalary,
        BigDecimal grossSalary,
        BigDecimal netSalary,
        BigDecimal totalDeductions,
        BigDecimal totalEmployerCharges,
        String currency) implements PersistableEntity {
}
