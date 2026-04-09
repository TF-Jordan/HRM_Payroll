package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PerformanceReviewRepository;
import yowyob.comops.api.hrm.domain.model.PerformanceReview;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PerformanceReviewR2dbcRepositoryAdapter implements PerformanceReviewRepository {

    private final PerformanceReviewSpringDataRepository repository;

    public PerformanceReviewR2dbcRepositoryAdapter(PerformanceReviewSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PerformanceReview> findById(UUID reviewId) {
        return repository.findById(reviewId).map(this::toDomain);
    }

    @Override
    public Flux<PerformanceReview> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<PerformanceReview> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<PerformanceReview> save(PerformanceReview pr) {
        PerformanceReviewEntity entity = new PerformanceReviewEntity(pr.id(), pr.tenantId(), pr.createdAt(),
                pr.updatedAt(), pr.organizationId(), pr.employeeId(), pr.reviewerId(), pr.reviewPeriod(),
                pr.overallRating(), pr.comments(), pr.status());
        return repository.save(entity).map(this::toDomain);
    }

    private PerformanceReview toDomain(PerformanceReviewEntity e) {
        return PerformanceReview.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.reviewerId(), e.reviewPeriod(), e.overallRating(),
                e.comments(), e.status());
    }
}
