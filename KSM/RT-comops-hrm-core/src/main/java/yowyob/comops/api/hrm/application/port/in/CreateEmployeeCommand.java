package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeCommand(
        UUID tenantId,
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
        String department,
        String jobTitle,
        String cnpsNumber) {
}
