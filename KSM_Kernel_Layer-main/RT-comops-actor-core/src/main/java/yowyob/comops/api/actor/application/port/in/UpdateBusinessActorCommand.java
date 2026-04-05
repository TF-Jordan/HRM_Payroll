package yowyob.comops.api.actor.application.port.in;

import java.util.UUID;

public record UpdateBusinessActorCommand(
        UUID tenantId,
        UUID userId,
        String name,
        String businessId,
        String niu,
        String tradeRegistryNumber,
        String website,
        String contactPhone,
        String privateAddress,
        String businessAddress,
        String businessProfile) {
}
