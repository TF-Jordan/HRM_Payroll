package yowyob.comops.api.tp.domain.model;

import yowyob.comops.api.common.domain.model.PartyType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ThirdPartySearchResult(
                UUID id,
                UUID tenantId,
                UUID organizationId,
                PartyType partyType,
                UUID partyId,
                String referenceCode,
                String displayName,
                Set<String> roles,
                boolean prospect,
                String accountingAccount,
                String segment,
                Integer qualificationScore,
                boolean active,
                Instant lastContactedAt,
                Instant nextFollowUpAt,
                String followUpStatus,
                Instant convertedAt) {
}
