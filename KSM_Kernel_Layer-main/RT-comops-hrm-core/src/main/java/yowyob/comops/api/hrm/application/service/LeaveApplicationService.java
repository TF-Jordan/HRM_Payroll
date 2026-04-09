package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.ApproveLeaveRequestUseCase;
import yowyob.comops.api.hrm.application.port.in.CreateLeaveRequestCommand;
import yowyob.comops.api.hrm.application.port.in.CreateLeaveRequestUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveRequestRepository;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.exception.InsufficientLeaveBalanceException;
import yowyob.comops.api.hrm.domain.exception.LeaveRequestNotFoundException;
import yowyob.comops.api.hrm.domain.model.LeaveBalance;
import yowyob.comops.api.hrm.domain.model.LeaveRequest;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LeaveApplicationService implements CreateLeaveRequestUseCase, ApproveLeaveRequestUseCase {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public LeaveApplicationService(LeaveRequestRepository leaveRequestRepository,
                                   LeaveBalanceRepository leaveBalanceRepository,
                                   EmployeeRepository employeeRepository,
                                   ReactiveTransactionalExecutor transactionalExecutor) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    public Mono<LeaveRequest> getLeaveRequest(UUID leaveRequestId) {
        return leaveRequestRepository.findById(leaveRequestId)
                .switchIfEmpty(Mono.error(new LeaveRequestNotFoundException(leaveRequestId)));
    }

    public Flux<LeaveRequest> listByEmployee(UUID tenantId, UUID employeeId) {
        return leaveRequestRepository.findByEmployeeId(tenantId, employeeId);
    }

    public Flux<LeaveRequest> listPending(UUID tenantId, UUID organizationId) {
        return leaveRequestRepository.findByOrganizationIdAndStatus(tenantId, organizationId, "PENDING");
    }

    @Override
    public Mono<LeaveRequest> createLeaveRequest(CreateLeaveRequestCommand command) {
        Objects.requireNonNull(command, "command is required");
        int year = command.startDate().getYear();
        return transactionalExecutor.transactional(
                employeeRepository.findById(command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                                        command.tenantId(), command.employeeId(), command.leaveType(), year)
                                .defaultIfEmpty(LeaveBalance.create(command.tenantId(), command.organizationId(),
                                        command.employeeId(), command.leaveType(), year))
                                .flatMap(balance -> {
                                    if (balance.available().compareTo(command.daysRequested()) < 0) {
                                        return Mono.error(new InsufficientLeaveBalanceException(
                                                command.employeeId(), command.leaveType(),
                                                command.daysRequested(), balance.available()));
                                    }
                                    LeaveRequest request = LeaveRequest.create(command.tenantId(),
                                            command.organizationId(), command.employeeId(), command.leaveType(),
                                            command.startDate(), command.endDate(), command.daysRequested(),
                                            command.reason());
                                    return leaveRequestRepository.save(request);
                                })));
    }

    @Override
    public Mono<LeaveRequest> approveLeaveRequest(UUID leaveRequestId, UUID approvedBy) {
        return transactionalExecutor.transactional(
                leaveRequestRepository.findById(leaveRequestId)
                        .switchIfEmpty(Mono.error(new LeaveRequestNotFoundException(leaveRequestId)))
                        .flatMap(request -> {
                            LeaveRequest approved = request.approve(approvedBy);
                            int year = approved.startDate().getYear();
                            return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                                            approved.tenantId(), approved.employeeId(), approved.leaveType(), year)
                                    .defaultIfEmpty(LeaveBalance.create(approved.tenantId(), approved.organizationId(),
                                            approved.employeeId(), approved.leaveType(), year))
                                    .flatMap(balance -> {
                                        LeaveBalance updated = balance.take(approved.daysRequested());
                                        return leaveBalanceRepository.save(updated);
                                    })
                                    .then(leaveRequestRepository.save(approved));
                        }));
    }

    @Override
    public Mono<LeaveRequest> rejectLeaveRequest(UUID leaveRequestId, UUID rejectedBy, String reason) {
        return transactionalExecutor.transactional(
                leaveRequestRepository.findById(leaveRequestId)
                        .switchIfEmpty(Mono.error(new LeaveRequestNotFoundException(leaveRequestId)))
                        .map(request -> request.reject(rejectedBy, reason))
                        .flatMap(leaveRequestRepository::save));
    }
}
