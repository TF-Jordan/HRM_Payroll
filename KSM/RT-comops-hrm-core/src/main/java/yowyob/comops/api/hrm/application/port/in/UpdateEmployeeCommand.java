package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateEmployeeCommand(
        UUID tenantId,
        UUID employeeId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String gender,
        LocalDate birthDate,
        String department,
        String jobTitle,
        UUID agencyId,
        String cnpsNumber) {
}
