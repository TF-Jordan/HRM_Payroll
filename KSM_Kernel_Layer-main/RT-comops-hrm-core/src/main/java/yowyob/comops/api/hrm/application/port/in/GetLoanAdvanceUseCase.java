package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LoanAdvance;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetLoanAdvanceUseCase {

    Mono<LoanAdvance> getLoanAdvance(UUID loanId);

    Flux<LoanAdvance> getLoansByEmployee(UUID tenantId, UUID employeeId);
}
