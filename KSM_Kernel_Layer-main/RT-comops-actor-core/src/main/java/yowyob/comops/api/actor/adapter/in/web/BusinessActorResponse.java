package yowyob.comops.api.actor.adapter.in.web;

import yowyob.comops.api.actor.domain.model.BusinessActorProfile;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record BusinessActorResponse(
        UUID id,
        UUID tenantId,
        UUID actorId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String code,
        boolean isIndividual,
        boolean isAvailable,
        boolean isVerified,
        boolean isActive,
        String type,
        String role,
        Set<String> qualifications,
        Set<String> paymentMethods,
        Set<UUID> addresses,
        String biography,
        Instant deletedAt,
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
                profile.governanceReason(), profile.code(), profile.isIndividual(), profile.isAvailable(),
                profile.isVerified(), profile.isActive(), profile.type(), profile.role(), profile.qualifications(),
                profile.paymentMethods(), profile.addresses(), profile.biography(), profile.deletedAt(), profile.name(),
                profile.businessId(), profile.niu(), profile.tradeRegistryNumber(), profile.website(),
                profile.contactPhone(), profile.privateAddress(), profile.businessAddress(),
                profile.businessProfile());
    }
}
