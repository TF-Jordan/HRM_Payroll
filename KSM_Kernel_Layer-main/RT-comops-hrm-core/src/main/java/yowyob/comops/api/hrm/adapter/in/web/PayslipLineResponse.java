package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.PayslipLine;

import java.math.BigDecimal;
import java.util.UUID;

public record PayslipLineResponse(
        UUID id,
        UUID payrollEntryId,
        String code,
        String label,
        String lineType,
        BigDecimal base,
        BigDecimal rate,
        BigDecimal employeeAmount,
        BigDecimal employerAmount,
        int sortOrder) {

    public static PayslipLineResponse from(PayslipLine line) {
        return new PayslipLineResponse(line.id(), line.payrollEntryId(), line.code(), line.label(),
                line.lineType(), line.base(), line.rate(), line.employeeAmount(), line.employerAmount(),
                line.sortOrder());
    }
}
