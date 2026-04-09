package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID employeeId,
        String contractType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal baseSalary,
        String currency,
        String status) {

    public static ContractResponse from(Contract contract) {
        return new ContractResponse(contract.id(), contract.tenantId(), contract.organizationId(),
                contract.employeeId(), contract.contractType(), contract.startDate(), contract.endDate(),
                contract.baseSalary(), contract.currency(), contract.status());
    }
}
