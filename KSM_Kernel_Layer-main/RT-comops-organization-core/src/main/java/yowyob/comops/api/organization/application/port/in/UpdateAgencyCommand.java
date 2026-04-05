package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

public record UpdateAgencyCommand(UUID tenantId, UUID agencyId, String code, String name, String agencyType) {
}
