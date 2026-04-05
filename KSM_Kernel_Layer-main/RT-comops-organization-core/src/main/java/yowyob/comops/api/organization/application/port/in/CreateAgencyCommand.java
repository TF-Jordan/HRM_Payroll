package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

public record CreateAgencyCommand(UUID tenantId, UUID organizationId, String code, String name, String agencyType) {
}
