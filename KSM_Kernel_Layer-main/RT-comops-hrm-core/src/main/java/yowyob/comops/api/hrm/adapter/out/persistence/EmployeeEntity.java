package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "hrm", name = "employee")
public record EmployeeEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID actorId,
        String registrationNumber,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String gender,
        LocalDate birthDate,
        LocalDate hireDate,
        LocalDate terminationDate,
        String department,
        String jobTitle,
        String status,
        String cnpsNumber) implements PersistableEntity {
}
