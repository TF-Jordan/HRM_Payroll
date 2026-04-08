package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Employee;

import java.time.LocalDate;
import java.util.UUID;

import reactor.core.publisher.Mono;

public interface TerminateEmployeeUseCase {

    Mono<Employee> terminateEmployee(UUID employeeId, LocalDate terminationDate);
}
