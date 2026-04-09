package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.hrm.application.port.out.PayrollRunRepository;
import yowyob.comops.api.hrm.application.port.out.PayslipLineRepository;
import yowyob.comops.api.hrm.domain.exception.InvalidPayrollStateException;
import yowyob.comops.api.hrm.domain.exception.PayrollRunNotFoundException;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import yowyob.comops.api.hrm.domain.model.PayrollEntry;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.hrm.domain.model.PayslipLine;
import yowyob.comops.api.hrm.domain.service.CameroonPayrollCalculator;
import yowyob.comops.api.hrm.domain.service.PayrollCalculationResult;
import yowyob.comops.api.hrm.domain.service.PayslipLineData;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PayrollCalculationService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEntryRepository payrollEntryRepository;
    private final PayslipLineRepository payslipLineRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final DependentRepository dependentRepository;
    private final LoanAdvanceRepository loanAdvanceRepository;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public PayrollCalculationService(PayrollRunRepository payrollRunRepository,
                                     PayrollEntryRepository payrollEntryRepository,
                                     PayslipLineRepository payslipLineRepository,
                                     EmployeeRepository employeeRepository,
                                     ContractRepository contractRepository,
                                     DependentRepository dependentRepository,
                                     LoanAdvanceRepository loanAdvanceRepository,
                                     BusinessEventPublisher businessEventPublisher,
                                     ReactiveTransactionalExecutor transactionalExecutor) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEntryRepository = payrollEntryRepository;
        this.payslipLineRepository = payslipLineRepository;
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.dependentRepository = dependentRepository;
        this.loanAdvanceRepository = loanAdvanceRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    public Mono<PayrollRun> calculatePayroll(UUID payrollRunId) {
        return transactionalExecutor.transactional(
                payrollRunRepository.findById(payrollRunId)
                        .switchIfEmpty(Mono.error(new PayrollRunNotFoundException(payrollRunId)))
                        .flatMap(run -> {
                            if (!"DRAFT".equals(run.status())) {
                                return Mono.error(new InvalidPayrollStateException(
                                        run.id(), run.status(), "DRAFT"));
                            }
                            return calculateForRun(run);
                        }));
    }

    private Mono<PayrollRun> calculateForRun(PayrollRun run) {
        UUID tenantId = run.tenantId();
        UUID organizationId = run.organizationId();
        String currency = run.currency();

        return employeeRepository.findByOrganizationId(tenantId, organizationId)
                .filter(employee -> "ACTIVE".equals(employee.status()))
                .flatMap(employee -> contractRepository.findActiveByEmployeeId(tenantId, employee.id())
                        .flatMap(contract -> calculateForEmployee(run, contract)))
                .collectList()
                .flatMap(entries -> {
                    BigDecimal totalGross = entries.stream()
                            .map(PayrollEntry::grossSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalNet = entries.stream()
                            .map(PayrollEntry::netSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalEmployerCharges = entries.stream()
                            .map(PayrollEntry::totalEmployerCharges).reduce(BigDecimal.ZERO, BigDecimal::add);
                    int employeeCount = entries.size();

                    PayrollRun calculated = run.markCalculated(totalGross, totalNet,
                            totalEmployerCharges, employeeCount);
                    return payrollRunRepository.save(calculated)
                            .flatMap(saved -> businessEventPublisher.publish(payrollCalculatedEvent(saved))
                                    .thenReturn(saved));
                });
    }

    private Mono<PayrollEntry> calculateForEmployee(PayrollRun run, Contract contract) {
        UUID tenantId = run.tenantId();
        UUID employeeId = contract.employeeId();

        Mono<Long> childCountMono = dependentRepository.countChildrenByEmployeeId(tenantId, employeeId)
                .defaultIfEmpty(0L);

        Mono<BigDecimal> loanDeductionMono = loanAdvanceRepository.findActiveByEmployeeId(tenantId, employeeId)
                .map(LoanAdvance::monthlyDeduction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Mono.zip(childCountMono, loanDeductionMono)
                .flatMap(tuple -> {
                    int childCount = tuple.getT1().intValue();
                    BigDecimal loanDeduction = tuple.getT2();

                    PayrollCalculationResult result = CameroonPayrollCalculator.calculate(
                            contract.baseSalary(), childCount, null, loanDeduction);

                    PayrollEntry entry = PayrollEntry.create(tenantId, run.id(), employeeId,
                            contract.id(), contract.baseSalary(), run.currency());
                    PayrollEntry calculated = entry.withCalculatedAmounts(result.grossSalary(),
                            result.netSalary(), result.totalDeductions(), result.totalEmployerCharges());

                    return payrollEntryRepository.save(calculated)
                            .flatMap(saved -> savePayslipLines(saved, result)
                                    .then(applyLoanRepayments(tenantId, employeeId, loanDeduction))
                                    .thenReturn(saved));
                });
    }

    private Flux<PayslipLine> savePayslipLines(PayrollEntry entry, PayrollCalculationResult result) {
        return Flux.fromIterable(result.lines())
                .map(lineData -> PayslipLine.create(entry.tenantId(), entry.id(), lineData.code(),
                        lineData.label(), lineData.lineType(), lineData.base(), lineData.rate(),
                        lineData.employeeAmount(), lineData.employerAmount(), lineData.sortOrder()))
                .collectList()
                .flatMapMany(payslipLineRepository::saveAll);
    }

    private Mono<Void> applyLoanRepayments(UUID tenantId, UUID employeeId, BigDecimal totalLoanDeduction) {
        if (totalLoanDeduction.signum() <= 0) {
            return Mono.empty();
        }
        return loanAdvanceRepository.findActiveByEmployeeId(tenantId, employeeId)
                .flatMap(loan -> {
                    LoanAdvance repaid = loan.applyRepayment(loan.monthlyDeduction());
                    return loanAdvanceRepository.save(repaid);
                })
                .then();
    }

    private BusinessEvent payrollCalculatedEvent(PayrollRun payrollRun) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("period", payrollRun.period());
        payload.put("totalGross", payrollRun.totalGross());
        payload.put("totalNet", payrollRun.totalNet());
        payload.put("totalEmployerCharges", payrollRun.totalEmployerCharges());
        payload.put("currency", payrollRun.currency());
        payload.put("employeeCount", payrollRun.employeeCount());
        return BusinessEvent.now(payrollRun.tenantId(), payrollRun.organizationId(),
                "PAYROLL_CALCULATED", "PAYROLL_RUN", payrollRun.id(), payload);
    }

    public Flux<PayrollEntry> getPayrollEntries(UUID tenantId, UUID payrollRunId) {
        return payrollEntryRepository.findByPayrollRunId(tenantId, payrollRunId);
    }

    public Flux<PayslipLine> getPayslipLines(UUID tenantId, UUID payrollEntryId) {
        return payslipLineRepository.findByPayrollEntryId(tenantId, payrollEntryId);
    }
}
