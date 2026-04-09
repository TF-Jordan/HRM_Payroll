package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContractSpringDataRepository extends ReactiveCrudRepository<ContractEntity, UUID> {

    Flux<ContractEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Mono<ContractEntity> findByTenantIdAndEmployeeIdAndStatus(UUID tenantId, UUID employeeId, String status);

    Flux<ContractEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
