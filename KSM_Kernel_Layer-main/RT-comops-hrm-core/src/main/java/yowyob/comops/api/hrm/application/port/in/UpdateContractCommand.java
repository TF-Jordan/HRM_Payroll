package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateContractCommand(
        UUID tenantId,
        UUID contractId,
        String contractType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseSalary,
        String currency) {
}
