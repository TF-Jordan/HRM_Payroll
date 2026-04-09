package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TrainingSpringDataRepository extends ReactiveCrudRepository<TrainingEntity, UUID> {

    Flux<TrainingEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
