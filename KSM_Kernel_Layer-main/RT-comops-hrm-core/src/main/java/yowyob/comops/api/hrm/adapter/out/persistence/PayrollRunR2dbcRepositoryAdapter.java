package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PayrollRunRepository;
import yowyob.comops.api.hrm.domain.model.PayrollRun;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PayrollRunR2dbcRepositoryAdapter implements PayrollRunRepository {

    private final PayrollRunSpringDataRepository repository;

    public PayrollRunR2dbcRepositoryAdapter(PayrollRunSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayrollRun> findById(UUID payrollRunId) {
        return repository.findById(payrollRunId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollRun> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByPeriod(UUID tenantId, UUID organizationId, String period) {
        return repository.existsByTenantIdAndOrganizationIdAndPeriod(tenantId, organizationId, period);
    }

    @Override
    public Mono<PayrollRun> save(PayrollRun pr) {
        PayrollRunEntity entity = new PayrollRunEntity(pr.id(), pr.tenantId(), pr.createdAt(),
                pr.updatedAt(), pr.organizationId(), pr.agencyId(), pr.period(), pr.status(),
                pr.calculatedAt(), pr.validatedBy(), pr.validatedAt(), pr.paidAt(), pr.totalGross(),
                pr.totalNet(), pr.totalEmployerCharges(), pr.currency(), pr.employeeCount());
        return repository.save(entity).map(this::toDomain);
    }

    private PayrollRun toDomain(PayrollRunEntity e) {
        return PayrollRun.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.period(), e.status(), e.calculatedAt(),
                e.validatedBy(), e.validatedAt(), e.paidAt(), e.totalGross(), e.totalNet(),
                e.totalEmployerCharges(), e.currency(), e.employeeCount());
    }
}
