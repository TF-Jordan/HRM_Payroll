package yowyob.comops.api.kernel.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class ClientApplication {

    private final UUID id;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String clientId;
    private final String name;
    private final String description;
    private final String secretHash;
    private final ClientApplicationStatus status;
    private final boolean systemManaged;
    private final Instant lastAuthenticatedAt;
    private final Instant secretRotatedAt;

    private ClientApplication(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String clientId,
            String name,
            String description,
            String secretHash,
            ClientApplicationStatus status,
            boolean systemManaged,
            Instant lastAuthenticatedAt,
            Instant secretRotatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        this.clientId = normalizeClientId(clientId);
        this.name = requireText(name, "name");
        this.description = normalizeNullableText(description);
        this.secretHash = requireText(secretHash, "secretHash");
        this.status = Objects.requireNonNull(status, "status is required");
        this.systemManaged = systemManaged;
        this.lastAuthenticatedAt = lastAuthenticatedAt;
        this.secretRotatedAt = secretRotatedAt == null ? updatedAt : secretRotatedAt;
    }

    public static ClientApplication register(
            String clientId,
            String name,
            String description,
            String secretHash,
            boolean systemManaged) {
        Instant now = Instant.now();
        return new ClientApplication(
                UUID.randomUUID(),
                now,
                now,
                clientId,
                name,
                description,
                secretHash,
                ClientApplicationStatus.ACTIVE,
                systemManaged,
                null,
                now);
    }

    public static ClientApplication rehydrate(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String clientId,
            String name,
            String description,
            String secretHash,
            ClientApplicationStatus status,
            boolean systemManaged,
            Instant lastAuthenticatedAt,
            Instant secretRotatedAt) {
        return new ClientApplication(id, createdAt, updatedAt, clientId, name, description, secretHash, status,
                systemManaged, lastAuthenticatedAt, secretRotatedAt);
    }

    public UUID id() {
        return id;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String clientId() {
        return clientId;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String secretHash() {
        return secretHash;
    }

    public ClientApplicationStatus status() {
        return status;
    }

    public boolean systemManaged() {
        return systemManaged;
    }

    public Instant lastAuthenticatedAt() {
        return lastAuthenticatedAt;
    }

    public Instant secretRotatedAt() {
        return secretRotatedAt;
    }

    public boolean isActive() {
        return status == ClientApplicationStatus.ACTIVE;
    }

    public ClientApplication updateDefinition(String name, String description, boolean systemManaged) {
        return new ClientApplication(id, createdAt, Instant.now(), clientId, name, description, secretHash, status,
                systemManaged, lastAuthenticatedAt, secretRotatedAt);
    }

    public ClientApplication rotateSecret(String secretHash) {
        Instant now = Instant.now();
        return new ClientApplication(id, createdAt, now, clientId, name, description, secretHash,
                ClientApplicationStatus.ACTIVE, systemManaged, lastAuthenticatedAt, now);
    }

    public ClientApplication markAuthenticated() {
        Instant now = Instant.now();
        return new ClientApplication(id, createdAt, now, clientId, name, description, secretHash, status,
                systemManaged, now, secretRotatedAt);
    }

    public ClientApplication revoke() {
        return new ClientApplication(id, createdAt, Instant.now(), clientId, name, description, secretHash,
                ClientApplicationStatus.REVOKED, systemManaged, lastAuthenticatedAt, secretRotatedAt);
    }

    public ClientApplication activate() {
        return new ClientApplication(id, createdAt, Instant.now(), clientId, name, description, secretHash,
                ClientApplicationStatus.ACTIVE, systemManaged, lastAuthenticatedAt, secretRotatedAt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeClientId(String clientId) {
        return requireText(clientId, "clientId").toLowerCase(Locale.ROOT);
    }

    private static String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
