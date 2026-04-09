package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for monthly leave accrual per Cameroon labor code.
 *
 * <h3>Base accrual</h3>
 * 1.5 working days per month of service.
 *
 * <h3>Seniority bonus (annual extra days)</h3>
 * <ul>
 *   <li>5–9 years: +1 day/year</li>
 *   <li>10–14 years: +2 days/year</li>
 *   <li>15–19 years: +3 days/year</li>
 *   <li>20+ years: +5 days/year</li>
 * </ul>
 * Monthly seniority bonus = annual bonus / 12.
 *
 * <h3>Child bonus</h3>
 * +1 extra day per child (max 3) per year, computed monthly.
 */
@Service
public class LeaveAccrualService {

    private static final BigDecimal BASE_MONTHLY_ACCRUAL = new BigDecimal("1.5");
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final String ANNUAL_LEAVE = "ANNUAL";

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final DependentRepository dependentRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public LeaveAccrualService(EmployeeRepository employeeRepository,
                               LeaveBalanceRepository leaveBalanceRepository,
                               DependentRepository dependentRepository,
                               ReactiveTransactionalExecutor transactionalExecutor) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.dependentRepository = dependentRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    public Flux<LeaveBalance> accrueMonthlyLeave(java.util.UUID tenantId, java.util.UUID organizationId) {
        int year = LocalDate.now().getYear();

        return employeeRepository.findByOrganizationId(tenantId, organizationId)
                .filter(employee -> "ACTIVE".equals(employee.status()))
                .flatMap(employee -> transactionalExecutor.transactional(
                        accrueForEmployee(employee, year)));
    }

    private Mono<LeaveBalance> accrueForEmployee(Employee employee, int year) {
        return dependentRepository.countChildrenByEmployeeId(employee.tenantId(), employee.id())
                .defaultIfEmpty(0L)
                .flatMap(childCount -> {
                    BigDecimal monthlyAccrual = computeMonthlyAccrual(employee, childCount.intValue());

                    return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                                    employee.tenantId(), employee.id(), ANNUAL_LEAVE, year)
                            .defaultIfEmpty(LeaveBalance.create(employee.tenantId(),
                                    employee.organizationId(), employee.id(), ANNUAL_LEAVE, year))
                            .map(balance -> balance.accrue(monthlyAccrual))
                            .flatMap(leaveBalanceRepository::save);
                });
    }

    static BigDecimal computeMonthlyAccrual(Employee employee, int childCount) {
        BigDecimal accrual = BASE_MONTHLY_ACCRUAL;

        // Seniority bonus
        long yearsOfService = ChronoUnit.YEARS.between(employee.hireDate(), LocalDate.now());
        BigDecimal annualSeniorityBonus;
        if (yearsOfService >= 20) {
            annualSeniorityBonus = BigDecimal.valueOf(5);
        } else if (yearsOfService >= 15) {
            annualSeniorityBonus = BigDecimal.valueOf(3);
        } else if (yearsOfService >= 10) {
            annualSeniorityBonus = BigDecimal.valueOf(2);
        } else if (yearsOfService >= 5) {
            annualSeniorityBonus = BigDecimal.ONE;
        } else {
            annualSeniorityBonus = BigDecimal.ZERO;
        }
        accrual = accrual.add(annualSeniorityBonus.divide(TWELVE, 4, java.math.RoundingMode.HALF_UP));

        // Child bonus: +1 day/year per child, max 3 children
        int eligibleChildren = Math.min(childCount, 3);
        if (eligibleChildren > 0) {
            BigDecimal childBonus = BigDecimal.valueOf(eligibleChildren).divide(TWELVE, 4,
                    java.math.RoundingMode.HALF_UP);
            accrual = accrual.add(childBonus);
        }

        return accrual;
    }
}
