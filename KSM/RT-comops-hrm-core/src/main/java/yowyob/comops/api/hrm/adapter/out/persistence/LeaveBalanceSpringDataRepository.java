package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveBalanceSpringDataRepository extends ReactiveCrudRepository<LeaveBalanceEntity, UUID> {

    Flux<LeaveBalanceEntity> findAllByTenantIdAndEmployeeIdAndYear(UUID tenantId, UUID employeeId, int year);

    Mono<LeaveBalanceEntity> findByTenantIdAndEmployeeIdAndLeaveTypeAndYear(UUID tenantId, UUID employeeId,
            String leaveType, int year);

    Flux<LeaveBalanceEntity> findAllByTenantIdAndOrganizationIdAndYear(UUID tenantId, UUID organizationId, int year);
}
