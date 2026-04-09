package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.PayrollRun;
import reactor.core.publisher.Mono;

public interface CreatePayrollRunUseCase {

    Mono<PayrollRun> createPayrollRun(CreatePayrollRunCommand command);
}
