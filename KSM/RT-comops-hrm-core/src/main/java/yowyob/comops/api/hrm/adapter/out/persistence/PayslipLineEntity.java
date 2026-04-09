package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "payslip_line")
public record PayslipLineEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID payrollEntryId,
        String code,
        String label,
        String lineType,
        BigDecimal base,
        BigDecimal rate,
        BigDecimal employeeAmount,
        BigDecimal employerAmount,
        int sortOrder) implements PersistableEntity {
}
