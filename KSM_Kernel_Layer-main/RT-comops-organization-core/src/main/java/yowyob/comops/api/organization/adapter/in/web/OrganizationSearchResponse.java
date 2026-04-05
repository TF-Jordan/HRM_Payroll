package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.OrganizationSearchResult;
import java.util.UUID;

public record OrganizationSearchResponse(
        UUID id,
        UUID tenantId,
        UUID businessActorId,
        String code,
        String legalName,
        String displayName,
        String organizationType) {

    public static OrganizationSearchResponse from(OrganizationSearchResult result) {
        return new OrganizationSearchResponse(
                result.id(),
                result.tenantId(),
                result.businessActorId(),
                result.code(),
                result.legalName(),
                result.displayName(),
                result.organizationType());
    }
}
