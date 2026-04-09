package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LeaveBalanceR2dbcRepositoryAdapter implements LeaveBalanceRepository {

    private final LeaveBalanceSpringDataRepository repository;

    public LeaveBalanceR2dbcRepositoryAdapter(LeaveBalanceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LeaveBalance> findById(UUID leaveBalanceId) {
        return repository.findById(leaveBalanceId).map(this::toDomain);
    }

    @Override
    public Flux<LeaveBalance> findByEmployeeIdAndYear(UUID tenantId, UUID employeeId, int year) {
        return repository.findAllByTenantIdAndEmployeeIdAndYear(tenantId, employeeId, year).map(this::toDomain);
    }

    @Override
    public Mono<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(UUID tenantId, UUID employeeId,
            String leaveType, int year) {
        return repository.findByTenantIdAndEmployeeIdAndLeaveTypeAndYear(tenantId, employeeId, leaveType, year)
                .map(this::toDomain);
    }

    @Override
    public Flux<LeaveBalance> findByOrganizationIdAndYear(UUID tenantId, UUID organizationId, int year) {
        return repository.findAllByTenantIdAndOrganizationIdAndYear(tenantId, organizationId, year).map(this::toDomain);
    }

    @Override
    public Mono<LeaveBalance> save(LeaveBalance leaveBalance) {
        LeaveBalanceEntity entity = new LeaveBalanceEntity(leaveBalance.id(), leaveBalance.tenantId(),
                leaveBalance.createdAt(), leaveBalance.updatedAt(), leaveBalance.organizationId(),
                leaveBalance.employeeId(), leaveBalance.leaveType(), leaveBalance.year(),
                leaveBalance.accrued(), leaveBalance.taken(), leaveBalance.adjustment());
        return repository.save(entity).map(this::toDomain);
    }

    private LeaveBalance toDomain(LeaveBalanceEntity entity) {
        return LeaveBalance.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.employeeId(), entity.leaveType(), entity.year(),
                entity.accrued(), entity.taken(), entity.adjustment());
    }
}
