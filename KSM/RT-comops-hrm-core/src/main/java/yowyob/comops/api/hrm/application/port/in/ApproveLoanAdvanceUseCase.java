package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LoanAdvance;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ApproveLoanAdvanceUseCase {

    Mono<LoanAdvance> approveLoanAdvance(UUID loanId, UUID approvedBy);
}
