package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class InvalidPayrollStateException extends RuntimeException {

    public InvalidPayrollStateException(UUID payrollRunId, String currentStatus, String expectedStatus) {
        super("Payroll run " + payrollRunId + " is " + currentStatus + ", expected " + expectedStatus);
    }
}
