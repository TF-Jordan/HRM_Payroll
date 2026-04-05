package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class OrganizationServiceSubscription extends BaseEntity {

    private final UUID organizationId;
    private final String serviceCode;

    private OrganizationServiceSubscription(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String serviceCode) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.serviceCode = PlatformServiceCode.from(serviceCode).code();
    }

    public static OrganizationServiceSubscription create(UUID tenantId, UUID organizationId, String serviceCode) {
        Instant now = Instant.now();
        return new OrganizationServiceSubscription(UUID.randomUUID(), tenantId, now, now, organizationId, serviceCode);
    }

    public static OrganizationServiceSubscription rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String serviceCode) {
        return new OrganizationServiceSubscription(id, tenantId, createdAt, updatedAt, organizationId, serviceCode);
    }

    public UUID organizationId() {
        return organizationId;
    }

    public String serviceCode() {
        return serviceCode;
    }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
