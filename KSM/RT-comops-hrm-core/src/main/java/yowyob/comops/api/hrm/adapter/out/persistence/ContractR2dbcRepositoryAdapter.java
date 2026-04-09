package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.domain.model.Contract;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ContractR2dbcRepositoryAdapter implements ContractRepository {

    private final ContractSpringDataRepository repository;

    public ContractR2dbcRepositoryAdapter(ContractSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Contract> findById(UUID contractId) {
        return repository.findById(contractId).map(this::toDomain);
    }

    @Override
    public Flux<Contract> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Mono<Contract> findActiveByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findByTenantIdAndEmployeeIdAndStatus(tenantId, employeeId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<Contract> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<Contract> save(Contract contract) {
        ContractEntity entity = new ContractEntity(contract.id(), contract.tenantId(), contract.createdAt(),
                contract.updatedAt(), contract.organizationId(), contract.employeeId(), contract.contractType(),
                contract.startDate(), contract.endDate(), contract.baseSalary(), contract.currency(),
                contract.status());
        return repository.save(entity).map(this::toDomain);
    }

    private Contract toDomain(ContractEntity entity) {
        return Contract.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.employeeId(), entity.contractType(), entity.startDate(),
                entity.endDate(), entity.baseSalary(), entity.currency(), entity.status());
    }
}
