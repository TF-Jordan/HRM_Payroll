package yowyob.comops.api.organization.domain.model;

import java.util.UUID;

public record OrganizationSearchResult(
        UUID id,
        UUID tenantId,
        UUID businessActorId,
        String code,
        String legalName,
        String displayName,
        String organizationType) {
}
