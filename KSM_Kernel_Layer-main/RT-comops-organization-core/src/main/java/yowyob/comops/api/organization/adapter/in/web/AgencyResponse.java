package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.Agency;
import java.time.Instant;
import java.util.UUID;

public record AgencyResponse(UUID id, UUID tenantId, UUID organizationId, String governanceStatus,
        UUID governedByUserId, Instant governedAt, String governanceReason, String code, String name, String agencyType,
        boolean active) {

    public static AgencyResponse from(Agency agency) {
        return new AgencyResponse(agency.id(), agency.tenantId(), agency.organizationId(),
                agency.governanceStatus().name(), agency.governedByUserId(), agency.governedAt(),
                agency.governanceReason(), agency.code(), agency.name(), agency.agencyType(), agency.active());
    }
}
