package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.common.domain.model.PartyType;
import java.util.Set;
import java.util.UUID;

public record CreateThirdPartyCommand(
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
        boolean active) {
}
