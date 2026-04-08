package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateContractCommand(
        UUID tenantId,
        UUID organizationId,
        UUID employeeId,
        String contractType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseSalary,
        String currency) {
}
