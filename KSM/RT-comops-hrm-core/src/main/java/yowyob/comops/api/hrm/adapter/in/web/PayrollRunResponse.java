package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.PayrollRun;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayrollRunResponse(
        UUID id,
        UUID organizationId,
        UUID agencyId,
        String period,
        String status,
        Instant calculatedAt,
        UUID validatedBy,
        Instant validatedAt,
        Instant paidAt,
        BigDecimal totalGross,
        BigDecimal totalNet,
        BigDecimal totalEmployerCharges,
        String currency,
        int employeeCount) {

    public static PayrollRunResponse from(PayrollRun run) {
        return new PayrollRunResponse(run.id(), run.organizationId(), run.agencyId(), run.period(),
                run.status(), run.calculatedAt(), run.validatedBy(), run.validatedAt(), run.paidAt(),
                run.totalGross(), run.totalNet(), run.totalEmployerCharges(), run.currency(),
                run.employeeCount());
    }
}
