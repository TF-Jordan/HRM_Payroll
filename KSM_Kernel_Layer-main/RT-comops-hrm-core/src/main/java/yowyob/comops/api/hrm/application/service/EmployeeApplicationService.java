package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.CreateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.GetEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.ListEmployeesUseCase;
import yowyob.comops.api.hrm.application.port.in.TerminateEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.domain.exception.DuplicateRegistrationNumberException;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeApplicationService implements CreateEmployeeUseCase, GetEmployeeUseCase,
        ListEmployeesUseCase, UpdateEmployeeUseCase, TerminateEmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public EmployeeApplicationService(EmployeeRepository employeeRepository,
                                      BusinessEventPublisher businessEventPublisher,
                                      ReactiveTransactionalExecutor transactionalExecutor) {
        this.employeeRepository = employeeRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Employee> createEmployee(CreateEmployeeCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                employeeRepository.existsByRegistrationNumber(command.tenantId(), command.organizationId(),
                                command.registrationNumber())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new DuplicateRegistrationNumberException(command.registrationNumber()));
                            }
                            Employee employee = Employee.create(command.tenantId(), command.organizationId(),
                                    command.agencyId(), command.actorId(), command.registrationNumber(),
                                    command.firstName(), command.lastName(), command.email(), command.phoneNumber(),
                                    command.gender(), command.birthDate(), command.hireDate(), command.department(),
                                    command.jobTitle(), command.cnpsNumber());
                            return employeeRepository.save(employee);
                        })
                        .flatMap(saved -> businessEventPublisher.publish(employeeCreatedEvent(saved))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<Employee> getEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)));
    }

    @Override
    public Flux<Employee> listEmployees(UUID tenantId, UUID organizationId, UUID agencyId) {
        if (agencyId != null) {
            return employeeRepository.findByOrganizationIdAndAgencyId(tenantId, organizationId, agencyId);
        }
        return employeeRepository.findByOrganizationId(tenantId, organizationId);
    }

    @Override
    public Mono<Employee> updateEmployee(UpdateEmployeeCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                employeeRepository.findById(command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(existing -> {
                            Employee updated = existing.update(command.firstName(), command.lastName(),
                                    command.email(), command.phoneNumber(), command.gender(), command.birthDate(),
                                    command.department(), command.jobTitle(), command.agencyId(), command.cnpsNumber());
                            return employeeRepository.save(updated);
                        }));
    }

    @Override
    public Mono<Employee> terminateEmployee(UUID employeeId, LocalDate terminationDate) {
        return transactionalExecutor.transactional(
                employeeRepository.findById(employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .map(employee -> employee.terminate(terminationDate))
                        .flatMap(employeeRepository::save)
                        .flatMap(saved -> businessEventPublisher.publish(employeeTerminatedEvent(saved))
                                .thenReturn(saved)));
    }

    private BusinessEvent employeeCreatedEvent(Employee employee) {
        return BusinessEvent.now(employee.tenantId(), employee.organizationId(), "EMPLOYEE_CREATED", "EMPLOYEE",
                employee.id(), payload(
                        "registrationNumber", employee.registrationNumber(),
                        "firstName", employee.firstName(),
                        "lastName", employee.lastName(),
                        "hireDate", employee.hireDate().toString(),
                        "department", employee.department(),
                        "jobTitle", employee.jobTitle()));
    }

    private BusinessEvent employeeTerminatedEvent(Employee employee) {
        return BusinessEvent.now(employee.tenantId(), employee.organizationId(), "EMPLOYEE_TERMINATED", "EMPLOYEE",
                employee.id(), payload(
                        "registrationNumber", employee.registrationNumber(),
                        "terminationDate", employee.terminationDate().toString()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            if (entries[index + 1] != null) {
                payload.put(entries[index].toString(), entries[index + 1]);
            }
        }
        return payload;
    }
}
