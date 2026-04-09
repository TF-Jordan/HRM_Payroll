package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "leave_request")
public record LeaveRequestEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal daysRequested,
        String reason,
        String status,
        UUID approvedBy,
        Instant approvedAt,
        String rejectionReason) implements PersistableEntity {
}
