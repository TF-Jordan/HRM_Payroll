package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.domain.exception.ContractNotFoundException;
import yowyob.comops.api.hrm.domain.exception.DuplicateRegistrationNumberException;
import yowyob.comops.api.hrm.domain.exception.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.exception.InsufficientLeaveBalanceException;
import yowyob.comops.api.hrm.domain.exception.InvalidContractStateException;
import yowyob.comops.api.hrm.domain.exception.InvalidEmployeeStateException;
import yowyob.comops.api.hrm.domain.exception.InvalidPayrollStateException;
import yowyob.comops.api.hrm.domain.exception.LeaveRequestNotFoundException;
import yowyob.comops.api.hrm.domain.exception.LoanAdvanceNotFoundException;
import yowyob.comops.api.hrm.domain.exception.PayrollRunNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {EmployeeController.class, ContractController.class,
        DependentController.class, LoanAdvanceController.class})
public class HrmExceptionHandler {

    @ExceptionHandler(DuplicateRegistrationNumberException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DuplicateRegistrationNumberException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "REGISTRATION_NUMBER_DUPLICATE"));
    }

    @ExceptionHandler({EmployeeNotFoundException.class, ContractNotFoundException.class,
            LeaveRequestNotFoundException.class, LoanAdvanceNotFoundException.class,
            PayrollRunNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException exception) {
        String errorCode = switch (exception) {
            case EmployeeNotFoundException ignored -> "EMPLOYEE_NOT_FOUND";
            case ContractNotFoundException ignored -> "CONTRACT_NOT_FOUND";
            case LeaveRequestNotFoundException ignored -> "LEAVE_REQUEST_NOT_FOUND";
            case LoanAdvanceNotFoundException ignored -> "LOAN_ADVANCE_NOT_FOUND";
            case PayrollRunNotFoundException ignored -> "PAYROLL_RUN_NOT_FOUND";
            default -> "HRM_NOT_FOUND";
        };
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler({InvalidEmployeeStateException.class, InvalidContractStateException.class,
            InvalidPayrollStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(RuntimeException exception) {
        String errorCode = switch (exception) {
            case InvalidEmployeeStateException ignored -> "EMPLOYEE_INVALID_STATE";
            case InvalidContractStateException ignored -> "CONTRACT_INVALID_STATE";
            case InvalidPayrollStateException ignored -> "PAYROLL_INVALID_STATE";
            default -> "HRM_INVALID_STATE";
        };
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(InsufficientLeaveBalanceException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "INSUFFICIENT_LEAVE_BALANCE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "HRM_INVALID_REQUEST"));
    }
}
