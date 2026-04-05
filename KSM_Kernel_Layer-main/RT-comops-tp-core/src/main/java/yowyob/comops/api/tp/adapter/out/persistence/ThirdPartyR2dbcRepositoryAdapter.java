package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.common.domain.model.PartyRef;
import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ThirdPartyR2dbcRepositoryAdapter implements ThirdPartyRepository {

    private final ThirdPartySpringDataRepository repository;

    public ThirdPartyR2dbcRepositoryAdapter(ThirdPartySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceCode) {
        return repository.existsByTenantIdAndOrganizationIdAndReferenceCodeIgnoreCase(tenantId, organizationId,
                referenceCode);
    }

    @Override
    public Mono<Boolean> existsByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount,
            UUID excludedThirdPartyId) {
        if (accountingAccount == null || accountingAccount.isBlank()) {
            return Mono.just(false);
        }
        return repository.findFirstByTenantIdAndOrganizationIdAndAccountingAccountIgnoreCase(tenantId, organizationId,
                        accountingAccount)
                .map(entity -> !entity.id().equals(excludedThirdPartyId))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<ThirdParty> findById(UUID tenantId, UUID thirdPartyId) {
        return repository.findByIdAndTenantId(thirdPartyId, tenantId).map(this::toDomain);
    }

    @Override
    public Mono<ThirdParty> findByAccountingAccount(UUID tenantId, UUID organizationId, String accountingAccount) {
        return repository.findFirstByTenantIdAndOrganizationIdAndAccountingAccountIgnoreCase(tenantId, organizationId,
                accountingAccount).map(this::toDomain);
    }

    @Override
    public Flux<ThirdParty> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<ThirdParty> save(ThirdParty thirdParty) {
        ThirdPartyEntity entity = new ThirdPartyEntity(thirdParty.id(), thirdParty.tenantId(), thirdParty.createdAt(),
                thirdParty.updatedAt(), thirdParty.organizationId(), thirdParty.partyRef().partyType().name(),
                thirdParty.partyRef().partyId(), thirdParty.referenceCode(), thirdParty.displayName(), thirdParty.roles(),
                thirdParty.prospect(), thirdParty.accountingAccount(), thirdParty.segment(),
                thirdParty.qualificationScore(), thirdParty.lastContactedAt(), thirdParty.nextFollowUpAt(),
                thirdParty.followUpStatus(), thirdParty.active(), thirdParty.convertedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID thirdPartyId) {
        return repository.findByIdAndTenantId(thirdPartyId, tenantId).flatMap(repository::delete).then();
    }

    private ThirdParty toDomain(ThirdPartyEntity entity) {
        return ThirdParty.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), new PartyRef(PartyType.valueOf(entity.partyType()), entity.partyId()),
                entity.referenceCode(), entity.displayName(), entity.roles(), entity.prospect(),
                entity.accountingAccount(), entity.segment(), entity.qualificationScore(), entity.active(),
                entity.lastContactedAt(), entity.nextFollowUpAt(), entity.followUpStatus(), entity.convertedAt());
    }
}
