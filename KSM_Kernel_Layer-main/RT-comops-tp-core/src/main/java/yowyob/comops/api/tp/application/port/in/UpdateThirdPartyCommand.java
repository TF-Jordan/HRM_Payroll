package yowyob.comops.api.tp.application.port.in;

import java.util.Set;
import java.util.UUID;

public record UpdateThirdPartyCommand(
        UUID tenantId,
        UUID thirdPartyId,
        String referenceCode,
        String displayName,
        Set<String> roles,
        boolean prospect,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        boolean active) {
}
