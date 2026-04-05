package yowyob.comops.api.auth.application.port.out;

import java.util.List;
import java.util.UUID;

public record UserOrganizationAccess(
        UUID organizationId,
        String organizationCode,
        String displayName,
        String legalName,
        List<String> services) {
}
