package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Dependent;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DependentRepository {

    Mono<Dependent> findById(UUID dependentId);

    Flux<Dependent> findByEmployeeId(UUID tenantId, UUID employeeId);

    Mono<Long> countChildrenByEmployeeId(UUID tenantId, UUID employeeId);

    Mono<Dependent> save(Dependent dependent);

    Mono<Void> deleteById(UUID dependentId);
}
