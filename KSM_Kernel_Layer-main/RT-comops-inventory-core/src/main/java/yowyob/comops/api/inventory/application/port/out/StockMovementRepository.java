package yowyob.comops.api.inventory.application.port.out;

import yowyob.comops.api.inventory.domain.model.StockMovement;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockMovementRepository {

    Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceNumber);

    Mono<StockMovement> findById(UUID tenantId, UUID stockMovementId);

    Flux<StockMovement> findByAgencyAndProduct(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId);

    Mono<StockMovement> save(StockMovement stockMovement);
}
