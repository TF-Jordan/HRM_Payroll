package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.inventory.application.port.in.RecordTransformationCommand;
import yowyob.comops.api.inventory.application.port.in.RecordTransformationUseCase;
import yowyob.comops.api.inventory.application.port.in.ValidateProductTransformationUseCase;
import yowyob.comops.api.inventory.application.port.out.ProductTransformationRepository;
import yowyob.comops.api.inventory.domain.ProductTransformationNotFoundException;
import yowyob.comops.api.inventory.domain.model.ProductTransformation;
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
public class ProductTransformationApplicationService implements RecordTransformationUseCase,
        ValidateProductTransformationUseCase {

    private final ProductTransformationRepository repository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public ProductTransformationApplicationService(ProductTransformationRepository repository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.repository = repository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<ProductTransformation> recordTransformation(RecordTransformationCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(resolveReferenceNumber(command)
                .map(referenceNumber -> ProductTransformation.record(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.sourceProductId(), command.targetProductId(), referenceNumber,
                        command.sourceQuantity(), command.targetQuantity()))
                .flatMap(repository::save)
                .flatMap(saved -> businessEventPublisher.publish(transformationRecordedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<ProductTransformation> validateTransformation(java.util.UUID transformationId) {
        return transactionalExecutor.transactional(ReactiveRequestContextHolder
                .getRequiredContext()
                .flatMap(context -> repository.findById(context.tenantId(), transformationId))
                .switchIfEmpty(Mono.error(new ProductTransformationNotFoundException(transformationId)))
                .map(ProductTransformation::validate)
                .flatMap(repository::save));
    }

    private Mono<String> resolveReferenceNumber(RecordTransformationCommand command) {
        if (command.referenceNumber() != null && !command.referenceNumber().isBlank()) {
            return Mono.just(command.referenceNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), command.agencyId(), DocumentTypes.PRODUCT_TRANSFORMATION));
    }

    private BusinessEvent transformationRecordedEvent(ProductTransformation transformation) {
        return BusinessEvent.now(transformation.tenantId(), transformation.organizationId(),
                "PRODUCT_TRANSFORMATION_RECORDED", "PRODUCT_TRANSFORMATION", transformation.id(), payload(
                        "referenceNumber", transformation.referenceNumber(),
                        "agencyId", transformation.agencyId(),
                        "sourceProductId", transformation.sourceProductId(),
                        "targetProductId", transformation.targetProductId(),
                        "sourceQuantity", transformation.sourceQuantity(),
                        "targetQuantity", transformation.targetQuantity()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
