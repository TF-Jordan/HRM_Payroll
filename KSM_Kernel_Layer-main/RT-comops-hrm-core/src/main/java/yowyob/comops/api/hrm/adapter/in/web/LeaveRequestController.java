package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateLeaveRequestCommand;
import yowyob.comops.api.hrm.application.service.LeaveApplicationService;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/hrm/leave-requests")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class LeaveRequestController {

    private final LeaveApplicationService leaveApplicationService;

    public LeaveRequestController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<LeaveRequestResponse>>> createLeaveRequest(
            @Valid @RequestBody Mono<CreateLeaveRequestRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return leaveApplicationService.createLeaveRequest(new CreateLeaveRequestCommand(
                            context.tenantId(), context.organizationId(), request.employeeId(),
                            request.leaveType(), request.startDate(), request.endDate(),
                            request.daysRequested(), request.reason()));
                })
                .map(LeaveRequestResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Leave request created.")));
    }

    @GetMapping("/{leaveRequestId}")
    public Mono<ResponseEntity<ApiResponse<LeaveRequestResponse>>> getLeaveRequest(
            @PathVariable("leaveRequestId") UUID leaveRequestId) {
        return leaveApplicationService.getLeaveRequest(leaveRequestId)
                .map(LeaveRequestResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    public Mono<ResponseEntity<ApiResponse<List<LeaveRequestResponse>>>> listByEmployee(
            @PathVariable("employeeId") UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> leaveApplicationService.listByEmployee(context.tenantId(), employeeId))
                .map(LeaveRequestResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave requests fetched.")));
    }

    @GetMapping("/pending")
    public Mono<ResponseEntity<ApiResponse<List<LeaveRequestResponse>>>> listPending() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> leaveApplicationService.listPending(
                        context.tenantId(), context.organizationId()))
                .map(LeaveRequestResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Pending leave requests fetched.")));
    }

    @PostMapping("/{leaveRequestId}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<LeaveRequestResponse>>> approveLeaveRequest(
            @PathVariable("leaveRequestId") UUID leaveRequestId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> leaveApplicationService.approveLeaveRequest(leaveRequestId,
                        context.userId()))
                .map(LeaveRequestResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request approved.")));
    }

    @PostMapping("/{leaveRequestId}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<LeaveRequestResponse>>> rejectLeaveRequest(
            @PathVariable("leaveRequestId") UUID leaveRequestId,
            @Valid @RequestBody Mono<RejectLeaveRequestRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return leaveApplicationService.rejectLeaveRequest(leaveRequestId,
                            context.userId(), request.reason());
                })
                .map(LeaveRequestResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Leave request rejected.")));
    }
}
