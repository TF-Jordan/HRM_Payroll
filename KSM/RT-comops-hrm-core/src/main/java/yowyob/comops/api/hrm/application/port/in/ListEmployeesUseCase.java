package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Employee;

import java.util.UUID;

import reactor.core.publisher.Flux;

public interface ListEmployeesUseCase {

    Flux<Employee> listEmployees(UUID tenantId, UUID organizationId, UUID agencyId);
}
