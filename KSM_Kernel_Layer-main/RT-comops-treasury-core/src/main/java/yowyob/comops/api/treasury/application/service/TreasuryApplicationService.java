package yowyob.comops.api.treasury.application.service;

import yowyob.comops.api.treasury.application.port.in.GetBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankAccountsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.out.BankAccountRepository;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.DuplicateBankAccountException;
import yowyob.comops.api.treasury.domain.model.BankAccount;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TreasuryApplicationService implements RegisterBankAccountUseCase, GetBankAccountUseCase, ListBankAccountsUseCase {

    private final BankAccountRepository bankAccountRepository;

    public TreasuryApplicationService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public Mono<BankAccount> register(RegisterBankAccountCommand command) {
        Objects.requireNonNull(command, "command is required");
        BankAccount bankAccount = BankAccount.register(command.tenantId(), command.organizationId(),
                command.bankThirdPartyId(), command.bankName(), command.accountNumber(), command.iban(), command.currency());
        return bankAccountRepository.existsByAccountNumber(bankAccount.tenantId(), bankAccount.organizationId(), bankAccount.accountNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateBankAccountException(bankAccount.accountNumber()))
                        : bankAccountRepository.save(bankAccount));
    }

    @Override
    public Mono<BankAccount> getBankAccount(UUID bankAccountId) {
        return bankAccountRepository.findById(bankAccountId)
                .switchIfEmpty(Mono.error(new BankAccountNotFoundException(bankAccountId)));
    }

    @Override
    public Flux<BankAccount> listBankAccounts(UUID tenantId, UUID organizationId) {
        return bankAccountRepository.findByOrganizationId(tenantId, organizationId);
    }
}
