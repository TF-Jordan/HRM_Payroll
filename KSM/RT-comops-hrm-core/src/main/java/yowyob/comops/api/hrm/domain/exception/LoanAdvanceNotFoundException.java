package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class LoanAdvanceNotFoundException extends RuntimeException {

    public LoanAdvanceNotFoundException(UUID loanId) {
        super("Loan/advance not found: " + loanId);
    }
}
