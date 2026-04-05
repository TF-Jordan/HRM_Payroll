package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.domain.model.ThirdPartySearchResult;
import java.util.Set;
import java.util.UUID;

public record ThirdPartySearchResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        PartyType partyType,
        UUID partyId,
        String referenceCode,
        String displayName,
        Set<String> roles,
        boolean prospect,
        String segment,
        Integer qualificationScore,
        boolean active,
        String followUpStatus) {

    public static ThirdPartySearchResponse from(ThirdPartySearchResult result) {
        return new ThirdPartySearchResponse(result.id(), result.tenantId(), result.organizationId(),
                result.partyType(), result.partyId(), result.referenceCode(), result.displayName(), result.roles(),
                result.prospect(), result.segment(), result.qualificationScore(), result.active(),
                result.followUpStatus());
    }
}
