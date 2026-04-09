package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LoanAdvanceSpringDataRepository extends ReactiveCrudRepository<LoanAdvanceEntity, UUID> {

    Flux<LoanAdvanceEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LoanAdvanceEntity> findAllByTenantIdAndEmployeeIdAndStatus(UUID tenantId, UUID employeeId, String status);

    Flux<LoanAdvanceEntity> findAllByTenantIdAndOrganizationIdAndStatus(UUID tenantId, UUID organizationId,
            String status);
}
