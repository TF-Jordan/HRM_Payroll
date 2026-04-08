package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class InvalidEmployeeStateException extends RuntimeException {

    public InvalidEmployeeStateException(UUID employeeId, String currentStatus, String expectedStatus) {
        super("Employee " + employeeId + " is " + currentStatus + ", expected " + expectedStatus);
    }
}
