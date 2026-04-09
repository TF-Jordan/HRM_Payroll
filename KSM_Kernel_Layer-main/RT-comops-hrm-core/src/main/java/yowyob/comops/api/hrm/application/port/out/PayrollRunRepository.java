package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.PayrollRun;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRunRepository {

    Mono<PayrollRun> findById(UUID payrollRunId);

    Flux<PayrollRun> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Boolean> existsByPeriod(UUID tenantId, UUID organizationId, String period);

    Mono<PayrollRun> save(PayrollRun payrollRun);
}
