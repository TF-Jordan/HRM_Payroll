package yowyob.comops.api.settings.application.port.out;

import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DocumentSequenceRepository {

    Mono<DocumentSequence> save(DocumentSequence documentSequence);

    Mono<DocumentSequence> findByScopeAndType(UUID tenantId, UUID organizationId, UUID agencyId, String documentType);
}
