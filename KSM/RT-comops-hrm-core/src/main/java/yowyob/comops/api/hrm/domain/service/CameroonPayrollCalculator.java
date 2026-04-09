package yowyob.comops.api.hrm.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure domain service implementing Cameroon payroll calculation rules.
 *
 * <h3>CNPS (Caisse Nationale de Prévoyance Sociale)</h3>
 * <ul>
 *   <li>Monthly ceiling: 750,000 XAF</li>
 *   <li>Employee PV (Pension Vieillesse): 4.2%</li>
 *   <li>Employer PV: 4.2%</li>
 *   <li>Employer AF (Allocations Familiales): 7%</li>
 *   <li>Employer AT (Accidents du Travail): 1.75% – 5% (risk-dependent)</li>
 * </ul>
 *
 * <h3>IRPP (Impôt sur le Revenu des Personnes Physiques)</h3>
 * Progressive annual brackets: 10% / 15% / 25% / 35%.
 *
 * <h3>CAC (Centimes Additionnels Communaux)</h3>
 * 10% of IRPP.
 */
public final class CameroonPayrollCalculator {

    private static final BigDecimal CNPS_MONTHLY_CEILING = new BigDecimal("750000");

    private static final BigDecimal CNPS_EMPLOYEE_PV_RATE = new BigDecimal("0.042");
    private static final BigDecimal CNPS_EMPLOYER_PV_RATE = new BigDecimal("0.042");
    private static final BigDecimal CNPS_EMPLOYER_AF_RATE = new BigDecimal("0.07");
    private static final BigDecimal DEFAULT_AT_RATE = new BigDecimal("0.0175");

    private static final BigDecimal CAC_RATE = new BigDecimal("0.10");

    private static final BigDecimal CHILD_ALLOWANCE_PER_CHILD = new BigDecimal("2800");

