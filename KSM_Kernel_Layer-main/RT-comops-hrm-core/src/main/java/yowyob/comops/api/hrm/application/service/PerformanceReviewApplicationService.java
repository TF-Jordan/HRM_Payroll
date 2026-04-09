package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.PerformanceReviewRepository;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.PerformanceReview;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PerformanceReviewApplicationService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public PerformanceReviewApplicationService(PerformanceReviewRepository performanceReviewRepository,
                                               EmployeeRepository employeeRepository,
                                               ReactiveTransactionalExecutor transactionalExecutor) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeRepository = employeeRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    public Mono<PerformanceReview> createReview(UUID tenantId, UUID organizationId, UUID employeeId,
                                                UUID reviewerId, String reviewPeriod) {
        return transactionalExecutor.transactional(
                employeeRepository.findById(employeeId)
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                        .flatMap(employee -> {
                            PerformanceReview review = PerformanceReview.create(tenantId, organizationId,
                                    employeeId, reviewerId, reviewPeriod);
                            return performanceReviewRepository.save(review);
                        }));
    }

    public Mono<PerformanceReview> getReview(UUID reviewId) {
        return performanceReviewRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found: " + reviewId)));
    }

    public Flux<PerformanceReview> listByEmployee(UUID tenantId, UUID employeeId) {
        return performanceReviewRepository.findByEmployeeId(tenantId, employeeId);
    }

    public Flux<PerformanceReview> listByOrganization(UUID tenantId, UUID organizationId) {
        return performanceReviewRepository.findByOrganizationId(tenantId, organizationId);
    }

    public Mono<PerformanceReview> completeReview(UUID reviewId, BigDecimal overallRating, String comments) {
        return transactionalExecutor.transactional(
                performanceReviewRepository.findById(reviewId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Review not found: " + reviewId)))
                        .map(review -> review.complete(overallRating, comments))
                        .flatMap(performanceReviewRepository::save));
    }
}
