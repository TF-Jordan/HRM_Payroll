package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.LeaveRequestRepository;
import yowyob.comops.api.hrm.domain.model.LeaveRequest;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class LeaveRequestR2dbcRepositoryAdapter implements LeaveRequestRepository {

    private final LeaveRequestSpringDataRepository repository;

    public LeaveRequestR2dbcRepositoryAdapter(LeaveRequestSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<LeaveRequest> findById(UUID leaveRequestId) {
        return repository.findById(leaveRequestId).map(this::toDomain);
    }

    @Override
    public Flux<LeaveRequest> findByEmployeeId(UUID tenantId, UUID employeeId) {
        return repository.findAllByTenantIdAndEmployeeId(tenantId, employeeId).map(this::toDomain);
    }

    @Override
    public Flux<LeaveRequest> findByOrganizationIdAndStatus(UUID tenantId, UUID organizationId, String status) {
        return repository.findAllByTenantIdAndOrganizationIdAndStatus(tenantId, organizationId, status)
                .map(this::toDomain);
    }

    @Override
    public Mono<LeaveRequest> save(LeaveRequest lr) {
        LeaveRequestEntity entity = new LeaveRequestEntity(lr.id(), lr.tenantId(), lr.createdAt(),
                lr.updatedAt(), lr.organizationId(), lr.employeeId(), lr.leaveType(), lr.startDate(),
                lr.endDate(), lr.daysRequested(), lr.reason(), lr.status(), lr.approvedBy(),
                lr.approvedAt(), lr.rejectionReason());
        return repository.save(entity).map(this::toDomain);
    }

    private LeaveRequest toDomain(LeaveRequestEntity e) {
        return LeaveRequest.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.employeeId(), e.leaveType(), e.startDate(), e.endDate(),
                e.daysRequested(), e.reason(), e.status(), e.approvedBy(), e.approvedAt(),
                e.rejectionReason());
    }
}
