package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCommercialThirdPartyRequest(
        @NotNull UUID organizationId,
        @NotNull PartyType partyType,
        @NotNull UUID partyId,
        @NotBlank String referenceCode,
        @NotBlank String displayName,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        Boolean active,
        boolean prospect) {
}
