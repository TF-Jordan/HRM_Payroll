package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Organization extends BaseEntity {

    private final UUID businessActorId;
    private final OrganizationGovernanceStatus governanceStatus;
    private final UUID governedByUserId;
    private final Instant governedAt;
    private final String governanceReason;
    private final String code;
    private final String legalName;
    private final String displayName;
    private final String organizationType;

    private Organization(
            UUID id,
            UUID tenantId,
            Instant createdAt,
            Instant updatedAt,
            UUID businessActorId,
            OrganizationGovernanceStatus governanceStatus,
            UUID governedByUserId,
            Instant governedAt,
            String governanceReason,
            String code,
            String legalName,
            String displayName,
            String organizationType) {
        super(id, tenantId, createdAt, updatedAt);
        this.businessActorId = Objects.requireNonNull(businessActorId, "businessActorId is required");
        this.governanceStatus = governanceStatus == null ? OrganizationGovernanceStatus.PENDING_APPROVAL : governanceStatus;
        this.governedByUserId = governedByUserId;
        this.governedAt = governedAt;
        this.governanceReason = normalizeOptional(governanceReason);
        this.code = normalizeCode(code);
        this.legalName = requireText(legalName, "legalName");
        this.displayName = requireText(displayName, "displayName");
        this.organizationType = normalizeType(organizationType);
    }

    public static Organization create(UUID tenantId, UUID businessActorId, String code, String legalName, String displayName,
            String organizationType) {
        Instant now = Instant.now();
        return new Organization(UUID.randomUUID(), tenantId, now, now, businessActorId,
                OrganizationGovernanceStatus.PENDING_APPROVAL, null, null, null, code, legalName, displayName,
                organizationType);
    }

    public static Organization rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID businessActorId, String governanceStatus, UUID governedByUserId, Instant governedAt,
            String governanceReason, String code, String legalName, String displayName, String organizationType) {
        return new Organization(id, tenantId, createdAt, updatedAt, businessActorId,
                OrganizationGovernanceStatus.from(governanceStatus), governedByUserId, governedAt, governanceReason,
                code, legalName, displayName, organizationType);
    }

    public Organization update(String code, String legalName, String displayName, String organizationType) {
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), businessActorId,
                governanceStatus, governedByUserId, governedAt, governanceReason, code, legalName,
                displayName, organizationType);
    }

    public Organization transferOwnership(UUID newBusinessActorId) {
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), newBusinessActorId,
                governanceStatus, governedByUserId, governedAt, governanceReason, code, legalName,
                displayName, organizationType);
    }

    public Organization approve(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.APPROVED, adminUserId, reason);
    }

    public Organization reject(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.REJECTED, adminUserId, reason);
    }

    public Organization suspend(UUID adminUserId, String reason) {
        if (governanceStatus == OrganizationGovernanceStatus.CLOSED) {
            throw new IllegalStateException("closed organization cannot be suspended");
        }
        return govern(OrganizationGovernanceStatus.SUSPENDED, adminUserId, reason);
    }

    public Organization close(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.CLOSED, adminUserId, reason);
    }

    public Organization reopen(UUID adminUserId, String reason) {
        return govern(OrganizationGovernanceStatus.APPROVED, adminUserId, reason);
    }

    private Organization govern(OrganizationGovernanceStatus status, UUID adminUserId, String reason) {
        return new Organization(id(), tenantId(), createdAt(), Instant.now(), businessActorId,
                status, adminUserId, Instant.now(), reason, code, legalName, displayName, organizationType);
    }

    public UUID businessActorId() { return businessActorId; }
    public OrganizationGovernanceStatus governanceStatus() { return governanceStatus; }
    public UUID governedByUserId() { return governedByUserId; }
    public Instant governedAt() { return governedAt; }
    public String governanceReason() { return governanceReason; }
    public String code() { return code; }
    public String legalName() { return legalName; }
    public String displayName() { return displayName; }
    public String organizationType() { return organizationType; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeCode(String value) {
        return requireText(value, "code").toUpperCase();
    }

    private static String normalizeType(String value) {
        return requireText(value, "organizationType").toUpperCase();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
