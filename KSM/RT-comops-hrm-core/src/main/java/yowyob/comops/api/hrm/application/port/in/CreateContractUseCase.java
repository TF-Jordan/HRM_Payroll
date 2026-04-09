package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Contract;
import reactor.core.publisher.Mono;

public interface CreateContractUseCase {

    Mono<Contract> createContract(CreateContractCommand command);
}
