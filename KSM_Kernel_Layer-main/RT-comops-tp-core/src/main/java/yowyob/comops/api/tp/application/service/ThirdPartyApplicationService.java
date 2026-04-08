package yowyob.comops.api.tp.application.service;

import yowyob.comops.api.common.domain.model.PartyRef;
import yowyob.comops.api.kernel.application.port.in.RecordSystemAuditUseCase;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.tp.application.port.in.AddThirdPartyBankAccountCommand;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.DeleteThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyStatisticsUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.LookupThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyBankAccountsUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyLifecycleUseCase;
import yowyob.comops.api.tp.application.port.in.SearchThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyCommand;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.out.ThirdPartyBankAccountRepository;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.application.port.out.ThirdPartySearchGateway;
import yowyob.comops.api.tp.domain.DuplicateThirdPartyReferenceException;
import yowyob.comops.api.tp.domain.ProspectConversionNotAllowedException;
import yowyob.comops.api.tp.domain.ThirdPartyAccountingAccountAlreadyExistsException;
import yowyob.comops.api.tp.domain.ThirdPartyBankAccountAlreadyExistsException;
import yowyob.comops.api.tp.domain.ThirdPartyBankAccountNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartyLookupNotFoundException;
import yowyob.comops.api.tp.domain.ThirdPartyNotFoundException;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import yowyob.comops.api.tp.domain.model.ThirdPartySearchResult;
import yowyob.comops.api.tp.domain.model.ThirdPartyStatistics;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ThirdPartyApplicationService implements CreateThirdPartyUseCase, GetThirdPartyUseCase,
                ListThirdPartiesUseCase, SearchThirdPartiesUseCase, UpdateThirdPartyUseCase, DeleteThirdPartyUseCase,
                ManageThirdPartyBankAccountsUseCase, LookupThirdPartyUseCase, ManageThirdPartyLifecycleUseCase,
                GetThirdPartyStatisticsUseCase {

        private static final Logger log = LoggerFactory.getLogger(ThirdPartyApplicationService.class);

        private static final Set<String> AUTO_MANAGED_SEGMENTS = Set.of("NEW", "QUALIFIED", "HOT");

        private final ThirdPartyRepository thirdPartyRepository;
        private final ThirdPartyBankAccountRepository thirdPartyBankAccountRepository;
        private final Optional<ThirdPartySearchGateway> thirdPartySearchGateway;
        private final BusinessEventPublisher businessEventPublisher;
        private final ReactiveTransactionalExecutor transactionalExecutor;
        private final RecordSystemAuditUseCase recordSystemAuditUseCase;

        public ThirdPartyApplicationService(ThirdPartyRepository thirdPartyRepository,
                        ThirdPartyBankAccountRepository thirdPartyBankAccountRepository,
                        BusinessEventPublisher businessEventPublisher,
                        ReactiveTransactionalExecutor transactionalExecutor,
                        Optional<ThirdPartySearchGateway> thirdPartySearchGateway,
                        RecordSystemAuditUseCase recordSystemAuditUseCase) {
                this.thirdPartyRepository = thirdPartyRepository;
                this.thirdPartyBankAccountRepository = thirdPartyBankAccountRepository;
                this.businessEventPublisher = businessEventPublisher;
                this.transactionalExecutor = transactionalExecutor;
                this.thirdPartySearchGateway = thirdPartySearchGateway;
                this.recordSystemAuditUseCase = recordSystemAuditUseCase;
        }

        @Override
        public Mono<ThirdParty> createThirdParty(CreateThirdPartyCommand command) {
                Objects.requireNonNull(command, "command is required");
                ThirdParty thirdParty = ThirdParty.create(
                                command.tenantId(),
                                command.organizationId(),
                                new PartyRef(command.partyType(), command.partyId()),
                                command.code(),
                                command.name(),
                                command.roles(),
                                command.prospect(),
                                command.accountingAccount(),
                                command.segment(),
                                command.qualificationScore(),
                                command.enabled(),
                                command.type(),
                                command.legalForm(),
                                command.uniqueIdentificationNumber(),
                                command.tradeRegistrationNumber(),
                                command.name(),
                                command.acronym(),
                                command.longName(),
                                command.logoUri(),
                                command.logoId(),
                                command.accountingAccountNumbers(),
                                command.authorizedPaymentMethods(),
                                command.authorizedCreditLimit(),
                                command.maxDiscountRate(),
                                command.vatSubject() == null ? false : command.vatSubject(),
                                command.operationsBalance(),
                                command.openingBalance(),
                                command.payTermNumber(),
                                command.payTermType(),
                                command.thirdPartyFamily(),
                                command.classification(),
                                command.taxNumber());

                Mono<ThirdParty> operation = assertReferenceAvailable(thirdParty.tenantId(),
                                thirdParty.organizationId(),
                                thirdParty.referenceCode())
                                .then(assertAccountingAccountAvailable(thirdParty.tenantId(),
                                                thirdParty.organizationId(),
                                                thirdParty.accountingAccount(), null))
                                .then(Mono.fromSupplier(() -> prepareNewThirdPartyForInsert(thirdParty, false)))
                                .flatMap(thirdPartyRepository::save)
                                .flatMap(saved -> ReactiveRequestContextHolder.getRequiredContext()
                                                .flatMap(context -> businessEventPublisher
                                                                .publish(thirdPartyEvent("THIRD_PARTY_CREATED", saved))
                                                                .then(recordSystemAuditUseCase.record(saved.tenantId(),
                                                                                saved.organizationId(),
                                                                                context.userId(), "THIRD_PARTY_CREATED",
                                                                                "THIRD_PARTY", saved.id().toString(),
                                                                                saved.referenceCode()))
                                                                .thenReturn(saved)));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> getThirdParty(UUID thirdPartyId) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId))
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(thirdPartyId)));
        }

        @Override
        public Flux<ThirdParty> listThirdParties(UUID organizationId, String role, Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMapMany(context -> thirdPartyRepository.findByOrganizationId(context.tenantId(),
                                                organizationId))
                                .filter(thirdParty -> role == null || role.isBlank() || thirdParty.hasRole(role))
                                .filter(thirdParty -> prospect == null || thirdParty.prospect() == prospect);
        }

        @Override
        public Flux<ThirdPartySearchResult> searchThirdParties(UUID organizationId, String query, String role,
                        Boolean prospect, String segment, Integer minimumQualificationScore, Boolean active,
                        String followUpStatus, int page, int size) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMapMany(context -> thirdPartySearchGateway
                                                .map(gateway -> gateway
                                                                .search(context.tenantId(), organizationId, query, role,
                                                                                prospect,
                                                                                segment, minimumQualificationScore,
                                                                                active, followUpStatus, page, size)
                                                                .onErrorResume(e -> {
                                                                        log.warn("Elasticsearch query failed, falling back to repository for organization {}",
                                                                                        organizationId, e);
                                                                        return Mono.empty();
                                                                })
                                                                .switchIfEmpty(Flux.defer(() -> {
                                                                        log.warn("Falling back to repository search: ES returned 0 results or failed for organization {}",
                                                                                        organizationId);
                                                                        return searchFromRepository(context.tenantId(),
                                                                                        organizationId, query, role,
                                                                                        prospect, segment,
                                                                                        minimumQualificationScore,
                                                                                        active, followUpStatus, page,
                                                                                        size);
                                                                })))
                                                .orElseGet(() -> searchFromRepository(context.tenantId(),
                                                                organizationId, query, role,
                                                                prospect, segment, minimumQualificationScore, active,
                                                                followUpStatus, page, size)));
        }

        @Override
        public Mono<ThirdParty> updateThirdParty(UpdateThirdPartyCommand command) {
                Objects.requireNonNull(command, "command is required");
                Mono<ThirdParty> operation = thirdPartyRepository.findById(command.tenantId(), command.thirdPartyId())
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(command.thirdPartyId())))
                                .flatMap(existing -> {
                                        ThirdParty updated = existing.update(command.referenceCode(),
                                                        command.displayName(), command.roles(),
                                                        command.prospect(), command.accountingAccount(),
                                                        command.segment(),
                                                        command.qualificationScore(), command.enabled(),
                                                        command.type(),
                                                        command.legalForm(),
                                                        command.uniqueIdentificationNumber(),
                                                        command.tradeRegistrationNumber(),
                                                        command.name(),
                                                        command.acronym(),
                                                        command.longName(),
                                                        command.logoUri(),
                                                        command.logoId(),
                                                        command.accountingAccountNumbers(),
                                                        command.authorizedPaymentMethods(),
                                                        command.authorizedCreditLimit(),
                                                        command.maxDiscountRate(),
                                                        command.vatSubject() == null ? false : command.vatSubject(),
                                                        command.operationsBalance(),
                                                        command.openingBalance(),
                                                        command.payTermNumber(),
                                                        command.payTermType(),
                                                        command.thirdPartyFamily(),
                                                        command.classification(),
                                                        command.taxNumber());
                                        Mono<Void> duplicateReferenceCheck = existing.referenceCode()
                                                        .equalsIgnoreCase(updated.referenceCode())
                                                                        ? Mono.empty()
                                                                        : assertReferenceAvailable(updated.tenantId(),
                                                                                        updated.organizationId(),
                                                                                        updated.referenceCode());
                                        return duplicateReferenceCheck
                                                        .then(assertAccountingAccountAvailable(updated.tenantId(),
                                                                        updated.organizationId(),
                                                                        updated.accountingAccount(), updated.id()))
                                                        .then(thirdPartyBankAccountRepository
                                                                        .findByThirdPartyId(updated.tenantId(),
                                                                                        updated.id())
                                                                        .hasElements()
                                                                        .map(hasBankAccount -> applyQualificationPolicy(
                                                                                        updated, hasBankAccount,
                                                                                        false)))
                                                        .flatMap(thirdPartyRepository::save)
                                                        .flatMap(saved -> ReactiveRequestContextHolder
                                                                        .getRequiredContext()
                                                                        .flatMap(context -> businessEventPublisher
                                                                                        .publish(
                                                                                                        thirdPartyEvent("THIRD_PARTY_UPDATED",
                                                                                                                        saved))
                                                                                        .then(recordSystemAuditUseCase
                                                                                                        .record(saved.tenantId(),
                                                                                                                        saved.organizationId(),
                                                                                                                        context.userId(),
                                                                                                                        "THIRD_PARTY_UPDATED",
                                                                                                                        "THIRD_PARTY",
                                                                                                                        saved.id().toString(),
                                                                                                                        saved.referenceCode()))
                                                                                        .thenReturn(saved)));
                                });
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<Void> deleteThirdParty(UUID thirdPartyId) {
                Mono<Void> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .flatMap(bankAccount -> thirdPartyBankAccountRepository
                                                                                .deleteById(context.tenantId(),
                                                                                                bankAccount.id()))
                                                                .then(thirdPartyRepository.deleteById(
                                                                                context.tenantId(), existing.id()))
                                                                .then(businessEventPublisher.publish(thirdPartyEvent(
                                                                                "THIRD_PARTY_DELETED", existing)))
                                                                .then(recordSystemAuditUseCase.record(
                                                                                existing.tenantId(),
                                                                                existing.organizationId(),
                                                                                context.userId(), "THIRD_PARTY_DELETED",
                                                                                "THIRD_PARTY",
                                                                                existing.id().toString(),
                                                                                existing.referenceCode()))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Flux<ThirdPartyBankAccount> listBankAccounts(UUID thirdPartyId) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMapMany(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .thenMany(thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(), thirdPartyId)));
        }

        @Override
        public Mono<ThirdPartyBankAccount> addBankAccount(AddThirdPartyBankAccountCommand command) {
                Mono<ThirdPartyBankAccount> operation = thirdPartyRepository
                                .findById(command.tenantId(), command.thirdPartyId())
                                .switchIfEmpty(Mono.error(new ThirdPartyNotFoundException(command.thirdPartyId())))
                                .flatMap(thirdParty -> thirdPartyBankAccountRepository.existsByIban(command.tenantId(),
                                                command.thirdPartyId(), command.iban())
                                                .flatMap(exists -> exists
                                                                ? Mono.error(new ThirdPartyBankAccountAlreadyExistsException(
                                                                                command.iban()))
                                                                : thirdPartyBankAccountRepository
                                                                                .findByThirdPartyId(command.tenantId(),
                                                                                                command.thirdPartyId())
                                                                                .flatMap(account -> thirdPartyBankAccountRepository
                                                                                                .save(command.primary()
                                                                                                                ? account.clearPrimary()
                                                                                                                : account))
                                                                                .then(thirdPartyBankAccountRepository
                                                                                                .save(ThirdPartyBankAccount
                                                                                                                .create(
                                                                                                                                command.tenantId(),
                                                                                                                                command.thirdPartyId(),
                                                                                                                                command.label(),
                                                                                                                                command.bankName(),
                                                                                                                                command.iban(),
                                                                                                                                command.swiftBic(),
                                                                                                                                command.currency(),
                                                                                                                                command.primary())))))
                                .flatMap(saved -> ReactiveRequestContextHolder.getRequiredContext()
                                                .flatMap(context -> businessEventPublisher.publish(BusinessEvent.now(
                                                                saved.tenantId(),
                                                                context.organizationId(),
                                                                "THIRD_PARTY_BANK_ACCOUNT_ADDED", "THIRD_PARTY",
                                                                saved.thirdPartyId(), payload(
                                                                                "iban", saved.iban(),
                                                                                "bankName", saved.bankName(),
                                                                                "currency", saved.currency(),
                                                                                "primary", saved.primary())))
                                                                .then(recordSystemAuditUseCase.record(saved.tenantId(),
                                                                                context.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_BANK_ACCOUNT_ADDED",
                                                                                "THIRD_PARTY",
                                                                                saved.thirdPartyId().toString(),
                                                                                saved.iban()))
                                                                .thenReturn(saved)));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<Void> removeBankAccount(UUID thirdPartyId, UUID bankAccountId) {
                Mono<Void> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .then(thirdPartyBankAccountRepository.findById(context.tenantId(),
                                                                bankAccountId))
                                                .filter(account -> account.thirdPartyId().equals(thirdPartyId))
                                                .switchIfEmpty(Mono.error(new ThirdPartyBankAccountNotFoundException(
                                                                bankAccountId)))
                                                .flatMap(account -> thirdPartyBankAccountRepository
                                                                .deleteById(context.tenantId(), account.id())
                                                                .then(recordSystemAuditUseCase.record(
                                                                                context.tenantId(),
                                                                                context.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_BANK_ACCOUNT_DELETED",
                                                                                "THIRD_PARTY",
                                                                                thirdPartyId.toString(),
                                                                                account.iban()))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<Void> setPrimaryBankAccount(UUID thirdPartyId, UUID bankAccountId) {
                Mono<Void> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .then(thirdPartyBankAccountRepository.findById(context.tenantId(),
                                                                bankAccountId))
                                                .filter(account -> account.thirdPartyId().equals(thirdPartyId))
                                                .switchIfEmpty(Mono.error(new ThirdPartyBankAccountNotFoundException(
                                                                bankAccountId)))
                                                .flatMap(target -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .flatMap(account -> thirdPartyBankAccountRepository
                                                                                .save(account.id().equals(target.id())
                                                                                                ? account.markPrimary()
                                                                                                : account.clearPrimary()))
                                                                .then(recordSystemAuditUseCase.record(
                                                                                context.tenantId(),
                                                                                context.organizationId(),
                                                                                context.userId(),
                                                                                "THIRD_PARTY_BANK_ACCOUNT_PRIMARY_UPDATED",
                                                                                "THIRD_PARTY", thirdPartyId.toString(),
                                                                                target.iban()))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> definePrimaryBankAccount(UUID thirdPartyId, String iban) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(thirdParty -> thirdPartyBankAccountRepository
                                                                .findByIban(context.tenantId(), iban)
                                                                .filter(account -> account.thirdPartyId()
                                                                                .equals(thirdPartyId))
                                                                .flatMap(account -> setPrimaryBankAccount(thirdPartyId,
                                                                                account.id())
                                                                                .thenReturn(thirdParty))
                                                                .switchIfEmpty(addBankAccount(
                                                                                new AddThirdPartyBankAccountCommand(
                                                                                                context.tenantId(),
                                                                                                thirdPartyId, "PRIMARY",
                                                                                                "UNSPECIFIED", iban,
                                                                                                null, "XAF", true))
                                                                                .thenReturn(thirdParty))));
        }

        @Override
        public Mono<ThirdParty> findByBankAccountNumber(UUID organizationId, String bankAccountNumber, String role,
                        Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyBankAccountRepository
                                                .findByIban(context.tenantId(), bankAccountNumber)
                                                .flatMap(account -> thirdPartyRepository.findById(context.tenantId(),
                                                                account.thirdPartyId()))
                                                .filter(thirdParty -> thirdParty.organizationId()
                                                                .equals(organizationId))
                                                .filter(thirdParty -> role == null || role.isBlank()
                                                                || thirdParty.hasRole(role))
                                                .filter(thirdParty -> prospect == null
                                                                || thirdParty.prospect() == prospect)
                                                .switchIfEmpty(Mono.error(
                                                                new ThirdPartyLookupNotFoundException("bank account",
                                                                                bankAccountNumber))));
        }

        @Override
        public Mono<ThirdParty> findByAccountingAccount(UUID organizationId, String accountingAccount, String role,
                        Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository
                                                .findByAccountingAccount(context.tenantId(), organizationId,
                                                                accountingAccount)
                                                .filter(thirdParty -> role == null || role.isBlank()
                                                                || thirdParty.hasRole(role))
                                                .filter(thirdParty -> prospect == null
                                                                || thirdParty.prospect() == prospect)
                                                .switchIfEmpty(Mono.error(new ThirdPartyLookupNotFoundException(
                                                                "accounting account",
                                                                accountingAccount))));
        }

        @Override
        public Mono<ThirdParty> activateThirdParty(UUID thirdPartyId) {
                return mutateLifecycle(thirdPartyId, ThirdParty::activate, "THIRD_PARTY_ACTIVATED",
                                "THIRD_PARTY_ACTIVATED");
        }

        @Override
        public Mono<ThirdParty> deactivateThirdParty(UUID thirdPartyId) {
                return mutateLifecycle(thirdPartyId, ThirdParty::deactivate, "THIRD_PARTY_DEACTIVATED",
                                "THIRD_PARTY_DEACTIVATED");
        }

        @Override
        public Mono<ThirdParty> defineAccountingAccount(UUID thirdPartyId, String accountingAccount) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> assertAccountingAccountAvailable(
                                                                context.tenantId(),
                                                                existing.organizationId(), accountingAccount,
                                                                existing.id())
                                                                .then(thirdPartyBankAccountRepository
                                                                                .findByThirdPartyId(context.tenantId(),
                                                                                                thirdPartyId)
                                                                                .hasElements()
                                                                                .map(hasBankAccount -> applyQualificationPolicy(
                                                                                                existing.defineAccountingAccount(
                                                                                                                accountingAccount),
                                                                                                hasBankAccount,
                                                                                                false)))
                                                                .flatMap(thirdPartyRepository::save)
                                                                .flatMap(saved -> businessEventPublisher
                                                                                .publish(thirdPartyEvent(
                                                                                                "THIRD_PARTY_ACCOUNTING_ACCOUNT_UPDATED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_ACCOUNTING_ACCOUNT_UPDATED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .accountingAccount())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> qualifyThirdParty(UUID thirdPartyId, String segment, Integer qualificationScore) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyRepository
                                                                .save(existing.qualify(segment, qualificationScore))
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_QUALIFIED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_QUALIFIED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                (saved.segment() == null
                                                                                                                ? "UNCLASSIFIED"
                                                                                                                : saved.segment())
                                                                                                                + ":"
                                                                                                                + saved.qualificationScore()))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> recomputeQualificationScore(UUID thirdPartyId) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .hasElements()
                                                                .map(hasBankAccount -> applyQualificationPolicy(
                                                                                existing, hasBankAccount, true))
                                                                .flatMap(thirdPartyRepository::save)
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_SCORE_RECOMPUTED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_SCORE_RECOMPUTED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .qualificationScore())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> scheduleFollowUp(UUID thirdPartyId, Instant nextFollowUpAt) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyRepository
                                                                .save(existing.scheduleFollowUp(nextFollowUpAt))
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_FOLLOW_UP_SCHEDULED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_FOLLOW_UP_SCHEDULED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .nextFollowUpAt())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> completeFollowUp(UUID thirdPartyId, Instant contactedAt, Instant nextFollowUpAt) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyRepository
                                                                .save(existing.completeFollowUp(contactedAt,
                                                                                nextFollowUpAt))
                                                                .flatMap(saved -> businessEventPublisher.publish(
                                                                                thirdPartyEvent("THIRD_PARTY_FOLLOW_UP_COMPLETED",
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                "THIRD_PARTY_FOLLOW_UP_COMPLETED",
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                String.valueOf(saved
                                                                                                                .lastContactedAt())))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdParty> convertProspectToCustomer(UUID thirdPartyId) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> {
                                                        if (!existing.prospect() || !existing.active()) {
                                                                return Mono.error(
                                                                                new ProspectConversionNotAllowedException(
                                                                                                thirdPartyId));
                                                        }
                                                        return thirdPartyBankAccountRepository
                                                                        .findByThirdPartyId(context.tenantId(),
                                                                                        thirdPartyId)
                                                                        .hasElements()
                                                                        .map(hasBankAccount -> applyQualificationPolicy(
                                                                                        existing.convertToCustomer(),
                                                                                        hasBankAccount, true))
                                                                        .flatMap(thirdPartyRepository::save)
                                                                        .flatMap(saved -> businessEventPublisher
                                                                                        .publish(
                                                                                                        thirdPartyEvent("THIRD_PARTY_CONVERTED_TO_CUSTOMER",
                                                                                                                        saved))
                                                                                        .then(recordSystemAuditUseCase
                                                                                                        .record(saved.tenantId(),
                                                                                                                        saved.organizationId(),
                                                                                                                        context.userId(),
                                                                                                                        "THIRD_PARTY_CONVERTED_TO_CUSTOMER",
                                                                                                                        "THIRD_PARTY",
                                                                                                                        saved.id().toString(),
                                                                                                                        saved.referenceCode()))
                                                                                        .thenReturn(saved));
                                                }));
                return transactionalExecutor.transactional(operation);
        }

        @Override
        public Mono<ThirdPartyStatistics> getStatistics(UUID organizationId, String role, Boolean prospect) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository
                                                .findByOrganizationId(context.tenantId(), organizationId)
                                                .filter(thirdParty -> role == null || role.isBlank()
                                                                || thirdParty.hasRole(role))
                                                .filter(thirdParty -> prospect == null
                                                                || thirdParty.prospect() == prospect)
                                                .collectList()
                                                .flatMap(thirdParties -> Flux.fromIterable(thirdParties)
                                                                .flatMap(thirdParty -> thirdPartyBankAccountRepository
                                                                                .findByThirdPartyId(context.tenantId(),
                                                                                                thirdParty.id())
                                                                                .hasElements()
                                                                                .map(hasBankAccount -> Map.entry(
                                                                                                thirdParty,
                                                                                                hasBankAccount)))
                                                                .collectList()
                                                                .map(entries -> buildStatistics(entries,
                                                                                thirdParties))));
        }

        @Override
        public Mono<Long> getProspectConversionCount(UUID organizationId) {
                return ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository
                                                .findByOrganizationId(context.tenantId(), organizationId)
                                                .filter(thirdParty -> !thirdParty.prospect())
                                                .filter(thirdParty -> thirdParty.convertedAt() != null)
                                                .count());
        }

        private Mono<Void> assertReferenceAvailable(UUID tenantId, UUID organizationId, String referenceCode) {
                return thirdPartyRepository.existsByReference(tenantId, organizationId, referenceCode)
                                .flatMap(exists -> exists
                                                ? Mono.error(new DuplicateThirdPartyReferenceException(referenceCode))
                                                : Mono.empty());
        }

        private Mono<Void> assertAccountingAccountAvailable(UUID tenantId, UUID organizationId,
                        String accountingAccount,
                        UUID excludedThirdPartyId) {
                return thirdPartyRepository.existsByAccountingAccount(tenantId, organizationId, accountingAccount,
                                excludedThirdPartyId)
                                .flatMap(exists -> exists
                                                ? Mono.error(new ThirdPartyAccountingAccountAlreadyExistsException(
                                                                accountingAccount))
                                                : Mono.empty());
        }

        private Mono<ThirdParty> mutateLifecycle(UUID thirdPartyId, Function<ThirdParty, ThirdParty> mutator,
                        String eventType, String auditAction) {
                Mono<ThirdParty> operation = ReactiveRequestContextHolder.getRequiredContext()
                                .flatMap(context -> thirdPartyRepository.findById(context.tenantId(), thirdPartyId)
                                                .switchIfEmpty(Mono
                                                                .error(new ThirdPartyNotFoundException(thirdPartyId)))
                                                .flatMap(existing -> thirdPartyBankAccountRepository
                                                                .findByThirdPartyId(context.tenantId(),
                                                                                thirdPartyId)
                                                                .hasElements()
                                                                .map(hasBankAccount -> applyQualificationPolicy(
                                                                                mutator.apply(existing),
                                                                                hasBankAccount, false))
                                                                .flatMap(thirdPartyRepository::save)
                                                                .flatMap(saved -> businessEventPublisher
                                                                                .publish(thirdPartyEvent(eventType,
                                                                                                saved))
                                                                                .then(recordSystemAuditUseCase.record(
                                                                                                saved.tenantId(),
                                                                                                saved.organizationId(),
                                                                                                context.userId(),
                                                                                                auditAction,
                                                                                                "THIRD_PARTY",
                                                                                                saved.id().toString(),
                                                                                                saved.referenceCode()))
                                                                                .thenReturn(saved))));
                return transactionalExecutor.transactional(operation);
        }

        private ThirdPartyStatistics buildStatistics(List<Map.Entry<ThirdParty, Boolean>> entries,
                        List<ThirdParty> thirdParties) {
                long activeCount = thirdParties.stream().filter(ThirdParty::active).count();
                long prospectCount = thirdParties.stream().filter(ThirdParty::prospect).count();
                long convertedCount = thirdParties.stream().filter(thirdParty -> thirdParty.convertedAt() != null)
                                .count();
                long withBankAccountCount = entries.stream().filter(Map.Entry::getValue).count();
                return new ThirdPartyStatistics(thirdParties.size(), activeCount, thirdParties.size() - activeCount,
                                prospectCount, convertedCount, withBankAccountCount);
        }

        private Flux<ThirdPartySearchResult> searchFromRepository(UUID tenantId, UUID organizationId, String query,
                        String role, Boolean prospect, String segment, Integer minimumQualificationScore,
                        Boolean active, String followUpStatus, int page, int size) {
                String normalizedQuery = query == null ? null : query.trim();
                String normalizedSegment = segment == null ? null : segment.trim().toUpperCase();
                String normalizedFollowUp = followUpStatus == null ? null : followUpStatus.trim().toUpperCase();
                return thirdPartyRepository.findByOrganizationId(tenantId, organizationId)
                                .filter(thirdParty -> role == null || role.isBlank() || thirdParty.hasRole(role))
                                .filter(thirdParty -> prospect == null || thirdParty.prospect() == prospect)
                                .filter(thirdParty -> active == null || thirdParty.active() == active)
                                .filter(thirdParty -> normalizedSegment == null || normalizedSegment.isBlank()
                                                || normalizedSegment.equals(thirdParty.segment()))
                                .filter(thirdParty -> normalizedFollowUp == null || normalizedFollowUp.isBlank()
                                                || normalizedFollowUp.equals(thirdParty.followUpStatus()))
                                .filter(thirdParty -> minimumQualificationScore == null
                                                || (thirdParty.qualificationScore() != null
                                                                && thirdParty.qualificationScore() >= minimumQualificationScore))
                                .filter(thirdParty -> matchesQuery(thirdParty, normalizedQuery))
                                .map(this::toSearchResult)
                                .skip((long) page * size)
                                .take(size);
        }

        private boolean matchesQuery(ThirdParty thirdParty, String query) {
                if (query == null || query.isBlank()) {
                        return true;
                }
                String normalized = query.toUpperCase();
                return thirdParty.referenceCode().contains(normalized)
                                || thirdParty.displayName().toUpperCase().contains(normalized)
                                || (thirdParty.longName() != null && thirdParty.longName().toUpperCase().contains(normalized))
                                || (thirdParty.acronym() != null && thirdParty.acronym().toUpperCase().contains(normalized));
        }

        private ThirdPartySearchResult toSearchResult(ThirdParty thirdParty) {
                return new ThirdPartySearchResult(
                                thirdParty.id(),
                                thirdParty.tenantId(),
                                thirdParty.organizationId(),
                                thirdParty.partyRef().partyType(),
                                thirdParty.partyRef().partyId(),
                                thirdParty.code(),
                                thirdParty.name(),
                                thirdParty.type(),
                                thirdParty.longName(),
                                thirdParty.roles(),
                                thirdParty.prospect(),
                                thirdParty.accountingAccount(),
                                thirdParty.segment(),
                                thirdParty.qualificationScore(),
                                thirdParty.enabled(),
                                thirdParty.lastContactedAt(),
                                thirdParty.nextFollowUpAt(),
                                thirdParty.followUpStatus(),
                                thirdParty.convertedAt());
        }

        private ThirdParty applyQualificationPolicy(ThirdParty thirdParty, boolean hasBankAccount,
                        boolean forceRecompute) {
                Integer score = thirdParty.qualificationScore();
                String segment = thirdParty.segment();
                boolean autoManaged = forceRecompute || isAutoManagedQualification(thirdParty);

                if (score == null || autoManaged) {
                        score = computeQualificationScore(thirdParty, hasBankAccount);
                }
                if (segment == null || autoManaged) {
                        segment = deriveSegment(score);
                }
                return thirdParty.qualify(segment, score);
        }

        private ThirdParty prepareNewThirdPartyForInsert(ThirdParty thirdParty, boolean hasBankAccount) {
                ThirdParty qualified = applyQualificationPolicy(thirdParty, hasBankAccount, false);
                return ThirdParty.rehydrate(qualified.id(), qualified.tenantId(), thirdParty.createdAt(),
                                thirdParty.createdAt(),
                                qualified.organizationId(), qualified.partyRef(), qualified.referenceCode(),
                                qualified.displayName(),
                                qualified.roles(), qualified.prospect(), qualified.accountingAccount(),
                                qualified.segment(),
                                qualified.qualificationScore(), qualified.active(), qualified.lastContactedAt(),
                                qualified.nextFollowUpAt(), qualified.followUpStatus(), qualified.convertedAt(),
                                qualified.type(), qualified.legalForm(), qualified.uniqueIdentificationNumber(),
                                qualified.tradeRegistrationNumber(), qualified.name(), qualified.acronym(),
                                qualified.longName(), qualified.logoUri(), qualified.logoId(),
                                qualified.accountingAccountNumbers(), qualified.authorizedPaymentMethods(),
                                qualified.authorizedCreditLimit(), qualified.maxDiscountRate(), qualified.vatSubject(),
                                qualified.operationsBalance(), qualified.openingBalance(), qualified.payTermNumber(),
                                qualified.payTermType(), qualified.thirdPartyFamily(), qualified.classification(),
                                qualified.taxNumber(), qualified.loyaltyPoints(), qualified.loyaltyPointsUsed(),
                                qualified.loyaltyPointsExpired(), qualified.deletedAt());
        }

        private boolean isAutoManagedQualification(ThirdParty thirdParty) {
                return thirdParty.segment() == null || AUTO_MANAGED_SEGMENTS.contains(thirdParty.segment());
        }

        private int computeQualificationScore(ThirdParty thirdParty, boolean hasBankAccount) {
                int score = 0;
                if (thirdParty.active()) {
                        score += 20;
                }
                score += thirdParty.prospect() ? 25 : 10;
                if (thirdParty.accountingAccount() != null) {
                        score += 20;
                }
                if (hasBankAccount) {
                        score += 20;
                }
                score += Math.min(thirdParty.roles().size() * 5, 15);
                if (thirdParty.segment() != null && thirdParty.segment().startsWith("VIP")) {
                        score += 10;
                }
                return Math.min(score, 100);
        }

        private String deriveSegment(int score) {
                if (score >= 80) {
                        return "HOT";
                }
                if (score >= 50) {
                        return "QUALIFIED";
                }
                return "NEW";
        }

        private BusinessEvent thirdPartyEvent(String eventType, ThirdParty thirdParty) {
                return BusinessEvent.now(thirdParty.tenantId(), thirdParty.organizationId(), eventType,
                                "THIRD_PARTY", thirdParty.id(), payload(
                                                "code", thirdParty.code(),
                                                "referenceCode", thirdParty.referenceCode(),
                                                "name", thirdParty.name(),
                                                "displayName", thirdParty.displayName(),
                                                "type", thirdParty.type(),
                                                "longName", thirdParty.longName(),
                                                "roles", thirdParty.roles(),
                                                "prospect", thirdParty.prospect(),
                                                "partyType", thirdParty.partyRef().partyType().name(),
                                                "partyId", thirdParty.partyRef().partyId(),
                                                "accountingAccount", thirdParty.accountingAccount(),
                                                "segment", thirdParty.segment(),
                                                "qualificationScore", thirdParty.qualificationScore(),
                                                "lastContactedAt", thirdParty.lastContactedAt(),
                                                "nextFollowUpAt", thirdParty.nextFollowUpAt(),
                                                "followUpStatus", thirdParty.followUpStatus(),
                                                "enabled", thirdParty.enabled(),
                                                "active", thirdParty.active(),
                                                "classification", thirdParty.classification(),
                                                "convertedAt", thirdParty.convertedAt()));
        }

        private Map<String, Object> payload(Object... entries) {
                Map<String, Object> payload = new LinkedHashMap<>();
                for (int index = 0; index < entries.length; index += 2) {
                        payload.put(entries[index].toString(), entries[index + 1]);
                }
                return payload;
        }
}
