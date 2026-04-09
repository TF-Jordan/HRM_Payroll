package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LoanAdvanceR2dbcRepositoryAdapter implements LoanAdvanceRepository {

    private final LoanAdvanceSpringDataRepository repository;

    public LoanAdvanceR2dbcRepositoryAdapter(LoanAdvanceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LoanAdvance> findById(UUID loanId) {
        return repository.findById(loanId).map(this::toDomain);
    }

    @Override
    public Flux<LoanAdvance> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<LoanAdvance> findActiveByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeIdAndStatus(tenantId, employeeId, "ACTIVE").map(this::toDomain);
    }

    @Override
    public Flux<LoanAdvance> findByOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status) {
        return repository.findAllByTenantIdAndOrganizationIdAndStatus(tenantId, organizationId, status)
                .map(this::toDomain);
    }

    @Override
    public Mono<LoanAdvance> save(LoanAdvance loan) {
        LoanAdvanceEntity entity = new LoanAdvanceEntity(loan.id(), loan.tenantId(), loan.createdAt(),
                loan.updatedAt(), loan.organizationId(), loan.employeeId(), loan.loanType(), loan.amount(),
                loan.currency(), loan.monthlyDeduction(), loan.totalRepaid(), loan.remainingBalance(),
                loan.installmentsCount(), loan.installmentsPaid(), loan.status(), loan.approvedBy(),
                loan.approvedAt());
        return repository.save(entity).map(this::toDomain);
    }

    private LoanAdvance toDomain(LoanAdvanceEntity entity) {
        return LoanAdvance.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.employeeId(), entity.loanType(), entity.amount(),
                entity.currency(), entity.monthlyDeduction(), entity.totalRepaid(), entity.remainingBalance(),
                entity.installmentsCount(), entity.installmentsPaid(), entity.status(), entity.approvedBy(),
                entity.approvedAt());
    }
}
