package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Employee;
import reactor.core.publisher.Mono;

public interface UpdateEmployeeUseCase {

    Mono<Employee> updateEmployee(UpdateEmployeeCommand command);
}
