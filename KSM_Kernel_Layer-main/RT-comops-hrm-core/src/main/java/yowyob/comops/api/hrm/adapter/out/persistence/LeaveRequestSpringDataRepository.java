package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LeaveRequestSpringDataRepository extends ReactiveCrudRepository<LeaveRequestEntity, UUID> {

    Flux<LeaveRequestEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LeaveRequestEntity> findAllByTenantIdAndOrganizationIdAndStatus(UUID tenantId, UUID organizationId,
            String status);
}
