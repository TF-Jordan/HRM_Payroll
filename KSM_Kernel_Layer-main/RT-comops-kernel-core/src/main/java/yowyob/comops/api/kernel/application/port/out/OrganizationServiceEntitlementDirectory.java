package yowyob.comops.api.kernel.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OrganizationServiceEntitlementDirectory {

    Mono<Boolean> hasEffectiveService(UUID tenantId, UUID organizationId, String serviceCode);
}
