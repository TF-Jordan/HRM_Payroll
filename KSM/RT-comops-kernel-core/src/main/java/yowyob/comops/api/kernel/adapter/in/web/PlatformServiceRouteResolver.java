package yowyob.comops.api.kernel.adapter.in.web;

import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import java.util.List;

public final class PlatformServiceRouteResolver {

    private static final List<RouteServiceMapping> CLIENT_APPLICATION_MAPPINGS = List.of(
            new RouteServiceMapping("/api/organizations", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/agencies", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/employees", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/warehouses", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/pois", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/organizations/opening-hours", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/organizations/points-of-interest", PlatformServiceCode.ORGANIZATION.code()),
            new RouteServiceMapping("/api/general-options", PlatformServiceCode.SETTINGS.code()),
            new RouteServiceMapping("/api/generalOptions", PlatformServiceCode.SETTINGS.code()),
            new RouteServiceMapping("/api/settings/document-sequences", PlatformServiceCode.SETTINGS.code()),
            new RouteServiceMapping("/api/clients", PlatformServiceCode.COMMERCIAL.code()),
            new RouteServiceMapping("/api/customers", PlatformServiceCode.COMMERCIAL.code()),
            new RouteServiceMapping("/api/suppliers", PlatformServiceCode.COMMERCIAL.code()),
            new RouteServiceMapping("/api/prospects", PlatformServiceCode.COMMERCIAL.code()),
            new RouteServiceMapping("/api/sales-agents", PlatformServiceCode.COMMERCIAL.code()),
            new RouteServiceMapping("/api/third-parties", PlatformServiceCode.COMMERCIAL.code()),
            new RouteServiceMapping("/api/products", PlatformServiceCode.PRODUCT.code()),
            new RouteServiceMapping("/api/inventory", PlatformServiceCode.INVENTORY.code()),
            new RouteServiceMapping("/api/inventories", PlatformServiceCode.INVENTORY.code()),
            new RouteServiceMapping("/api/sales", PlatformServiceCode.SALES.code()),
            new RouteServiceMapping("/api/accounting", PlatformServiceCode.ACCOUNTING.code()),
            new RouteServiceMapping("/api/treasury", PlatformServiceCode.TREASURY.code()),
            new RouteServiceMapping("/api/banking", PlatformServiceCode.TREASURY.code()),
            new RouteServiceMapping("/api/resources", PlatformServiceCode.RESOURCE.code()));

    private static final List<RouteServiceMapping> ORGANIZATION_ENTITLEMENT_MAPPINGS = CLIENT_APPLICATION_MAPPINGS.stream()
            .filter(mapping -> !PlatformServiceCode.ORGANIZATION.code().equals(mapping.serviceCode())
                    && !PlatformServiceCode.SETTINGS.code().equals(mapping.serviceCode()))
            .toList();

    String resolveClientApplicationServiceCode(String path) {
        return resolve(path, CLIENT_APPLICATION_MAPPINGS);
    }

    String resolveOrganizationEntitlementServiceCode(String path) {
        return resolve(path, ORGANIZATION_ENTITLEMENT_MAPPINGS);
    }

    private String resolve(String path, List<RouteServiceMapping> mappings) {
        return mappings.stream()
                .filter(mapping -> mapping.matches(path))
                .map(RouteServiceMapping::serviceCode)
                .findFirst()
                .orElse(null);
    }

    private record RouteServiceMapping(String prefix, String serviceCode) {
        private boolean matches(String path) {
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
    }
}
