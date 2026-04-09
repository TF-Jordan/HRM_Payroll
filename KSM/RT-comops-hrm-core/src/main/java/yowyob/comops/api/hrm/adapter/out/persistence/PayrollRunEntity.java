package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "payroll_run")
public record PayrollRunEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        String period,
        String status,
        Instant calculatedAt,
        UUID validatedBy,
        Instant validatedAt,
        Instant paidAt,
        BigDecimal totalGross,
        BigDecimal totalNet,
        BigDecimal totalEmployerCharges,
        String currency,
        int employeeCount) implements PersistableEntity {
}
