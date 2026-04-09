package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreatePayrollRunCommand;
import yowyob.comops.api.hrm.application.port.in.CreatePayrollRunUseCase;
import yowyob.comops.api.hrm.application.port.in.GetPayrollRunUseCase;
import yowyob.comops.api.hrm.application.port.in.ValidatePayrollRunUseCase;
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
@RequestMapping("/api/hrm/payroll-runs")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class PayrollRunController {

    private final CreatePayrollRunUseCase createPayrollRunUseCase;
    private final GetPayrollRunUseCase getPayrollRunUseCase;
    private final ValidatePayrollRunUseCase validatePayrollRunUseCase;

    public PayrollRunController(CreatePayrollRunUseCase createPayrollRunUseCase,
                                GetPayrollRunUseCase getPayrollRunUseCase,
                                ValidatePayrollRunUseCase validatePayrollRunUseCase) {
        this.createPayrollRunUseCase = createPayrollRunUseCase;
        this.getPayrollRunUseCase = getPayrollRunUseCase;
        this.validatePayrollRunUseCase = validatePayrollRunUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> createPayrollRun(
            @Valid @RequestBody Mono<CreatePayrollRunRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return createPayrollRunUseCase.createPayrollRun(new CreatePayrollRunCommand(
                            context.tenantId(), context.organizationId(),
                            request.agencyId(), request.period(), request.currency()));
                })
                .map(PayrollRunResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Payroll run created.")));
    }

    @GetMapping("/{payrollRunId}")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> getPayrollRun(
            @PathVariable("payrollRunId") UUID payrollRunId) {
        return getPayrollRunUseCase.getPayrollRun(payrollRunId)
                .map(PayrollRunResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Payroll run fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<PayrollRunResponse>>>> listPayrollRuns() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> getPayrollRunUseCase.listPayrollRuns(
                        context.tenantId(), context.organizationId()))
                .map(PayrollRunResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Payroll runs fetched.")));
    }

    @PostMapping("/{payrollRunId}/validate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> validatePayrollRun(
            @PathVariable("payrollRunId") UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> validatePayrollRunUseCase.validatePayrollRun(payrollRunId,
                        context.userId()))
                .map(PayrollRunResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Payroll run validated.")));
    }
}
