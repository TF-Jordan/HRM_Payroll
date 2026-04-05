package yowyob.comops.api.actor.adapter.in.web;

import yowyob.comops.api.actor.domain.model.BusinessActorProfile;
import java.time.Instant;
import java.util.UUID;

public record BusinessActorResponse(
        UUID id,
        UUID tenantId,
        UUID actorId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String name,
        String businessId,
        String niu,
        String tradeRegistryNumber,
        String website,
        String contactPhone,
        String privateAddress,
        String businessAddress,
        String businessProfile) {

    public static BusinessActorResponse from(BusinessActorProfile profile) {
        return new BusinessActorResponse(profile.id(), profile.tenantId(), profile.actorId(),
                profile.governanceStatus().name(), profile.governedByUserId(), profile.governedAt(),
                profile.governanceReason(), profile.name(), profile.businessId(), profile.niu(),
                profile.tradeRegistryNumber(), profile.website(), profile.contactPhone(), profile.privateAddress(),
                profile.businessAddress(), profile.businessProfile());
    }
}
