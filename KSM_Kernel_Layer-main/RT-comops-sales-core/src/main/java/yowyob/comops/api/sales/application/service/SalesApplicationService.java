package yowyob.comops.api.sales.application.service;

import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.sales.application.port.in.CreateSalesOrderCommand;
import yowyob.comops.api.sales.application.port.in.CreateSalesOrderLineCommand;
import yowyob.comops.api.sales.application.port.in.CreateSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.in.UpdateSalesOrderCommand;
import yowyob.comops.api.sales.application.port.in.UpdateSalesOrderUseCase;
import yowyob.comops.api.sales.application.port.out.SalesOrderRepository;
import yowyob.comops.api.sales.domain.DuplicateOrderNumberException;
import yowyob.comops.api.sales.domain.SalesOrderNotFoundException;
import yowyob.comops.api.sales.domain.model.SalesOrder;
import yowyob.comops.api.sales.domain.model.SalesOrderLine;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SalesApplicationService implements CreateSalesOrderUseCase, UpdateSalesOrderUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public SalesApplicationService(SalesOrderRepository salesOrderRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.salesOrderRepository = salesOrderRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<SalesOrder> createOrder(CreateSalesOrderCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(resolveOrderNumber(command)
                .map(orderNumber -> SalesOrder.create(command.tenantId(), command.organizationId(), command.agencyId(),
                        command.customerThirdPartyId(), orderNumber, resolveLines(command), command.currency()))
                .flatMap(salesOrder -> salesOrderRepository.existsByOrderNumber(salesOrder.tenantId(),
                        salesOrder.organizationId(), salesOrder.orderNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateOrderNumberException(salesOrder.orderNumber()))
                        : salesOrderRepository.save(salesOrder)))
                .flatMap(saved -> businessEventPublisher.publish(orderCreatedEvent(saved)).thenReturn(saved)));
    }

    @Override
    public Mono<SalesOrder> updateOrder(UpdateSalesOrderCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(salesOrderRepository.findById(command.orderId())
                .switchIfEmpty(Mono.error(new SalesOrderNotFoundException(command.orderId())))
                .flatMap(existing -> resolveOrderNumber(command)
                        .map(orderNumber -> existing.update(command.organizationId(), command.agencyId(),
                                command.customerThirdPartyId(), orderNumber, resolveLines(command), command.currency())))
                .flatMap(updated -> salesOrderRepository.existsByOrderNumberExcludingId(updated.tenantId(),
                                updated.organizationId(), updated.orderNumber(), updated.id())
                        .flatMap(exists -> exists
                                ? Mono.error(new DuplicateOrderNumberException(updated.orderNumber()))
                                : salesOrderRepository.save(updated))));
    }

    private Mono<String> resolveOrderNumber(CreateSalesOrderCommand command) {
        if (command.orderNumber() != null && !command.orderNumber().isBlank()) {
            return Mono.just(command.orderNumber().trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(command.tenantId(),
                command.organizationId(), command.agencyId(), DocumentTypes.SALES_ORDER));
    }

    private Mono<String> resolveOrderNumber(UpdateSalesOrderCommand command) {
        if (command.orderNumber() == null || command.orderNumber().isBlank()) {
            throw new IllegalArgumentException("orderNumber is required");
        }
        return Mono.just(command.orderNumber().trim());
    }

    private List<SalesOrderLine> resolveLines(CreateSalesOrderCommand command) {
        if (command.lines() != null && !command.lines().isEmpty()) {
            return command.lines().stream()
                    .map(this::toDomainLine)
                    .toList();
        }
        if (command.productId() == null || command.quantity() == null || command.unitPrice() == null) {
            throw new IllegalArgumentException("either lines or legacy product/quantity/unitPrice fields are required");
        }
        return List.of(SalesOrderLine.create(command.productId(), command.quantity(), command.unitPrice()));
    }

    private List<SalesOrderLine> resolveLines(UpdateSalesOrderCommand command) {
        if (command.lines() != null && !command.lines().isEmpty()) {
            return command.lines().stream()
                    .map(this::toDomainLine)
                    .toList();
        }
        if (command.productId() == null || command.quantity() == null || command.unitPrice() == null) {
            throw new IllegalArgumentException("either lines or legacy product/quantity/unitPrice fields are required");
        }
        return List.of(SalesOrderLine.create(command.productId(), command.quantity(), command.unitPrice()));
    }

    private SalesOrderLine toDomainLine(CreateSalesOrderLineCommand line) {
        return SalesOrderLine.create(line.productId(), line.quantity(), line.unitPrice());
    }

    private BusinessEvent orderCreatedEvent(SalesOrder order) {
        return BusinessEvent.now(order.tenantId(), order.organizationId(), "SALES_ORDER_CREATED", "SALES_ORDER",
                order.id(), payload(
                        "orderNumber", order.orderNumber(),
                        "agencyId", order.agencyId(),
                        "customerThirdPartyId", order.customerThirdPartyId(),
                        "status", order.status(),
                        "currency", order.currency(),
                        "totalQuantity", order.totalQuantity(),
                        "totalAmount", order.totalAmount()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
