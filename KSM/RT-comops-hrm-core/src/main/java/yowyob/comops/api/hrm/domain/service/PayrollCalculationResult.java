package yowyob.comops.api.hrm.domain.service;

import java.math.BigDecimal;
import java.util.List;

public record PayrollCalculationResult(
        BigDecimal grossSalary,
        BigDecimal netSalary,
        BigDecimal totalDeductions,
        BigDecimal totalEmployerCharges,
        List<PayslipLineData> lines) {
}
