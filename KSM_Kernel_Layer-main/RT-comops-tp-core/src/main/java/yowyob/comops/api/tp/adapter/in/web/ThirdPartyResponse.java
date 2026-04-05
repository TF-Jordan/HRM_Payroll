package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.domain.model.ThirdParty;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ThirdPartyResponse(
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
        Instant lastContactedAt,
        Instant nextFollowUpAt,
        String followUpStatus,
        boolean active,
        Instant convertedAt) {

    public static ThirdPartyResponse from(ThirdParty thirdParty) {
        return new ThirdPartyResponse(
                thirdParty.id(),
                thirdParty.tenantId(),
                thirdParty.organizationId(),
                thirdParty.partyRef().partyType(),
                thirdParty.partyRef().partyId(),
                thirdParty.referenceCode(),
                thirdParty.displayName(),
                thirdParty.roles(),
                thirdParty.prospect(),
                thirdParty.accountingAccount(),
                thirdParty.segment(),
                thirdParty.qualificationScore(),
                thirdParty.lastContactedAt(),
                thirdParty.nextFollowUpAt(),
                thirdParty.followUpStatus(),
                thirdParty.active(),
                thirdParty.convertedAt());
    }
}