    private static final int SCALE = 0;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);

    // IRPP annual brackets
    private static final BigDecimal BRACKET_1_LIMIT = new BigDecimal("2000000");
    private static final BigDecimal BRACKET_2_LIMIT = new BigDecimal("3000000");
    private static final BigDecimal BRACKET_3_LIMIT = new BigDecimal("5000000");
    private static final BigDecimal RATE_1 = new BigDecimal("0.10");
    private static final BigDecimal RATE_2 = new BigDecimal("0.15");
    private static final BigDecimal RATE_3 = new BigDecimal("0.25");
    private static final BigDecimal RATE_4 = new BigDecimal("0.35");

    private CameroonPayrollCalculator() {
    }

    public static PayrollCalculationResult calculate(BigDecimal grossSalary, int childCount,
                                                     BigDecimal atRate, BigDecimal loanDeduction) {
        BigDecimal effectiveAtRate = atRate != null ? atRate : DEFAULT_AT_RATE;
        BigDecimal effectiveLoanDeduction = loanDeduction != null ? loanDeduction : BigDecimal.ZERO;
        List<PayslipLineData> lines = new ArrayList<>();
        int order = 1;

        // --- EARNINGS ---
        lines.add(PayslipLineData.earning("BASE_SALARY", "Salaire de base", grossSalary, order++));

        // --- CNPS EMPLOYEE DEDUCTION ---
        BigDecimal cnpsBase = grossSalary.min(CNPS_MONTHLY_CEILING);
        BigDecimal cnpsEmployeePV = cnpsBase.multiply(CNPS_EMPLOYEE_PV_RATE).setScale(SCALE, RM);
        lines.add(PayslipLineData.deduction("CNPS_PV_EMP", "CNPS Pension Vieillesse (salarié)",
                cnpsBase, CNPS_EMPLOYEE_PV_RATE, cnpsEmployeePV, order++));

        // --- CNPS EMPLOYER CHARGES ---
        BigDecimal cnpsEmployerPV = cnpsBase.multiply(CNPS_EMPLOYER_PV_RATE).setScale(SCALE, RM);
        lines.add(PayslipLineData.employerCharge("CNPS_PV_EMP_ER", "CNPS Pension Vieillesse (employeur)",
                cnpsBase, CNPS_EMPLOYER_PV_RATE, cnpsEmployerPV, order++));

        BigDecimal cnpsEmployerAF = cnpsBase.multiply(CNPS_EMPLOYER_AF_RATE).setScale(SCALE, RM);
        lines.add(PayslipLineData.employerCharge("CNPS_AF", "CNPS Allocations Familiales",
                cnpsBase, CNPS_EMPLOYER_AF_RATE, cnpsEmployerAF, order++));

        BigDecimal cnpsEmployerAT = cnpsBase.multiply(effectiveAtRate).setScale(SCALE, RM);
        lines.add(PayslipLineData.employerCharge("CNPS_AT", "CNPS Accidents du Travail",
                cnpsBase, effectiveAtRate, cnpsEmployerAT, order++));

        // --- IRPP (progressive, annualized then /12) ---
        BigDecimal taxableMonthly = grossSalary.subtract(cnpsEmployeePV);
        BigDecimal annualTaxable = taxableMonthly.multiply(TWELVE);
        BigDecimal annualIRPP = computeProgressiveIRPP(annualTaxable);
        BigDecimal monthlyIRPP = annualIRPP.divide(TWELVE, SCALE, RM);
        lines.add(PayslipLineData.deduction("IRPP", "Impôt sur le Revenu (IRPP)",
                taxableMonthly, null, monthlyIRPP, order++));

        // --- CAC ---
        BigDecimal monthlyCac = monthlyIRPP.multiply(CAC_RATE).setScale(SCALE, RM);
        lines.add(PayslipLineData.deduction("CAC", "Centimes Additionnels Communaux",
                monthlyIRPP, CAC_RATE, monthlyCac, order++));

        // --- CHILD ALLOWANCE (earning) ---
        BigDecimal childAllowance = BigDecimal.ZERO;
        if (childCount > 0) {
            childAllowance = CHILD_ALLOWANCE_PER_CHILD.multiply(BigDecimal.valueOf(childCount));
            lines.add(PayslipLineData.earning("CHILD_ALLOWANCE", "Allocations familiales enfants",
                    childAllowance, order++));
        }

        // --- LOAN REPAYMENT DEDUCTION ---
        if (effectiveLoanDeduction.signum() > 0) {
            lines.add(PayslipLineData.deduction("LOAN_REPAYMENT", "Remboursement prêt/avance",
                    effectiveLoanDeduction, null, effectiveLoanDeduction, order++));
        }

        // --- TOTALS ---
        BigDecimal totalDeductions = cnpsEmployeePV.add(monthlyIRPP).add(monthlyCac).add(effectiveLoanDeduction);
        BigDecimal totalEmployerCharges = cnpsEmployerPV.add(cnpsEmployerAF).add(cnpsEmployerAT);
        BigDecimal netSalary = grossSalary.subtract(totalDeductions).add(childAllowance);

        return new PayrollCalculationResult(grossSalary, netSalary, totalDeductions,
                totalEmployerCharges, List.copyOf(lines));
    }

    static BigDecimal computeProgressiveIRPP(BigDecimal annualTaxable) {
        if (annualTaxable.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal remaining = annualTaxable;

        // Bracket 1: 0 – 2,000,000 at 10%
        BigDecimal bracket1 = remaining.min(BRACKET_1_LIMIT);
        tax = tax.add(bracket1.multiply(RATE_1));
        remaining = remaining.subtract(bracket1);

        if (remaining.signum() <= 0) {
            return tax.setScale(SCALE, RM);
        }

        // Bracket 2: 2,000,001 – 3,000,000 at 15%
        BigDecimal bracket2Width = BRACKET_2_LIMIT.subtract(BRACKET_1_LIMIT);
        BigDecimal bracket2 = remaining.min(bracket2Width);
        tax = tax.add(bracket2.multiply(RATE_2));
        remaining = remaining.subtract(bracket2);

        if (remaining.signum() <= 0) {
            return tax.setScale(SCALE, RM);
        }

        // Bracket 3: 3,000,001 – 5,000,000 at 25%
        BigDecimal bracket3Width = BRACKET_3_LIMIT.subtract(BRACKET_2_LIMIT);
        BigDecimal bracket3 = remaining.min(bracket3Width);
        tax = tax.add(bracket3.multiply(RATE_3));
        remaining = remaining.subtract(bracket3);

        if (remaining.signum() <= 0) {
            return tax.setScale(SCALE, RM);
        }

        // Bracket 4: > 5,000,000 at 35%
        tax = tax.add(remaining.multiply(RATE_4));
        return tax.setScale(SCALE, RM);
    }
}
