package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Contract;
import reactor.core.publisher.Mono;

public interface UpdateContractUseCase {

    Mono<Contract> updateContract(UpdateContractCommand command);
}
