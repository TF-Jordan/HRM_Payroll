package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateLeaveRequestCommand(
        UUID tenantId,
        UUID organizationId,
        UUID employeeId,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal daysRequested,
        String reason) {
}
