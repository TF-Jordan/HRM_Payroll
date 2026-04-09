package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateDependentCommand;
import yowyob.comops.api.hrm.application.port.in.CreateDependentUseCase;
import yowyob.comops.api.hrm.application.port.in.DeleteDependentUseCase;
import yowyob.comops.api.hrm.application.port.in.ListDependentsUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/hrm/dependents")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class DependentController {

    private final CreateDependentUseCase createDependentUseCase;
    private final ListDependentsUseCase listDependentsUseCase;
    private final DeleteDependentUseCase deleteDependentUseCase;

    public DependentController(CreateDependentUseCase createDependentUseCase,
                               ListDependentsUseCase listDependentsUseCase,
                               DeleteDependentUseCase deleteDependentUseCase) {
        this.createDependentUseCase = createDependentUseCase;
        this.listDependentsUseCase = listDependentsUseCase;
        this.deleteDependentUseCase = deleteDependentUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<DependentResponse>>> createDependent(
            @Valid @RequestBody Mono<CreateDependentRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return createDependentUseCase.createDependent(new CreateDependentCommand(
                            context.tenantId(), context.organizationId(), request.employeeId(),
                            request.firstName(), request.lastName(), request.relationship(),
                            request.birthDate(), request.gender()));
                })
                .map(DependentResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Dependent created.")));
    }

    @GetMapping("/employee/{employeeId}")
    public Mono<ResponseEntity<ApiResponse<List<DependentResponse>>>> listDependents(
            @PathVariable("employeeId") UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> listDependentsUseCase.listDependents(context.tenantId(), employeeId))
                .map(DependentResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Dependents fetched.")));
    }

    @DeleteMapping("/{dependentId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteDependent(
            @PathVariable("dependentId") UUID dependentId) {
        return deleteDependentUseCase.deleteDependent(dependentId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Dependent deleted.")));
    }
}
