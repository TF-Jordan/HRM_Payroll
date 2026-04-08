package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Contract;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetContractUseCase {

    Mono<Contract> getContract(UUID contractId);

    Flux<Contract> getContractsByEmployee(UUID tenantId, UUID employeeId);
}
