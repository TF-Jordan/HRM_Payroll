package yowyob.comops.api.settings.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AppBusinessSettingsSpringDataRepository extends ReactiveCrudRepository<AppBusinessSettingsEntity, UUID> {

    Mono<AppBusinessSettingsEntity> findByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
