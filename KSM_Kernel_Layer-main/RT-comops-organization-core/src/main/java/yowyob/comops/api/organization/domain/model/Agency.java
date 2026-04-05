package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Agency extends BaseEntity {

    private final UUID organizationId;
    private final AgencyGovernanceStatus governanceStatus;
    private final UUID governedByUserId;
    private final Instant governedAt;
    private final String governanceReason;
    private final String code;
    private final String name;
    private final String agencyType;
    private final boolean active;

    private Agency(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            AgencyGovernanceStatus governanceStatus, UUID governedByUserId, Instant governedAt, String governanceReason,
            String code, String name, String agencyType, boolean active) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.governanceStatus = governanceStatus == null ? AgencyGovernanceStatus.ACTIVE : governanceStatus;
        this.governedByUserId = governedByUserId;
        this.governedAt = governedAt;
        this.governanceReason = normalizeOptional(governanceReason);
        this.code = normalizeCode(code);
        this.name = requireText(name, "name");
        this.agencyType = normalizeType(agencyType);
        this.active = active;
    }

    public static Agency create(UUID tenantId, UUID organizationId, String code, String name, String agencyType) {
        Instant now = Instant.now();
        return new Agency(UUID.randomUUID(), tenantId, now, now, organizationId,
                AgencyGovernanceStatus.ACTIVE, null, null, null, code, name, agencyType, true);
    }

    public static Agency rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String governanceStatus, UUID governedByUserId, Instant governedAt, String governanceReason,
            String code, String name, String agencyType, boolean active) {
        return new Agency(id, tenantId, createdAt, updatedAt, organizationId,
                AgencyGovernanceStatus.from(governanceStatus), governedByUserId, governedAt, governanceReason,
                code, name, agencyType, active);
    }

    public UUID organizationId() { return organizationId; }
    public AgencyGovernanceStatus governanceStatus() { return governanceStatus; }
    public UUID governedByUserId() { return governedByUserId; }
    public Instant governedAt() { return governedAt; }
    public String governanceReason() { return governanceReason; }
    public String code() { return code; }
    public String name() { return name; }
    public String agencyType() { return agencyType; }
    public boolean active() { return active; }

    public Agency update(String code, String name, String agencyType) {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                governanceStatus, governedByUserId, governedAt, governanceReason, code, name, agencyType, active);
    }

    public Agency deactivate() {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.SUSPENDED, governedByUserId, Instant.now(), "deactivated",
                code, name, agencyType, false);
    }

    public Agency activate(UUID adminUserId, String reason) {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.ACTIVE, adminUserId, Instant.now(), reason,
                code, name, agencyType, true);
    }

    public Agency suspend(UUID adminUserId, String reason) {
        if (governanceStatus == AgencyGovernanceStatus.CLOSED) {
            throw new IllegalStateException("closed agency cannot be suspended");
        }
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.SUSPENDED, adminUserId, Instant.now(), reason,
                code, name, agencyType, false);
    }

    public Agency close(UUID adminUserId, String reason) {
        return new Agency(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                AgencyGovernanceStatus.CLOSED, adminUserId, Instant.now(), reason,
                code, name, agencyType, false);
    }

    private static String normalizeCode(String value) {
        return requireText(value, "code").toUpperCase();
    }

    private static String normalizeType(String value) {
        return requireText(value, "agencyType").toUpperCase();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
