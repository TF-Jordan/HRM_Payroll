package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.LeaveRequest;
import reactor.core.publisher.Mono;

public interface CreateLeaveRequestUseCase {

    Mono<LeaveRequest> createLeaveRequest(CreateLeaveRequestCommand command);
}
