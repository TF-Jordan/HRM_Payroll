package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import reactor.core.publisher.Mono;

public interface CreateLoanAdvanceUseCase {

    Mono<LoanAdvance> createLoanAdvance(CreateLoanAdvanceCommand command);
}
