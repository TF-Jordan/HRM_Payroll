package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Dependent;

import java.util.UUID;

import reactor.core.publisher.Flux;

public interface ListDependentsUseCase {

    Flux<Dependent> listDependents(UUID tenantId, UUID employeeId);
}
