package yowyob.comops.api.hrm.domain.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientLeaveBalanceException extends RuntimeException {

    public InsufficientLeaveBalanceException(UUID employeeId, String leaveType, BigDecimal requested, BigDecimal available) {
        super("Employee " + employeeId + " has insufficient " + leaveType + " balance: requested "
                + requested + ", available " + available);
    }
}
