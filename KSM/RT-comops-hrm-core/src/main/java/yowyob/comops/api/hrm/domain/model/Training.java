package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Training extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("PLANNED", "IN_PROGRESS", "COMPLETED", "CANCELLED");

    private final UUID organizationId;
    private final String title;
    private final String description;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer maxParticipants;
    private final String status;

    private Training(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, String title, String description, LocalDate startDate,
                     LocalDate endDate, Integer maxParticipants, String status) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.title = requireText(title, "title");
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
        this.status = normalizeStatus(status);
    }

    public static Training create(UUID tenantId, UUID organizationId, String title, String description,
                                  LocalDate startDate, LocalDate endDate, Integer maxParticipants) {
        Instant now = Instant.now();
        return new Training(UUID.randomUUID(), tenantId, now, now, organizationId, title, description,
                startDate, endDate, maxParticipants, "PLANNED");
    }

    public static Training rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, String title, String description,
                                     LocalDate startDate, LocalDate endDate, Integer maxParticipants,
                                     String status) {
        return new Training(id, tenantId, createdAt, updatedAt, organizationId, title, description,
                startDate, endDate, maxParticipants, status);
    }

    public Training start() {
        if (!"PLANNED".equals(status)) {
            throw new IllegalArgumentException("Training must be PLANNED to start, current: " + status);
        }
        return new Training(id(), tenantId(), createdAt(), Instant.now(), organizationId, title,
                description, startDate, endDate, maxParticipants, "IN_PROGRESS");
    }

    public Training complete() {
        if (!"IN_PROGRESS".equals(status)) {
            throw new IllegalArgumentException("Training must be IN_PROGRESS to complete, current: " + status);
        }
        return new Training(id(), tenantId(), createdAt(), Instant.now(), organizationId, title,
                description, startDate, endDate, maxParticipants, "COMPLETED");
    }

    public Training cancel() {
        if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            throw new IllegalArgumentException("Training cannot be cancelled, current: " + status);
        }
        return new Training(id(), tenantId(), createdAt(), Instant.now(), organizationId, title,
                description, startDate, endDate, maxParticipants, "CANCELLED");
    }

    public UUID organizationId() { return organizationId; }
    public String title() { return title; }
    public String description() { return description; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }
    public Integer maxParticipants() { return maxParticipants; }
    public String status() { return status; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }
}
