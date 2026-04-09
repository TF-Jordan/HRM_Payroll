package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Contract;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContractRepository {

    Mono<Contract> findById(UUID contractId);

    Flux<Contract> findByEmployeeId(UUID tenantId, UUID employeeId);

    Mono<Contract> findActiveByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<Contract> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Contract> save(Contract contract);
}
