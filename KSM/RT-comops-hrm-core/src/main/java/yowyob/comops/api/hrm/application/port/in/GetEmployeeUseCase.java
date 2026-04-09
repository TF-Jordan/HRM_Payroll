package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Employee;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface GetEmployeeUseCase {

    Mono<Employee> getEmployee(UUID employeeId);
}
