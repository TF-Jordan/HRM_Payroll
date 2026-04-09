package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.LeaveBalance;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveBalanceRepository {

    Mono<LeaveBalance> findById(UUID leaveBalanceId);

    Flux<LeaveBalance> findByEmployeeIdAndYear(UUID tenantId, UUID employeeId, int year);

    Mono<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(UUID tenantId, UUID employeeId,
            String leaveType, int year);

    Flux<LeaveBalance> findByOrganizationIdAndYear(UUID tenantId, UUID organizationId, int year);

    Mono<LeaveBalance> save(LeaveBalance leaveBalance);
}
