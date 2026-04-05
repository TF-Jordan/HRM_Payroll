package yowyob.comops.api.accounting.application.service;

import yowyob.comops.api.accounting.application.port.in.CreateInvoiceCommand;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceFromSalesOrderUseCase;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceLineCommand;
import yowyob.comops.api.accounting.application.port.in.CreateInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.DeleteInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.GetInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.in.ListInvoicesUseCase;
import yowyob.comops.api.accounting.application.port.in.UpdateInvoiceCommand;
import yowyob.comops.api.accounting.application.port.in.UpdateInvoiceUseCase;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderLineSnapshot;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderProvider;
import yowyob.comops.api.accounting.application.port.out.ConfirmedSalesOrderSnapshot;
import yowyob.comops.api.accounting.application.port.out.InvoiceRepository;
import yowyob.comops.api.accounting.domain.DuplicateInvoiceNumberException;
import yowyob.comops.api.accounting.domain.InvoiceAlreadyExistsForOrderException;
import yowyob.comops.api.accounting.domain.InvoiceNotFoundException;
import yowyob.comops.api.accounting.domain.SalesOrderInvoiceSourceNotFoundException;
import yowyob.comops.api.accounting.domain.model.Invoice;
import yowyob.comops.api.accounting.domain.model.InvoiceLine;
import yowyob.comops.api.common.domain.model.DocumentTypes;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountingApplicationService implements CreateInvoiceUseCase, CreateInvoiceFromSalesOrderUseCase,
        GetInvoiceUseCase, ListInvoicesUseCase, UpdateInvoiceUseCase, DeleteInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final GenerateDocumentNumberUseCase generateDocumentNumberUseCase;
    private final ConfirmedSalesOrderProvider confirmedSalesOrderProvider;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public AccountingApplicationService(InvoiceRepository invoiceRepository,
            GenerateDocumentNumberUseCase generateDocumentNumberUseCase,
            ConfirmedSalesOrderProvider confirmedSalesOrderProvider,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.invoiceRepository = invoiceRepository;
        this.generateDocumentNumberUseCase = generateDocumentNumberUseCase;
        this.confirmedSalesOrderProvider = confirmedSalesOrderProvider;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Invoice> createInvoice(CreateInvoiceCommand command) {
        Objects.requireNonNull(command, "command is required");
        List<InvoiceLine> lines = normalizeLines(command.lines());
        return transactionalExecutor.transactional(resolveInvoiceNumber(command.tenantId(), command.organizationId(),
                        command.invoiceNumber())
                .map(invoiceNumber -> Invoice.create(command.tenantId(), command.organizationId(),
                        command.customerThirdPartyId(), command.orderId(), invoiceNumber, lines, command.currency()))
                .flatMap(this::ensureInvoiceCanBeSaved)
                .flatMap(saved -> businessEventPublisher.publish(invoiceCreatedEvent(saved, "MANUAL")).thenReturn(saved)));
    }

    @Override
    public Mono<Invoice> createFromSalesOrder(UUID tenantId, UUID orderId) {
        return transactionalExecutor.transactional(confirmedSalesOrderProvider.getConfirmedOrder(tenantId, orderId)
                .switchIfEmpty(Mono.error(new SalesOrderInvoiceSourceNotFoundException(orderId)))
                .flatMap(snapshot -> invoiceRepository.existsByOrderId(orderId)
                        .flatMap(exists -> exists
                                ? Mono.error(new InvoiceAlreadyExistsForOrderException(orderId))
                                : createInvoiceFromSnapshot(snapshot))));
    }

    @Override
    public Mono<Invoice> getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(invoiceId)));
    }

    @Override
    public Flux<Invoice> listInvoices(UUID tenantId, UUID organizationId) {
        return invoiceRepository.findByOrganizationId(tenantId, organizationId);
    }

    @Override
    public Mono<Invoice> updateInvoice(UpdateInvoiceCommand command) {
        Objects.requireNonNull(command, "command is required");
        return transactionalExecutor.transactional(invoiceRepository.findById(command.invoiceId())
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(command.invoiceId())))
                .flatMap(existing -> {
                    if (!existing.organizationId().equals(command.organizationId())) {
                        return Mono.error(new IllegalArgumentException("invoice does not belong to the provided organization"));
                    }
                    Invoice updated = existing.update(command.organizationId(), command.customerThirdPartyId(),
                            command.orderId(), command.invoiceNumber(), normalizeLines(command.lines()),
                            command.currency());
                    return invoiceRepository.existsByInvoiceNumberExcludingId(updated.tenantId(), updated.organizationId(),
                                    updated.invoiceNumber(), updated.id())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateInvoiceNumberException(updated.invoiceNumber()))
                                    : invoiceRepository.save(updated));
                }));
    }

    @Override
    public Mono<Void> deleteInvoice(UUID invoiceId) {
        return transactionalExecutor.transactional(invoiceRepository.findById(invoiceId)
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(invoiceId)))
                .flatMap(existing -> {
                    if (!"DRAFT".equals(existing.status())) {
                        return Mono.error(new IllegalArgumentException("only DRAFT invoices can be deleted"));
                    }
                    return invoiceRepository.deleteById(existing.id());
                }));
    }

    private Mono<Invoice> createInvoiceFromSnapshot(ConfirmedSalesOrderSnapshot snapshot) {
        List<InvoiceLine> lines = snapshot.lines().stream()
                .map(this::toInvoiceLine)
                .toList();
        return resolveInvoiceNumber(snapshot.tenantId(), snapshot.organizationId(), null)
                .map(invoiceNumber -> Invoice.create(snapshot.tenantId(), snapshot.organizationId(),
                        snapshot.customerThirdPartyId(), snapshot.orderId(), invoiceNumber, lines, snapshot.currency()))
                .flatMap(this::ensureInvoiceCanBeSaved)
                .flatMap(saved -> businessEventPublisher.publish(invoiceCreatedEvent(saved, "SALES_ORDER")).thenReturn(saved));
    }

    private Mono<Invoice> ensureInvoiceCanBeSaved(Invoice invoice) {
        return invoiceRepository.existsByInvoiceNumber(invoice.tenantId(), invoice.organizationId(), invoice.invoiceNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateInvoiceNumberException(invoice.invoiceNumber()))
                        : invoiceRepository.save(invoice));
    }

    private List<InvoiceLine> normalizeLines(List<CreateInvoiceLineCommand> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("at least one invoice line is required");
        }
        return lines.stream()
                .map(this::toInvoiceLine)
                .toList();
    }

    private InvoiceLine toInvoiceLine(CreateInvoiceLineCommand line) {
        Objects.requireNonNull(line, "invoice line is required");
        return InvoiceLine.create(line.productId(), line.quantity(), line.unitPrice());
    }

    private InvoiceLine toInvoiceLine(ConfirmedSalesOrderLineSnapshot line) {
        Objects.requireNonNull(line, "sales order line snapshot is required");
        return InvoiceLine.create(line.productId(), line.quantity(), line.unitPrice());
    }

    private Mono<String> resolveInvoiceNumber(UUID tenantId, UUID organizationId, String requestedInvoiceNumber) {
        if (requestedInvoiceNumber != null && !requestedInvoiceNumber.isBlank()) {
            return Mono.just(requestedInvoiceNumber.trim());
        }
        return generateDocumentNumberUseCase.generate(new GenerateDocumentNumberCommand(tenantId,
                organizationId, null, DocumentTypes.SALES_INVOICE));
    }

    private BusinessEvent invoiceCreatedEvent(Invoice invoice, String source) {
        return BusinessEvent.now(invoice.tenantId(), invoice.organizationId(), "INVOICE_CREATED", "INVOICE",
                invoice.id(), payload(
                        "invoiceNumber", invoice.invoiceNumber(),
                        "customerThirdPartyId", invoice.customerThirdPartyId(),
                        "orderId", invoice.orderId(),
                        "status", invoice.status(),
                        "paymentStatus", invoice.paymentStatus(),
                        "currency", invoice.currency(),
                        "lineCount", invoice.lines().size(),
                        "totalQuantity", invoice.totalQuantity(),
                        "totalAmount", invoice.totalAmount(),
                        "source", source));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
