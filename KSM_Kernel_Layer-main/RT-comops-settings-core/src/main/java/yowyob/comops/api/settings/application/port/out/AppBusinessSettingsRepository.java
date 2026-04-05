package yowyob.comops.api.settings.application.port.out;

import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface AppBusinessSettingsRepository {

    Mono<AppBusinessSettings> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<AppBusinessSettings> save(AppBusinessSettings settings);
}
