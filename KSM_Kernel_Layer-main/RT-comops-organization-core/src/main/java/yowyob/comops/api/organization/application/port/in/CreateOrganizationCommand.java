package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

public record CreateOrganizationCommand(
        UUID tenantId,
        UUID businessActorId,
        String code,
        String legalName,
        String displayName,
        String organizationType) {
}
