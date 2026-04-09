package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRunSpringDataRepository extends ReactiveCrudRepository<PayrollRunEntity, UUID> {

    Flux<PayrollRunEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndPeriod(UUID tenantId, UUID organizationId, String period);
}
