package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Contract;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ActivateContractUseCase {

    Mono<Contract> activateContract(UUID contractId);
}
