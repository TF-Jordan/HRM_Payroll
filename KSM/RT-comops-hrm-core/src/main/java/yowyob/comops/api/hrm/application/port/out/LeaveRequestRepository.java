package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRequestRepository {

    Mono<LeaveRequest> findById(UUID leaveRequestId);

    Flux<LeaveRequest> findByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LeaveRequest> findByOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status);

    Mono<LeaveRequest> save(LeaveRequest leaveRequest);
}
