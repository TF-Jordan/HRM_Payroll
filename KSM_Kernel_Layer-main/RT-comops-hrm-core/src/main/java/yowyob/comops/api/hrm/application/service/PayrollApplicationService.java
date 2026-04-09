package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.CreatePayrollRunCommand;
import yowyob.comops.api.hrm.application.port.in.CreatePayrollRunUseCase;
import yowyob.comops.api.hrm.application.port.in.GetPayrollRunUseCase;
import yowyob.comops.api.hrm.application.port.in.ValidatePayrollRunUseCase;
import yowyob.comops.api.hrm.application.port.out.PayrollRunRepository;
import yowyob.comops.api.hrm.domain.exception.PayrollRunNotFoundException;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PayrollApplicationService implements CreatePayrollRunUseCase, GetPayrollRunUseCase,
        ValidatePayrollRunUseCase {

    private final PayrollRunRepository payrollRunRepository;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public PayrollApplicationService(PayrollRunRepository payrollRunRepository,
                                     BusinessEventPublisher businessEventPublisher,
                                     ReactiveTransactionalExecutor transactionalExecutor) {
        this.payrollRunRepository = payrollRunRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<PayrollRun> createPayrollRun(CreatePayrollRunCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(
                payrollRunRepository.existsByPeriod(command.tenantId(), command.organizationId(), command.period())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new IllegalArgumentException(
                                        "Payroll run already exists for period: " + command.period()));
                            }
                            PayrollRun payrollRun = PayrollRun.create(command.tenantId(),
                                    command.organizationId(), command.agencyId(), command.period(),
                                    command.currency());
                            return payrollRunRepository.save(payrollRun);
                        }));
    }

    @Override
    public Mono<PayrollRun> getPayrollRun(UUID payrollRunId) {
        return payrollRunRepository.findById(payrollRunId)
                .switchIfEmpty(Mono.error(new PayrollRunNotFoundException(payrollRunId)));
    }

    @Override
    public Flux<PayrollRun> listPayrollRuns(UUID tenantId, UUID organizationId) {
        return payrollRunRepository.findByOrganizationId(tenantId, organizationId);
    }

    @Override
    public Mono<PayrollRun> validatePayrollRun(UUID payrollRunId, UUID validatedBy) {
        return transactionalExecutor.transactional(
                payrollRunRepository.findById(payrollRunId)
                        .switchIfEmpty(Mono.error(new PayrollRunNotFoundException(payrollRunId)))
                        .map(run -> run.validate(validatedBy))
                        .flatMap(payrollRunRepository::save)
                        .flatMap(validated -> businessEventPublisher.publish(payrollValidatedEvent(validated))
                                .thenReturn(validated)));
    }

    private BusinessEvent payrollValidatedEvent(PayrollRun payrollRun) {
        return BusinessEvent.now(payrollRun.tenantId(), payrollRun.organizationId(),
                "PAYROLL_VALIDATED", "PAYROLL_RUN", payrollRun.id(), payload(
                        "period", payrollRun.period(),
                        "totalGross", payrollRun.totalGross(),
                        "totalNet", payrollRun.totalNet(),
                        "totalEmployerCharges", payrollRun.totalEmployerCharges(),
                        "currency", payrollRun.currency(),
                        "employeeCount", payrollRun.employeeCount()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            if (entries[index + 1] != null) {
                payload.put(entries[index].toString(), entries[index + 1]);
            }
        }
        return payload;
    }
}
