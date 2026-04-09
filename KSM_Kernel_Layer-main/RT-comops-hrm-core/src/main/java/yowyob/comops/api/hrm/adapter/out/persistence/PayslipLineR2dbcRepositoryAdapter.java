package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.PayslipLineRepository;
import yowyob.comops.api.hrm.domain.model.PayslipLine;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class PayslipLineR2dbcRepositoryAdapter implements PayslipLineRepository {

    private final PayslipLineSpringDataRepository repository;

    public PayslipLineR2dbcRepositoryAdapter(PayslipLineSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<PayslipLine> findByPayrollEntryId(UUID tenantId, UUID payrollEntryId) {
        return repository.findAllByTenantIdAndPayrollEntryIdOrderBySortOrderAsc(tenantId, payrollEntryId)
                .map(this::toDomain);
    }

    @Override
    public Mono<PayslipLine> save(PayslipLine pl) {
        return repository.save(toEntity(pl)).map(this::toDomain);
    }

    @Override
    public Flux<PayslipLine> saveAll(Iterable<PayslipLine> lines) {
        var entities = new java.util.ArrayList<PayslipLineEntity>();
        lines.forEach(pl -> entities.add(toEntity(pl)));
        return repository.saveAll(entities).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByPayrollEntryId(UUID payrollEntryId) {
        return repository.deleteAllByPayrollEntryId(payrollEntryId);
    }

    private PayslipLineEntity toEntity(PayslipLine pl) {
        return new PayslipLineEntity(pl.id(), pl.tenantId(), pl.createdAt(), pl.updatedAt(),
                pl.payrollEntryId(), pl.code(), pl.label(), pl.lineType(), pl.base(), pl.rate(),
                pl.employeeAmount(), pl.employerAmount(), pl.sortOrder());
    }

    private PayslipLine toDomain(PayslipLineEntity e) {
        return PayslipLine.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.payrollEntryId(), e.code(), e.label(), e.lineType(), e.base(), e.rate(),
                e.employeeAmount(), e.employerAmount(), e.sortOrder());
    }
}
