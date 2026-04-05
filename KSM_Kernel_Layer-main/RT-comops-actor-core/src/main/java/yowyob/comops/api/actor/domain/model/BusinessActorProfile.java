package yowyob.comops.api.actor.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class BusinessActorProfile extends BaseEntity {

    private final UUID actorId;
    private final BusinessActorGovernanceStatus governanceStatus;
    private final UUID governedByUserId;
    private final Instant governedAt;
    private final String governanceReason;
    private final String name;
    private final String businessId;
    private final String niu;
    private final String tradeRegistryNumber;
    private final String website;
    private final String contactPhone;
    private final String privateAddress;
    private final String businessAddress;
    private final String businessProfile;

    private BusinessActorProfile(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID actorId,
            BusinessActorGovernanceStatus governanceStatus, UUID governedByUserId, Instant governedAt,
            String governanceReason, String name,
            String businessId, String niu, String tradeRegistryNumber, String website, String contactPhone,
            String privateAddress, String businessAddress, String businessProfile) {
        super(id, tenantId, createdAt, updatedAt);
        this.actorId = requireUuid(actorId, "actorId");
        this.governanceStatus = governanceStatus == null ? BusinessActorGovernanceStatus.PENDING_REVIEW : governanceStatus;
        this.governedByUserId = governedByUserId;
        this.governedAt = governedAt;
        this.governanceReason = normalize(governanceReason);
        this.name = requireText(name, "name");
        this.businessId = normalize(businessId);
        this.niu = normalize(niu);
        this.tradeRegistryNumber = normalize(tradeRegistryNumber);
        this.website = normalize(website);
        this.contactPhone = normalize(contactPhone);
        this.privateAddress = normalize(privateAddress);
        this.businessAddress = normalize(businessAddress);
        this.businessProfile = normalize(businessProfile);
    }

    public static BusinessActorProfile create(UUID tenantId, UUID actorId, String name, String businessId, String niu,
            String tradeRegistryNumber, String website, String contactPhone, String privateAddress,
            String businessAddress, String businessProfile) {
        Instant now = Instant.now();
        return new BusinessActorProfile(UUID.randomUUID(), tenantId, now, now, actorId,
                BusinessActorGovernanceStatus.PENDING_REVIEW, null, null, null, name, businessId, niu,
                tradeRegistryNumber, website, contactPhone, privateAddress, businessAddress, businessProfile);
    }

    public static BusinessActorProfile rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID actorId,
            String governanceStatus, UUID governedByUserId, Instant governedAt, String governanceReason, String name,
            String businessId, String niu, String tradeRegistryNumber, String website, String contactPhone,
            String privateAddress, String businessAddress, String businessProfile) {
        return new BusinessActorProfile(id, tenantId, createdAt, updatedAt, actorId,
                BusinessActorGovernanceStatus.from(governanceStatus), governedByUserId, governedAt, governanceReason, name, businessId, niu,
                tradeRegistryNumber, website, contactPhone, privateAddress, businessAddress, businessProfile);
    }

    public BusinessActorProfile update(String name, String businessId, String niu, String tradeRegistryNumber,
            String website, String contactPhone, String privateAddress, String businessAddress, String businessProfile) {
        return new BusinessActorProfile(id(), tenantId(), createdAt(), Instant.now(), actorId,
                governanceStatus, governedByUserId, governedAt, governanceReason, name, businessId, niu,
                tradeRegistryNumber, website, contactPhone, privateAddress, businessAddress, businessProfile);
    }

    public BusinessActorProfile approve(UUID adminUserId, String reason) {
        return govern(BusinessActorGovernanceStatus.APPROVED, adminUserId, reason);
    }

    public BusinessActorProfile reject(UUID adminUserId, String reason) {
        return govern(BusinessActorGovernanceStatus.REJECTED, adminUserId, reason);
    }

    public BusinessActorProfile suspend(UUID adminUserId, String reason) {
        if (governanceStatus == BusinessActorGovernanceStatus.BLOCKED) {
            throw new IllegalStateException("blocked business actor cannot be suspended");
        }
        return govern(BusinessActorGovernanceStatus.SUSPENDED, adminUserId, reason);
    }

    public BusinessActorProfile block(UUID adminUserId, String reason) {
        return govern(BusinessActorGovernanceStatus.BLOCKED, adminUserId, reason);
    }

    public BusinessActorProfile reactivate(UUID adminUserId, String reason) {
        return govern(BusinessActorGovernanceStatus.APPROVED, adminUserId, reason);
    }

    private BusinessActorProfile govern(BusinessActorGovernanceStatus status, UUID adminUserId, String reason) {
        return new BusinessActorProfile(id(), tenantId(), createdAt(), Instant.now(), actorId,
                status, adminUserId, Instant.now(), reason, name, businessId, niu, tradeRegistryNumber, website,
                contactPhone, privateAddress, businessAddress, businessProfile);
    }

    public UUID actorId() { return actorId; }
    public BusinessActorGovernanceStatus governanceStatus() { return governanceStatus; }
    public UUID governedByUserId() { return governedByUserId; }
    public Instant governedAt() { return governedAt; }
    public String governanceReason() { return governanceReason; }
    public String name() { return name; }
    public String businessId() { return businessId; }
    public String niu() { return niu; }
    public String tradeRegistryNumber() { return tradeRegistryNumber; }
    public String website() { return website; }
    public String contactPhone() { return contactPhone; }
    public String privateAddress() { return privateAddress; }
    public String businessAddress() { return businessAddress; }
    public String businessProfile() { return businessProfile; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
