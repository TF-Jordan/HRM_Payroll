package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class InvalidContractStateException extends RuntimeException {

    public InvalidContractStateException(UUID contractId, String currentStatus, String expectedStatus) {
        super("Contract " + contractId + " is " + currentStatus + ", expected " + expectedStatus);
    }
}
