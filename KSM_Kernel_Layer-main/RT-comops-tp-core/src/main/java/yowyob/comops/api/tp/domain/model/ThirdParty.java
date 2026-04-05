package yowyob.comops.api.tp.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.common.domain.model.PartyRef;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ThirdParty extends BaseEntity {

    private final UUID organizationId;
    private final PartyRef partyRef;
    private final String referenceCode;
    private final String displayName;
    private final Set<String> roles;
    private final boolean prospect;
    private final String accountingAccount;
    private final String segment;
    private final Integer qualificationScore;
    private final Instant lastContactedAt;
    private final Instant nextFollowUpAt;
    private final String followUpStatus;
    private final boolean active;
    private final Instant convertedAt;

    private ThirdParty(
            UUID id,
            UUID tenantId,
            Instant createdAt,
            Instant updatedAt,
            UUID organizationId,
            PartyRef partyRef,
            String referenceCode,
            String displayName,
            Set<String> roles,
            boolean prospect,
            String accountingAccount,
            String segment,
            Integer qualificationScore,
            Instant lastContactedAt,
            Instant nextFollowUpAt,
            String followUpStatus,
            boolean active,
            Instant convertedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.partyRef = Objects.requireNonNull(partyRef, "partyRef is required");
        this.referenceCode = requireText(referenceCode, "referenceCode").toUpperCase();
        this.displayName = requireText(displayName, "displayName");
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("roles are required");
        }
        this.roles = normalizeRoles(roles, prospect);
        this.prospect = prospect;
        this.accountingAccount = normalizeOptional(accountingAccount);
        this.segment = normalizeOptional(segment);
        this.qualificationScore = normalizeScore(qualificationScore);
        this.lastContactedAt = lastContactedAt;
        this.nextFollowUpAt = nextFollowUpAt;
        this.followUpStatus = normalizeFollowUpStatus(followUpStatus, nextFollowUpAt, lastContactedAt);
        this.active = active;
        this.convertedAt = convertedAt;
    }

    public static ThirdParty create(UUID tenantId, UUID organizationId, PartyRef partyRef, String referenceCode,
            String displayName, Set<String> roles, boolean prospect, String accountingAccount, String segment,
            Integer qualificationScore, boolean active) {
        Instant now = Instant.now();
        return new ThirdParty(UUID.randomUUID(), tenantId, now, now, organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, null, null, null,
                active, null);
    }

    public static ThirdParty rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, PartyRef partyRef, String referenceCode, String displayName, Set<String> roles,
            boolean prospect, String accountingAccount, String segment, Integer qualificationScore, boolean active,
            Instant lastContactedAt, Instant nextFollowUpAt, String followUpStatus, Instant convertedAt) {
        return new ThirdParty(id, tenantId, createdAt, updatedAt, organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt);
    }

    public UUID organizationId() { return organizationId; }
    public PartyRef partyRef() { return partyRef; }
    public String referenceCode() { return referenceCode; }
    public String displayName() { return displayName; }
    public Set<String> roles() { return roles; }
    public boolean prospect() { return prospect; }
    public String accountingAccount() { return accountingAccount; }
    public String segment() { return segment; }
    public Integer qualificationScore() { return qualificationScore; }
    public Instant lastContactedAt() { return lastContactedAt; }
    public Instant nextFollowUpAt() { return nextFollowUpAt; }
    public String followUpStatus() { return followUpStatus; }
    public boolean active() { return active; }
    public Instant convertedAt() { return convertedAt; }
    public boolean hasRole(String role) { return roles.contains(requireText(role, "role").toUpperCase()); }

    public ThirdParty update(String referenceCode, String displayName, Set<String> roles, boolean prospect,
            String accountingAccount, String segment, Integer qualificationScore, boolean active) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt);
    }

    public ThirdParty defineAccountingAccount(String accountingAccount) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt);
    }

    public ThirdParty qualify(String segment, Integer qualificationScore) {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, active, convertedAt);
    }

    public ThirdParty scheduleFollowUp(Instant nextFollowUpAt) {
        if (nextFollowUpAt == null) {
            throw new IllegalArgumentException("nextFollowUpAt is required");
        }
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, "SCHEDULED", active, convertedAt);
    }

    public ThirdParty completeFollowUp(Instant contactedAt, Instant nextFollowUpAt) {
        Instant effectiveContactedAt = contactedAt == null ? Instant.now() : contactedAt;
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, effectiveContactedAt,
                nextFollowUpAt, nextFollowUpAt == null ? "COMPLETED" : "SCHEDULED", active, convertedAt);
    }

    public ThirdParty activate() {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, true, convertedAt);
    }

    public ThirdParty deactivate() {
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, roles, prospect, accountingAccount, segment, qualificationScore, lastContactedAt,
                nextFollowUpAt, followUpStatus, false, convertedAt);
    }

    public ThirdParty convertToCustomer() {
        LinkedHashSet<String> convertedRoles = new LinkedHashSet<>(roles);
        convertedRoles.remove("PROSPECT");
        convertedRoles.add("CUSTOMER");
        return new ThirdParty(id(), tenantId(), createdAt(), Instant.now(), organizationId, partyRef, referenceCode,
                displayName, Set.copyOf(convertedRoles), false, accountingAccount, segment, qualificationScore,
                lastContactedAt, nextFollowUpAt, followUpStatus, active, convertedAt == null ? Instant.now() : convertedAt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private static Integer normalizeScore(Integer value) {
        if (value == null) {
            return null;
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("qualificationScore must be between 0 and 100");
        }
        return value;
    }

    private static String normalizeFollowUpStatus(String value, Instant nextFollowUpAt, Instant lastContactedAt) {
        if (value == null || value.isBlank()) {
            if (nextFollowUpAt != null) {
                return "SCHEDULED";
            }
            if (lastContactedAt != null) {
                return "COMPLETED";
            }
            return null;
        }
        return value.trim().toUpperCase();
    }

    private static Set<String> normalizeRoles(Set<String> roles, boolean prospect) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        roles.stream()
                .map(role -> requireText(role, "role").toUpperCase())
                .forEach(normalized::add);
        if (prospect) {
            normalized.add("PROSPECT");
        }
        return Set.copyOf(normalized);
    }
}
