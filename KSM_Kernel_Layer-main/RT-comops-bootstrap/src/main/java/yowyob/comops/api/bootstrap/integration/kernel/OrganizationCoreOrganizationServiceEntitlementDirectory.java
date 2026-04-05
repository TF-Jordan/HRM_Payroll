package yowyob.comops.api.bootstrap.integration.kernel;

import yowyob.comops.api.kernel.application.port.out.OrganizationServiceEntitlementDirectory;
import yowyob.comops.api.organization.application.port.in.CheckOrganizationServiceEntitlementUseCase;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrganizationCoreOrganizationServiceEntitlementDirectory
        implements OrganizationServiceEntitlementDirectory {

    private final CheckOrganizationServiceEntitlementUseCase checkOrganizationServiceEntitlementUseCase;

    public OrganizationCoreOrganizationServiceEntitlementDirectory(
            CheckOrganizationServiceEntitlementUseCase checkOrganizationServiceEntitlementUseCase) {
        this.checkOrganizationServiceEntitlementUseCase = checkOrganizationServiceEntitlementUseCase;
    }

    @Override
    public Mono<Boolean> hasEffectiveService(UUID tenantId, UUID organizationId, String serviceCode) {
        return checkOrganizationServiceEntitlementUseCase.hasOrganizationService(tenantId, organizationId, serviceCode);
    }
}
