package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.CreateDependentCommand;
import yowyob.comops.api.hrm.application.port.in.CreateDependentUseCase;
import yowyob.comops.api.hrm.application.port.in.DeleteDependentUseCase;
import yowyob.comops.api.hrm.application.port.in.ListDependentsUseCase;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DependentApplicationService implements CreateDependentUseCase, ListDependentsUseCase,
        DeleteDependentUseCase {

    private final DependentRepository dependentRepository;
    private final EmployeeRepository employeeRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public DependentApplicationService(DependentRepository dependentRepository,
                                       EmployeeRepository employeeRepository,
                                       ReactiveTransactionalExecutor transactionalExecutor) {
        this.dependentRepository = dependentRepository;
        this.employeeRepository = employeeRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Dependent> createDependent(CreateDependentCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                employeeRepository.findById(command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> {
                            Dependent dependent = Dependent.create(command.tenantId(), command.organizationId(),
                                    command.employeeId(), command.firstName(), command.lastName(),
                                    command.relationship(), command.birthDate(), command.gender());
                            return dependentRepository.save(dependent);
                        }));
    }

    @Override
    public Flux<Dependent> listDependents(UUID tenantId, UUID employeeId) {
        return dependentRepository.findByEmployeeId(tenantId, employeeId);
    }

    @Override
    public Mono<Void> deleteDependent(UUID dependentId) {
        return dependentRepository.deleteById(dependentId);
    }
}
