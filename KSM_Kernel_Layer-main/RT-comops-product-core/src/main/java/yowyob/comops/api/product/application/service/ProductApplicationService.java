package yowyob.comops.api.product.application.service;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.product.application.port.in.CreateProductCommand;
import yowyob.comops.api.product.application.port.in.CreateProductUseCase;
import yowyob.comops.api.product.application.port.in.DeleteProductUseCase;
import yowyob.comops.api.product.application.port.in.GetProductUseCase;
import yowyob.comops.api.product.application.port.in.ListProductsUseCase;
import yowyob.comops.api.product.application.port.in.SearchProductsUseCase;
import yowyob.comops.api.product.application.port.in.UpdateProductCommand;
import yowyob.comops.api.product.application.port.in.UpdateProductUseCase;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.application.port.out.ProductSearchGateway;
import yowyob.comops.api.product.domain.DuplicateProductSkuException;
import yowyob.comops.api.product.domain.ProductNotFoundException;
import yowyob.comops.api.product.domain.ProductSearchUnavailableException;
import yowyob.comops.api.product.domain.model.Product;
import yowyob.comops.api.product.domain.model.ProductSearchResult;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class ProductApplicationService implements CreateProductUseCase, GetProductUseCase, ListProductsUseCase,
        SearchProductsUseCase, UpdateProductUseCase, DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final Optional<ProductSearchGateway> productSearchGateway;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public ProductApplicationService(ProductRepository productRepository, BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor, Optional<ProductSearchGateway> productSearchGateway) {
        this.productRepository = productRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
        this.productSearchGateway = productSearchGateway;
    }

    @Override
    public Mono<Product> createProduct(CreateProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Product product = Product.create(command.tenantId(), command.organizationId(), command.sku(), command.name(),
                command.familyCode(), command.variantLabel(), command.barcode(), command.description(),
                command.unitPrice(), command.currency(), command.status());
        Mono<Product> operation = productRepository.existsBySku(product.tenantId(), product.organizationId(), product.sku())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateProductSkuException(product.sku()))
                        : productRepository.save(product)
                                .flatMap(saved -> businessEventPublisher.publish(productCreatedEvent(saved))
                                        .thenReturn(saved)));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Product> getProduct(java.util.UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> productRepository.findById(context.tenantId(), productId))
                .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)));
    }

    @Override
    public Flux<Product> listProducts(java.util.UUID organizationId, String familyCode, String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> productRepository.findByOrganizationId(context.tenantId(), organizationId))
                .filter(product -> familyCode == null || familyCode.isBlank()
                        || product.familyCode().equalsIgnoreCase(familyCode))
                .filter(product -> status == null || status.isBlank()
                        || product.status().equalsIgnoreCase(status));
    }

    @Override
    public Flux<ProductSearchResult> searchProducts(java.util.UUID organizationId, String query, String familyCode,
            String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> productSearchGateway
                        .map(gateway -> gateway.search(context.tenantId(), organizationId, query, familyCode, status))
                        .orElseGet(() -> Flux.error(new ProductSearchUnavailableException())));
    }

    @Override
    public Mono<Product> updateProduct(UpdateProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Product> operation = productRepository.findById(command.tenantId(), command.productId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException(command.productId())))
                .flatMap(existing -> {
                    if (!existing.organizationId().equals(command.organizationId())) {
                        return Mono.error(new IllegalArgumentException("product does not belong to the provided organization"));
                    }
                    Product updated = existing.update(command.sku(), command.name(), command.familyCode(),
                            command.variantLabel(), command.barcode(), command.description(), command.unitPrice(),
                            command.currency(), command.status());
                    return productRepository.existsBySkuExcludingId(updated.tenantId(), updated.organizationId(),
                                    updated.sku(), updated.id())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateProductSkuException(updated.sku()))
                                    : productRepository.save(updated));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Void> deleteProduct(java.util.UUID productId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> productRepository.findById(context.tenantId(), productId)
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
                        .then(productRepository.deleteById(context.tenantId(), productId)))
                .as(transactionalExecutor::transactional);
    }

    private BusinessEvent productCreatedEvent(Product product) {
        return BusinessEvent.now(product.tenantId(), product.organizationId(), "PRODUCT_CREATED", "PRODUCT",
                product.id(), payload(
                        "sku", product.sku(),
                        "name", product.name(),
                        "familyCode", product.familyCode(),
                        "variantLabel", product.variantLabel(),
                        "barcode", product.barcode(),
                        "description", product.description(),
                        "unitPrice", product.unitPrice(),
                        "currency", product.currency(),
                        "status", product.status()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
