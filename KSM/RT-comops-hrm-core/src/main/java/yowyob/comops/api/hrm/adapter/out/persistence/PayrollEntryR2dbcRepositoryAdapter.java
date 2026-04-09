package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.hrm.domain.model.PayrollEntry;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PayrollEntryR2dbcRepositoryAdapter implements PayrollEntryRepository {

    private final PayrollEntrySpringDataRepository repository;

    public PayrollEntryR2dbcRepositoryAdapter(PayrollEntrySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayrollEntry> findById(UUID payrollEntryId) {
        return repository.findById(payrollEntryId).map(this::toDomain);
    }

    @Override
    public Flux<PayrollEntry> findByPayrollRunId(UUID tenantId, UUID payrollRunId) {
        return repository.findAllByTenantIdAndPayrollRunId(tenantId, payrollRunId).map(this::toDomain);
    }

    @Override
    public Mono<PayrollEntry> save(PayrollEntry pe) {
        PayrollEntryEntity entity = toEntity(pe);
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<PayrollEntry> saveAll(Iterable<PayrollEntry> entries) {
        var entities = new java.util.ArrayList<PayrollEntryEntity>();
        entries.forEach(pe -> entities.add(toEntity(pe)));
        return repository.saveAll(entities).map(this::toDomain);
    }

    private PayrollEntryEntity toEntity(PayrollEntry pe) {
        return new PayrollEntryEntity(pe.id(), pe.tenantId(), pe.createdAt(), pe.updatedAt(),
                pe.payrollRunId(), pe.employeeId(), pe.contractId(), pe.baseSalary(), pe.grossSalary(),
                pe.netSalary(), pe.totalDeductions(), pe.totalEmployerCharges(), pe.currency());
    }

    private PayrollEntry toDomain(PayrollEntryEntity e) {
        return PayrollEntry.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.payrollRunId(), e.employeeId(), e.contractId(), e.baseSalary(), e.grossSalary(),
                e.netSalary(), e.totalDeductions(), e.totalEmployerCharges(), e.currency());
    }
}
