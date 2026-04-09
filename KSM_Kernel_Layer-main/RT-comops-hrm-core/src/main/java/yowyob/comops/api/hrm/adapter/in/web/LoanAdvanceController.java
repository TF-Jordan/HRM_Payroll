package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.ApproveLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.in.CreateLoanAdvanceCommand;
import yowyob.comops.api.hrm.application.port.in.CreateLoanAdvanceUseCase;
import yowyob.comops.api.hrm.application.port.in.GetLoanAdvanceUseCase;
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
@RequestMapping("/api/hrm/loans")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class LoanAdvanceController {

    private final CreateLoanAdvanceUseCase createLoanAdvanceUseCase;
    private final ApproveLoanAdvanceUseCase approveLoanAdvanceUseCase;
    private final GetLoanAdvanceUseCase getLoanAdvanceUseCase;

    public LoanAdvanceController(CreateLoanAdvanceUseCase createLoanAdvanceUseCase,
                                 ApproveLoanAdvanceUseCase approveLoanAdvanceUseCase,
                                 GetLoanAdvanceUseCase getLoanAdvanceUseCase) {
        this.createLoanAdvanceUseCase = createLoanAdvanceUseCase;
        this.approveLoanAdvanceUseCase = approveLoanAdvanceUseCase;
        this.getLoanAdvanceUseCase = getLoanAdvanceUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> createLoanAdvance(
            @Valid @RequestBody Mono<CreateLoanAdvanceRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return createLoanAdvanceUseCase.createLoanAdvance(new CreateLoanAdvanceCommand(
                            context.tenantId(), context.organizationId(), request.employeeId(),
                            request.loanType(), request.amount(), request.currency(),
                            request.monthlyDeduction(), request.installmentsCount()));
                })
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Loan advance created.")));
    }

    @GetMapping("/{loanId}")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> getLoanAdvance(
            @PathVariable("loanId") UUID loanId) {
        return getLoanAdvanceUseCase.getLoanAdvance(loanId)
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Loan advance fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    public Mono<ResponseEntity<ApiResponse<List<LoanAdvanceResponse>>>> getLoansByEmployee(
            @PathVariable("employeeId") UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> getLoanAdvanceUseCase.getLoansByEmployee(context.tenantId(), employeeId))
                .map(LoanAdvanceResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Loans fetched.")));
    }

    @PostMapping("/{loanId}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<LoanAdvanceResponse>>> approveLoanAdvance(
            @PathVariable("loanId") UUID loanId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> approveLoanAdvanceUseCase.approveLoanAdvance(loanId, context.userId()))
                .map(LoanAdvanceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Loan advance approved.")));
    }
}
