package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ApproveLeaveRequestUseCase {

    Mono<LeaveRequest> approveLeaveRequest(UUID leaveRequestId, UUID approvedBy);

    Mono<LeaveRequest> rejectLeaveRequest(UUID leaveRequestId, UUID rejectedBy, String reason);
}
