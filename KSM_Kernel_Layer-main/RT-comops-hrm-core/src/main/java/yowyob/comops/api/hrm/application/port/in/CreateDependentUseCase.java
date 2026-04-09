package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Dependent;
import reactor.core.publisher.Mono;

public interface CreateDependentUseCase {

    Mono<Dependent> createDependent(CreateDependentCommand command);
}
