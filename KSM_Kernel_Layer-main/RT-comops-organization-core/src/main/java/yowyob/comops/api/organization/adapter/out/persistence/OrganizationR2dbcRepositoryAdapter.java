package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.model.Organization;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OrganizationR2dbcRepositoryAdapter implements OrganizationRepository {

    private final OrganizationSpringDataRepository repository;

    public OrganizationR2dbcRepositoryAdapter(OrganizationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, String code) {
        return repository.existsByTenantIdAndCodeIgnoreCase(tenantId, code);
    }

    @Override
    public Mono<Boolean> existsByCodeExcludingId(UUID tenantId, String code, UUID organizationId) {
        return repository.existsByTenantIdAndCodeIgnoreCaseAndIdNot(tenantId, code, organizationId);
    }

    @Override
    public Mono<Organization> findById(UUID tenantId, UUID organizationId) {
        return repository.findByIdAndTenantId(organizationId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Organization> findByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Organization> findByBusinessActorId(UUID tenantId, UUID businessActorId) {
        return repository.findAllByTenantIdAndBusinessActorId(tenantId, businessActorId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Organization> save(Organization organization) {
        return repository.save(new OrganizationEntity(organization.id(), organization.tenantId(), organization.createdAt(),
                organization.updatedAt(), organization.businessActorId(), organization.governanceStatus().name(),
                organization.governedByUserId(), organization.governedAt(), organization.governanceReason(),
                organization.code(), organization.legalName(), organization.displayName(),
                organization.organizationType())).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID organizationId) {
        return repository.deleteByIdAndTenantId(organizationId, tenantId);
    }

    private Organization toDomain(OrganizationEntity entity) {
        return Organization.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.businessActorId(), entity.governanceStatus(), entity.governedByUserId(), entity.governedAt(),
                entity.governanceReason(), entity.code(), entity.legalName(), entity.displayName(),
                entity.organizationType());
    }
}
