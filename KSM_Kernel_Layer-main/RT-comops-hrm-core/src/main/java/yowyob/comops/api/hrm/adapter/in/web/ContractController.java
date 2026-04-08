package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.ActivateContractUseCase;
import yowyob.comops.api.hrm.application.port.in.CreateContractCommand;
import yowyob.comops.api.hrm.application.port.in.CreateContractUseCase;
import yowyob.comops.api.hrm.application.port.in.GetContractUseCase;
import yowyob.comops.api.hrm.application.port.in.UpdateContractCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateContractUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/hrm/contracts")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class ContractController {

    private final CreateContractUseCase createContractUseCase;
    private final GetContractUseCase getContractUseCase;
    private final UpdateContractUseCase updateContractUseCase;
    private final ActivateContractUseCase activateContractUseCase;

    public ContractController(CreateContractUseCase createContractUseCase,
                              GetContractUseCase getContractUseCase,
                              UpdateContractUseCase updateContractUseCase,
                              ActivateContractUseCase activateContractUseCase) {
        this.createContractUseCase = createContractUseCase;
        this.getContractUseCase = getContractUseCase;
        this.updateContractUseCase = updateContractUseCase;
        this.activateContractUseCase = activateContractUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> createContract(
            @Valid @RequestBody Mono<CreateContractRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return createContractUseCase.createContract(new CreateContractCommand(
                            context.tenantId(), context.organizationId(), request.employeeId(),
                            request.contractType(), request.startDate(), request.endDate(),
                            request.baseSalary(), request.currency()));
                })
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Contract created.")));
    }

    @GetMapping("/{contractId}")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> getContract(
            @PathVariable("contractId") UUID contractId) {
        return getContractUseCase.getContract(contractId)
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contract fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    public Mono<ResponseEntity<ApiResponse<List<ContractResponse>>>> getContractsByEmployee(
            @PathVariable("employeeId") UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> getContractUseCase.getContractsByEmployee(context.tenantId(), employeeId))
                .map(ContractResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contracts fetched.")));
    }

    @PatchMapping("/{contractId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> updateContract(
            @PathVariable("contractId") UUID contractId,
            @Valid @RequestBody Mono<UpdateContractRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return updateContractUseCase.updateContract(new UpdateContractCommand(
                            context.tenantId(), contractId, request.contractType(), request.startDate(),
                            request.endDate(), request.baseSalary(), request.currency()));
                })
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contract updated.")));
    }

    @PostMapping("/{contractId}/activate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<ContractResponse>>> activateContract(
            @PathVariable("contractId") UUID contractId) {
        return activateContractUseCase.activateContract(contractId)
                .map(ContractResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Contract activated.")));
    }
}
