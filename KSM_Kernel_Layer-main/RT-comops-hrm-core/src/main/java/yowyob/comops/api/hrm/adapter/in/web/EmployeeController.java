package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.GetEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.ListEmployeesUseCase;
import yowyob.comops.api.hrm.application.port.in.TerminateEmployeeUseCase;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeCommand;
import yowyob.comops.api.hrm.application.port.in.UpdateEmployeeUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/hrm/employees")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:read')")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final UpdateEmployeeUseCase updateEmployeeUseCase;
    private final TerminateEmployeeUseCase terminateEmployeeUseCase;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase,
                              GetEmployeeUseCase getEmployeeUseCase,
                              ListEmployeesUseCase listEmployeesUseCase,
                              UpdateEmployeeUseCase updateEmployeeUseCase,
                              TerminateEmployeeUseCase terminateEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.updateEmployeeUseCase = updateEmployeeUseCase;
        this.terminateEmployeeUseCase = terminateEmployeeUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> createEmployee(
            @Valid @RequestBody Mono<CreateEmployeeRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return createEmployeeUseCase.createEmployee(new CreateEmployeeCommand(
                            context.tenantId(), context.organizationId(), request.agencyId(),
                            request.actorId(), request.registrationNumber(), request.firstName(),
                            request.lastName(), request.email(), request.phoneNumber(), request.gender(),
                            request.birthDate(), request.hireDate(), request.department(), request.jobTitle(),
                            request.cnpsNumber()));
                })
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Employee created.")));
    }

    @GetMapping("/{employeeId}")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> getEmployee(
            @PathVariable("employeeId") UUID employeeId) {
        return getEmployeeUseCase.getEmployee(employeeId)
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<EmployeeResponse>>>> listEmployees(
            @RequestParam(value = "agencyId", required = false) UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> listEmployeesUseCase.listEmployees(
                        context.tenantId(), context.organizationId(), agencyId))
                .map(EmployeeResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employees fetched.")));
    }

    @PatchMapping("/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> updateEmployee(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody Mono<UpdateEmployeeRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    var request = tuple.getT1();
                    var context = tuple.getT2();
                    return updateEmployeeUseCase.updateEmployee(new UpdateEmployeeCommand(
                            context.tenantId(), employeeId, request.firstName(), request.lastName(),
                            request.email(), request.phoneNumber(), request.gender(), request.birthDate(),
                            request.department(), request.jobTitle(), request.agencyId(), request.cnpsNumber()));
                })
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee updated.")));
    }

    @PostMapping("/{employeeId}/terminate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:write')")
    public Mono<ResponseEntity<ApiResponse<EmployeeResponse>>> terminateEmployee(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody Mono<TerminateEmployeeRequest> requestMono) {
        return requestMono
                .flatMap(request -> terminateEmployeeUseCase.terminateEmployee(employeeId, request.terminationDate()))
                .map(EmployeeResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employee terminated.")));
    }
}
