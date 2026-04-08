package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Employee;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository {

    Mono<Employee> findById(UUID employeeId);

    Flux<Employee> findByOrganizationId(UUID tenantId, UUID organizationId);

    Flux<Employee> findByOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<Boolean> existsByRegistrationNumber(UUID tenantId, UUID organizationId, String registrationNumber);

    Mono<Boolean> existsByRegistrationNumberExcludingId(UUID tenantId, UUID organizationId,
            String registrationNumber, UUID employeeId);

    Mono<Employee> save(Employee employee);
}
