package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PerformanceReviewSpringDataRepository extends ReactiveCrudRepository<PerformanceReviewEntity, UUID> {

    Flux<PerformanceReviewEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<PerformanceReviewEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
