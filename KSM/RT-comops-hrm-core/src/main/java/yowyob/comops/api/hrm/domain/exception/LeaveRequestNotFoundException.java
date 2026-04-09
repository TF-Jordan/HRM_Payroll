package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class LeaveRequestNotFoundException extends RuntimeException {

    public LeaveRequestNotFoundException(UUID leaveRequestId) {
        super("Leave request not found: " + leaveRequestId);
    }
}
