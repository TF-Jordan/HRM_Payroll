package yowyob.comops.api.hrm.domain.service;

import java.math.BigDecimal;

public record PayslipLineData(
        String code,
        String label,
        String lineType,
        BigDecimal base,
        BigDecimal rate,
        BigDecimal employeeAmount,
        BigDecimal employerAmount,
        int sortOrder) {

    public static PayslipLineData earning(String code, String label, BigDecimal amount, int sortOrder) {
        return new PayslipLineData(code, label, "EARNING", amount, null, amount, BigDecimal.ZERO, sortOrder);
    }

    public static PayslipLineData deduction(String code, String label, BigDecimal base, BigDecimal rate,
                                            BigDecimal employeeAmount, int sortOrder) {
        return new PayslipLineData(code, label, "DEDUCTION", base, rate, employeeAmount, BigDecimal.ZERO, sortOrder);
    }

    public static PayslipLineData employerCharge(String code, String label, BigDecimal base, BigDecimal rate,
                                                 BigDecimal employerAmount, int sortOrder) {
        return new PayslipLineData(code, label, "EMPLOYER_CHARGE", base, rate, BigDecimal.ZERO, employerAmount, sortOrder);
    }
}
