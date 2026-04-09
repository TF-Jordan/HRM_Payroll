package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.service.PerformanceReviewApplicationService;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/hrm/performance-reviews")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class PerformanceReviewController {

    private final PerformanceReviewApplicationService performanceReviewApplicationService;

    public PerformanceReviewController(PerformanceReviewApplicationService performanceReviewApplicationService) {
        this.performanceReviewApplicationService = performanceReviewApplicationService;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<PerformanceReviewResponse>>> createReview(
            @Valid @RequestBody Mono<CreatePerformanceReviewRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return performanceReviewApplicationService.createReview(context.tenantId(),
                            context.organizationId(), request.employeeId(), request.reviewerId(),
                            request.reviewPeriod());
                })
                .map(PerformanceReviewResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Performance review created.")));
    }

    @GetMapping("/{reviewId}")
    public Mono<ResponseEntity<ApiResponse<PerformanceReviewResponse>>> getReview(
            @PathVariable("reviewId") UUID reviewId) {
        return performanceReviewApplicationService.getReview(reviewId)
                .map(PerformanceReviewResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Performance review fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    public Mono<ResponseEntity<ApiResponse<List<PerformanceReviewResponse>>>> listByEmployee(
            @PathVariable("employeeId") UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> performanceReviewApplicationService.listByEmployee(
                        context.tenantId(), employeeId))
                .map(PerformanceReviewResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Performance reviews fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<PerformanceReviewResponse>>>> listByOrganization() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> performanceReviewApplicationService.listByOrganization(
                        context.tenantId(), context.organizationId()))
                .map(PerformanceReviewResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Performance reviews fetched.")));
    }

    @PostMapping("/{reviewId}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<PerformanceReviewResponse>>> completeReview(
            @PathVariable("reviewId") UUID reviewId,
            @Valid @RequestBody Mono<CompleteReviewRequest> requestMono) {
        return requestMono
                .flatMap(request -> performanceReviewApplicationService.completeReview(reviewId,
                        request.overallRating(), request.comments()))
                .map(PerformanceReviewResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Performance review completed.")));
    }
}
