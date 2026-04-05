package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.inventory.application.port.in.CreateInventorySessionCommand;
import yowyob.comops.api.inventory.application.port.in.CreateInventorySessionUseCase;
import yowyob.comops.api.inventory.application.port.in.ListInventorySessionsUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateInventorySessionUseCase;
import yowyob.comops.api.inventory.application.port.out.InventorySessionRepository;
import yowyob.comops.api.inventory.domain.model.InventorySession;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class InventorySessionApplicationService implements CreateInventorySessionUseCase, ListInventorySessionsUseCase,
        ValidateInventorySessionUseCase {

    private final InventorySessionRepository inventorySessionRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;

    public InventorySessionApplicationService(InventorySessionRepository inventorySessionRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase) {
        this.inventorySessionRepository = inventorySessionRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
    }

    @Override
    public Mono<InventorySession> createSession(CreateInventorySessionCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<String> referenceNumberMono = command.referenceNumber() == null || command.referenceNumber().isBlank()
                ? generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                        command.organizationId(), command.agencyId(), DocumentTypes.INVENTORY_SESSION))
                : Mono.just(command.referenceNumber().trim());
        return referenceNumberMono
                .map(referenceNumber -> InventorySession.create(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.productId(), referenceNumber, command.countedQuantity()))
                .flatMap(inventorySessionRepository::save);
    }

    @Override
    public Flux<InventorySession> listSessions(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> inventorySessionRepository.findByOrganization(context.tenantId(), organizationId));
    }

    @Override
    public Mono<InventorySession> validateSession(UUID inventorySessionId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> inventorySessionRepository.findById(context.tenantId(), inventorySessionId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("inventory session not found")))
                .map(inventorySession -> inventorySession.validate())
                .flatMap(inventorySessionRepository::save);
    }
}
