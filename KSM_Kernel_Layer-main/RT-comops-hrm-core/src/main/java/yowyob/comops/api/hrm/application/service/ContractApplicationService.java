package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.ActivateContractUseCase;
import yowyob.comops.api.hrm.application.port.in.CreateContractCommand;
import yowyob.comops.api.hrm.application.port.in.CreateContractUseCase;
import yowyob.comops.api.hrm.application.port.in.GetContractUseCase;
import yowyob.comops.api.hrm.application.port.in.UpdateContractCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateContractUseCase;
import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.domain.exception.ContractNotFoundException;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ContractApplicationService implements CreateContractUseCase, GetContractUseCase,
        UpdateContractUseCase, ActivateContractUseCase {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public ContractApplicationService(ContractRepository contractRepository,
                                      EmployeeRepository employeeRepository,
                                      ReactiveTransactionalExecutor transactionalExecutor) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Contract> createContract(CreateContractCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                employeeRepository.findById(command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> {
                            Contract contract = Contract.create(command.tenantId(), command.organizationId(),
                                    command.employeeId(), command.contractType(), command.startDate(),
                                    command.endDate(), command.baseSalary(), command.currency());
                            return contractRepository.save(contract);
                        }));
    }

    @Override
    public Mono<Contract> getContract(UUID contractId) {
        return contractRepository.findById(contractId)
                .switchIfEmpty(Mono.error(new ContractNotFoundException(contractId)));
    }

    @Override
    public Flux<Contract> getContractsByEmployee(UUID tenantId, UUID employeeId) {
        return contractRepository.findByEmployeeId(tenantId, employeeId);
    }

    @Override
    public Mono<Contract> updateContract(UpdateContractCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                contractRepository.findById(command.contractId())
                        .switchIfEmpty(Mono.error(new ContractNotFoundException(command.contractId())))
                        .map(existing -> existing.update(command.contractType(), command.startDate(),
                                command.endDate(), command.baseSalary(), command.currency()))
                        .flatMap(contractRepository::save));
    }

    @Override
    public Mono<Contract> activateContract(UUID contractId) {
        return transactionalExecutor.transactional(
                contractRepository.findById(contractId)
                        .switchIfEmpty(Mono.error(new ContractNotFoundException(contractId)))
                        .map(Contract::activate)
                        .flatMap(contractRepository::save));
    }
}
