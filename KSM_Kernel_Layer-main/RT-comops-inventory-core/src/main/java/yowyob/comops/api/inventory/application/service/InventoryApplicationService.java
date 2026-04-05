package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.inventory.application.port.in.GetStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementCommand;
import yowyob.comops.api.inventory.application.port.in.RecordStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateStockMovementUseCase;
import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.domain.DuplicateStockMovementReferenceException;
import yowyob.comops.api.inventory.domain.StockMovementNotFoundException;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class InventoryApplicationService implements RecordStockMovementUseCase, GetStockMovementUseCase,
        ValidateStockMovementUseCase {

    private final StockMovementRepository stockMovementRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public InventoryApplicationService(StockMovementRepository stockMovementRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.stockMovementRepository = stockMovementRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<StockMovement> recordMovement(RecordStockMovementCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(resolveReferenceNumber(command)
                .map(referenceNumber -> StockMovement.record(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.productId(), command.thirdPartyId(), referenceNumber,
                        command.sourceDocumentType(), command.sourceDocumentNumber(), command.movementType(),
                        command.quantity()))
                .flatMap(stockMovement -> stockMovementRepository.existsByReference(stockMovement.tenantId(),
                        stockMovement.organizationId(), stockMovement.referenceNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateStockMovementReferenceException(stockMovement.referenceNumber()))
                        : stockMovementRepository.save(stockMovement)))
                .flatMap(saved -> businessEventPublisher.publish(stockMovementRecordedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<StockMovement> getMovement(java.util.UUID movementId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> stockMovementRepository.findById(context.tenantId(), movementId))
                .switchIfEmpty(Mono.error(new StockMovementNotFoundException(movementId)));
    }

    @Override
    public Mono<StockMovement> validate(java.util.UUID movementId) {
        return transactionalExecutor.transactional(ReactiveRequestContextHolder
                .getRequiredContext()
                .flatMap(context -> stockMovementRepository.findById(context.tenantId(), movementId))
                .switchIfEmpty(Mono.error(new StockMovementNotFoundException(movementId)))
                .map(StockMovement::validate)
                .flatMap(stockMovementRepository::save));
    }

    private Mono<String> resolveReferenceNumber(RecordStockMovementCommand command) {
        if (command.referenceNumber() != null && !command.referenceNumber().isBlank()) {
            return Mono.just(command.referenceNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), command.agencyId(), DocumentTypes.STOCK_MOVEMENT));
    }

    private BusinessEvent stockMovementRecordedEvent(StockMovement movement) {
        return BusinessEvent.now(movement.tenantId(), movement.organizationId(), "STOCK_MOVEMENT_RECORDED",
                "STOCK_MOVEMENT", movement.id(), payload(
                        "referenceNumber", movement.referenceNumber(),
                        "agencyId", movement.agencyId(),
                        "productId", movement.productId(),
                        "thirdPartyId", movement.thirdPartyId(),
                        "sourceDocumentType", movement.sourceDocumentType(),
                        "sourceDocumentNumber", movement.sourceDocumentNumber(),
                        "movementType", movement.movementType(),
                        "quantity", movement.quantity(),
                        "signedQuantity", movement.signedQuantity()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
