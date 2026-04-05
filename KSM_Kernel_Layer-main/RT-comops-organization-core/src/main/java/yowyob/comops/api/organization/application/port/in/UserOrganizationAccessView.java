package yowyob.comops.api.organization.application.port.in;

import java.util.List;
import java.util.UUID;

public record UserOrganizationAccessView(
        UUID organizationId,
        String organizationCode,
        String displayName,
        String legalName,
        List<String> services) {
}
