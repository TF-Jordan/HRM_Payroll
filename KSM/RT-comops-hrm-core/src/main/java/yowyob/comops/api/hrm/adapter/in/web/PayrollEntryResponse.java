package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.PayrollEntry;

import java.math.BigDecimal;
import java.util.UUID;

public record PayrollEntryResponse(
        UUID id,
        UUID payrollRunId,
        UUID employeeId,
        UUID contractId,
        BigDecimal baseSalary,
        BigDecimal grossSalary,
        BigDecimal netSalary,
        BigDecimal totalDeductions,
        BigDecimal totalEmployerCharges,
        String currency) {

    public static PayrollEntryResponse from(PayrollEntry entry) {
        return new PayrollEntryResponse(entry.id(), entry.payrollRunId(), entry.employeeId(),
                entry.contractId(), entry.baseSalary(), entry.grossSalary(), entry.netSalary(),
                entry.totalDeductions(), entry.totalEmployerCharges(), entry.currency());
    }
}
