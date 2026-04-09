package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.domain.model.Dependent;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class DependentR2dbcRepositoryAdapter implements DependentRepository {

    private final DependentSpringDataRepository repository;

    public DependentR2dbcRepositoryAdapter(DependentSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Dependent> findById(UUID dependentId) {
        return repository.findById(dependentId).map(this::toDomain);
    }

    @Override
    public Flux<Dependent> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Mono<Long> countChildrenByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.countByTenantIdAndEmployeeIdAndRelationship(tenantId, employeeId, "CHILD");
    }

    @Override
    public Mono<Dependent> save(Dependent dependent) {
        DependentEntity entity = new DependentEntity(dependent.id(), dependent.tenantId(), dependent.createdAt(),
                dependent.updatedAt(), dependent.organizationId(), dependent.employeeId(), dependent.firstName(),
                dependent.lastName(), dependent.relationship(), dependent.birthDate(), dependent.gender());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID dependentId) {
        return repository.deleteById(dependentId);
    }

    private Dependent toDomain(DependentEntity entity) {
        return Dependent.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.employeeId(), entity.firstName(), entity.lastName(),
                entity.relationship(), entity.birthDate(), entity.gender());
    }
}
