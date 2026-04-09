package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.service.TrainingApplicationService;
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
@RequestMapping("/api/hrm/trainings")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class TrainingController {

    private final TrainingApplicationService trainingApplicationService;

    public TrainingController(TrainingApplicationService trainingApplicationService) {
        this.trainingApplicationService = trainingApplicationService;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> createTraining(
            @Valid @RequestBody Mono<CreateTrainingRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return trainingApplicationService.createTraining(context.tenantId(),
                            context.organizationId(), request.title(), request.description(),
                            request.startDate(), request.endDate(), request.maxParticipants());
                })
                .map(TrainingResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Training created.")));
    }

    @GetMapping("/{trainingId}")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> getTraining(
            @PathVariable("trainingId") UUID trainingId) {
        return trainingApplicationService.getTraining(trainingId)
                .map(TrainingResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Training fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<TrainingResponse>>>> listTrainings() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> trainingApplicationService.listTrainings(
                        context.tenantId(), context.organizationId()))
                .map(TrainingResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Trainings fetched.")));
    }

    @PostMapping("/{trainingId}/start")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> startTraining(
            @PathVariable("trainingId") UUID trainingId) {
        return trainingApplicationService.startTraining(trainingId)
                .map(TrainingResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Training started.")));
    }

    @PostMapping("/{trainingId}/complete")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<TrainingResponse>>> completeTraining(
            @PathVariable("trainingId") UUID trainingId) {
        return trainingApplicationService.completeTraining(trainingId)
                .map(TrainingResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Training completed.")));
    }
}
