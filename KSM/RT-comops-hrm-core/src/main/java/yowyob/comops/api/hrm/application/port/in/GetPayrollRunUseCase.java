package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.PayrollRun;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetPayrollRunUseCase {

    Mono<PayrollRun> getPayrollRun(UUID payrollRunId);

    Flux<PayrollRun> listPayrollRuns(UUID tenantId, UUID organizationId);
}
