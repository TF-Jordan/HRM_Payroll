package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestResponse(
        UUID id,
        UUID employeeId,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal daysRequested,
        String reason,
        String status,
        UUID approvedBy,
        Instant approvedAt,
        String rejectionReason) {

    public static LeaveRequestResponse from(LeaveRequest request) {
        return new LeaveRequestResponse(request.id(), request.employeeId(), request.leaveType(),
                request.startDate(), request.endDate(), request.daysRequested(), request.reason(),
                request.status(), request.approvedBy(), request.approvedAt(), request.rejectionReason());
    }
}
