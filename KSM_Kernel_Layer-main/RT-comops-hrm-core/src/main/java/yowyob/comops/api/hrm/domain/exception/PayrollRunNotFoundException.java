package yowyob.comops.api.hrm.domain.exception;

import java.util.UUID;

public class PayrollRunNotFoundException extends RuntimeException {

    public PayrollRunNotFoundException(UUID payrollRunId) {
        super("Payroll run not found: " + payrollRunId);
    }
}
