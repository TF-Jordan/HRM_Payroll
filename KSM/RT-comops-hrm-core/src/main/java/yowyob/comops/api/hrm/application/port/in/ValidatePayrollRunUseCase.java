package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.PayrollRun;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ValidatePayrollRunUseCase {

    Mono<PayrollRun> validatePayrollRun(UUID payrollRunId, UUID validatedBy);
}
