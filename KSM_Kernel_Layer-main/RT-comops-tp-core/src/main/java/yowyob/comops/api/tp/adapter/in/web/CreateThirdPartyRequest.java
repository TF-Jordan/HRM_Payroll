package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record CreateThirdPartyRequest(
        @NotNull UUID organizationId,
        @NotNull PartyType partyType,
        @NotNull UUID partyId,
        @NotBlank String referenceCode,
        @NotBlank String displayName,
        @NotEmpty Set<String> roles,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        Boolean active,
        boolean prospect) {
}
