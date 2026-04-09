package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDependentCommand(
        UUID tenantId,
        UUID organizationId,
        UUID employeeId,
        String firstName,
        String lastName,
        String relationship,
        LocalDate birthDate,
        String gender) {
}
