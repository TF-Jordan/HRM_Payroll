package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeSpringDataRepository extends ReactiveCrudRepository<EmployeeEntity, UUID> {

    Flux<EmployeeEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Flux<EmployeeEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndRegistrationNumber(UUID tenantId, UUID organizationId,
            String registrationNumber);

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndRegistrationNumberAndIdNot(UUID tenantId, UUID organizationId,
            String registrationNumber, UUID id);
}
