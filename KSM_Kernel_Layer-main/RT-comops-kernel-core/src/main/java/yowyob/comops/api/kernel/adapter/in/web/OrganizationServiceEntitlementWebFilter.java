package yowyob.comops.api.kernel.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.port.out.OrganizationServiceEntitlementDirectory;
import yowyob.comops.api.kernel.config.ApiKeyAuthenticationToken;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class OrganizationServiceEntitlementWebFilter implements WebFilter {

    private static final List<RouteServiceMapping> SERVICE_MAPPINGS = List.of(
            new RouteServiceMapping("/api/clients", "COMMERCIAL"),
            new RouteServiceMapping("/api/customers", "COMMERCIAL"),
            new RouteServiceMapping("/api/suppliers", "COMMERCIAL"),
            new RouteServiceMapping("/api/prospects", "COMMERCIAL"),
            new RouteServiceMapping("/api/sales-agents", "COMMERCIAL"),
            new RouteServiceMapping("/api/third-parties", "COMMERCIAL"),
            new RouteServiceMapping("/api/products", "PRODUCT"),
            new RouteServiceMapping("/api/inventory", "INVENTORY"),
            new RouteServiceMapping("/api/inventories", "INVENTORY"),
            new RouteServiceMapping("/api/sales", "SALES"),
            new RouteServiceMapping("/api/accounting", "ACCOUNTING"),
            new RouteServiceMapping("/api/treasury", "TREASURY"),
            new RouteServiceMapping("/api/banking", "TREASURY"),
            new RouteServiceMapping("/api/resources", "RESOURCE"));

    private final OrganizationServiceEntitlementDirectory organizationServiceEntitlementDirectory;
    private final ObjectMapper objectMapper;

    public OrganizationServiceEntitlementWebFilter(
            OrganizationServiceEntitlementDirectory organizationServiceEntitlementDirectory,
            ObjectMapper objectMapper) {
        this.organizationServiceEntitlementDirectory = organizationServiceEntitlementDirectory;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String serviceCode = resolveServiceCode(exchange.getRequest().getPath().pathWithinApplication().value());
        if (serviceCode == null) {
            return chain.filter(exchange);
        }
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(securityContext -> securityContext.getAuthentication())
                .mapNotNull(authentication -> authentication instanceof ApiKeyAuthenticationToken token ? token : null)
                .flatMap(token -> enforce(exchange, chain, token, serviceCode).thenReturn(Boolean.TRUE))
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange).thenReturn(Boolean.TRUE)))
                .then();
    }

    private Mono<Void> enforce(ServerWebExchange exchange, WebFilterChain chain, ApiKeyAuthenticationToken token,
            String serviceCode) {
        if (token.tenantId() == null) {
            return writeFailure(exchange, HttpStatus.BAD_REQUEST, "X-Tenant-Id is required for service-scoped endpoints.",
                    "TENANT_CONTEXT_REQUIRED");
        }
        if (token.organizationId() == null) {
            return writeFailure(exchange, HttpStatus.BAD_REQUEST,
                    "X-Organization-Id is required for organization-scoped business service endpoints.",
                    "ORGANIZATION_CONTEXT_REQUIRED");
        }
        return organizationServiceEntitlementDirectory.hasEffectiveService(token.tenantId(), token.organizationId(),
                        serviceCode)
                .defaultIfEmpty(Boolean.TRUE)
                .flatMap(enabled -> enabled
                        ? chain.filter(exchange)
                        : writeFailure(exchange, HttpStatus.FORBIDDEN,
                                "Organization is not subscribed to service " + serviceCode + ".",
                                "ORGANIZATION_SERVICE_NOT_SUBSCRIBED"));
    }

    private String resolveServiceCode(String path) {
        return SERVICE_MAPPINGS.stream()
                .filter(mapping -> mapping.matches(path))
                .map(RouteServiceMapping::serviceCode)
                .findFirst()
                .orElse(null);
    }

    private Mono<Void> writeFailure(ServerWebExchange exchange, HttpStatus status, String message, String errorCode) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(ApiResponse.failure(message, errorCode));
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            payload = ("{\"success\":false,\"message\":\"" + message.replace("\"", "'")
                    + "\",\"errorCode\":\"" + errorCode + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }

    private record RouteServiceMapping(String prefix, String serviceCode) {
        private boolean matches(String path) {
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
    }
}
