package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.LoanAdvance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanAdvanceResponse(
        UUID id,
        UUID employeeId,
        String loanType,
        BigDecimal amount,
        String currency,
        BigDecimal monthlyDeduction,
        BigDecimal totalRepaid,
        BigDecimal remainingBalance,
        int installmentsCount,
        int installmentsPaid,
        String status,
        UUID approvedBy,
        Instant approvedAt) {

    public static LoanAdvanceResponse from(LoanAdvance loan) {
        return new LoanAdvanceResponse(loan.id(), loan.employeeId(), loan.loanType(), loan.amount(),
                loan.currency(), loan.monthlyDeduction(), loan.totalRepaid(), loan.remainingBalance(),
                loan.installmentsCount(), loan.installmentsPaid(), loan.status(), loan.approvedBy(),
                loan.approvedAt());
    }
}
