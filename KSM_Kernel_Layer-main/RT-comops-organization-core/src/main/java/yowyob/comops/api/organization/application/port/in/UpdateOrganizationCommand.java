package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

public record UpdateOrganizationCommand(
        UUID tenantId,
        UUID organizationId,
        UUID currentBusinessActorId,
        String code,
        String legalName,
        String displayName,
        String organizationType) {
}
