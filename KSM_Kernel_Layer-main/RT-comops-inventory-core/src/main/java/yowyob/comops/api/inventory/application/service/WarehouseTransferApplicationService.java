package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.inventory.application.port.in.CreateWarehouseTransferCommand;
import yowyob.comops.api.inventory.application.port.in.CreateWarehouseTransferUseCase;
import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
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

@Service
public class WarehouseTransferApplicationService implements CreateWarehouseTransferUseCase {
    private final WarehouseTransferRepository repository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public WarehouseTransferApplicationService(WarehouseTransferRepository repository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.repository = repository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<WarehouseTransfer> createTransfer(CreateWarehouseTransferCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(resolveReferenceNumber(command)
                .map(referenceNumber -> WarehouseTransfer.create(command.tenantId(), command.organizationId(),
                        command.sourceAgencyId(), command.targetAgencyId(), command.productId(), referenceNumber,
                        command.quantity()))
                .flatMap(repository::save)
                .flatMap(saved -> businessEventPublisher.publish(transferCreatedEvent(saved)).thenReturn(saved)));
    }

    private Mono<String> resolveReferenceNumber(CreateWarehouseTransferCommand command) {
        if (command.referenceNumber() != null && !command.referenceNumber().isBlank()) {
            return Mono.just(command.referenceNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), command.sourceAgencyId(), DocumentTypes.WAREHOUSE_TRANSFER));
    }

    private BusinessEvent transferCreatedEvent(WarehouseTransfer transfer) {
        return BusinessEvent.now(transfer.tenantId(), transfer.organizationId(), "WAREHOUSE_TRANSFER_CREATED",
                "WAREHOUSE_TRANSFER", transfer.id(), payload(
                        "referenceNumber", transfer.referenceNumber(),
                        "sourceAgencyId", transfer.sourceAgencyId(),
                        "targetAgencyId", transfer.targetAgencyId(),
                        "productId", transfer.productId(),
                        "quantity", transfer.quantity(),
                        "status", transfer.status()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
