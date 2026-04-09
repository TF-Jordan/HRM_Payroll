package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.PerformanceReview;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PerformanceReviewRepository {

    Mono<PerformanceReview> findById(UUID reviewId);

    Flux<PerformanceReview> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<PerformanceReview> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<PerformanceReview> save(PerformanceReview review);
}
