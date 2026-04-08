package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "contract")
public record ContractEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String contractType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseSalary,
        String currency,
        String status) implements PersistableEntity {
}
