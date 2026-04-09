package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanAdvanceCommand(
        UUID tenantId,
        UUID organizationId,
        UUID employeeId,
        String loanType,
        BigDecimal amount,
        String currency,
        BigDecimal monthlyDeduction,
        int installmentsCount) {
}
