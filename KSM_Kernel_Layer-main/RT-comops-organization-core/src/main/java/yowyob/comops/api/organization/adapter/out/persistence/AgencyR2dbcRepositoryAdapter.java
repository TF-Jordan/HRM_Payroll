package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AgencyR2dbcRepositoryAdapter implements AgencyRepository {

    private final AgencySpringDataRepository repository;

    public AgencyR2dbcRepositoryAdapter(AgencySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code) {
        return repository.existsByTenantIdAndOrganizationIdAndCodeIgnoreCase(tenantId, organizationId, code);
    }

    @Override
    public Mono<Agency> findById(UUID tenantId, UUID agencyId) {
        return repository.findByIdAndTenantId(agencyId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Agency> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Agency> findByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Agency> save(Agency agency) {
        return repository.save(new AgencyEntity(agency.id(), agency.tenantId(), agency.createdAt(), agency.updatedAt(),
                agency.organizationId(), agency.governanceStatus().name(), agency.governedByUserId(),
                agency.governedAt(), agency.governanceReason(), agency.code(), agency.name(), agency.agencyType(),
                agency.active())).map(this::toDomain);
    }

    private Agency toDomain(AgencyEntity entity) {
        return Agency.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.governanceStatus(), entity.governedByUserId(), entity.governedAt(),
                entity.governanceReason(), entity.code(), entity.name(), entity.agencyType(), entity.active());
    }
}
