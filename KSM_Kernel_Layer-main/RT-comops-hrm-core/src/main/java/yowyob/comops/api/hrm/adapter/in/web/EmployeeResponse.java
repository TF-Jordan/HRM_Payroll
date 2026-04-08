package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.Employee;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
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
        LocalDate terminationDate,
        String department,
        String jobTitle,
        String status,
        String cnpsNumber) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.id(), employee.tenantId(), employee.organizationId(),
                employee.agencyId(), employee.actorId(), employee.registrationNumber(), employee.firstName(),
                employee.lastName(), employee.email(), employee.phoneNumber(), employee.gender(),
                employee.birthDate(), employee.hireDate(), employee.terminationDate(), employee.department(),
                employee.jobTitle(), employee.status(), employee.cnpsNumber());
    }
}
