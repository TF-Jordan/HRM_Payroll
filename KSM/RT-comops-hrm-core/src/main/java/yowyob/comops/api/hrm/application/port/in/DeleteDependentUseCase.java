package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface DeleteDependentUseCase {

    Mono<Void> deleteDependent(UUID dependentId);
}
