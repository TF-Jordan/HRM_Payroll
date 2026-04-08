package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(UUID contractId) {
        super("Contract not found: " + contractId);
    }
}
