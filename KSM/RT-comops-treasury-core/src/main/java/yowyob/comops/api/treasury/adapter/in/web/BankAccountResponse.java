package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.UUID;

public record BankAccountResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID bankThirdPartyId,
        String bankName,
        String accountNumber,
        String iban,
        String currency,
        String status) {

    public static BankAccountResponse from(BankAccount bankAccount) {
        return new BankAccountResponse(bankAccount.id(), bankAccount.tenantId(), bankAccount.organizationId(),
                bankAccount.bankThirdPartyId(), bankAccount.bankName(), bankAccount.accountNumber(), bankAccount.iban(),
                bankAccount.currency(), bankAccount.status());
    }
}
