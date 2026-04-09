package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record CreatePayrollRunCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String period,
        String currency) {
}
