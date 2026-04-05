package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.Organization;
import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        UUID tenantId,
        UUID businessActorId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String code,
        String legalName,
        String displayName,
        String organizationType) {

    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(
                organization.id(),
                organization.tenantId(),
                organization.businessActorId(),
                organization.governanceStatus().name(),
                organization.governedByUserId(),
                organization.governedAt(),
                organization.governanceReason(),
                organization.code(),
                organization.legalName(),
                organization.displayName(),
                organization.organizationType());
    }
}
