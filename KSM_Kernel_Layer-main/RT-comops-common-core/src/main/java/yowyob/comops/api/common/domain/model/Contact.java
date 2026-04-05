package yowyob.comops.api.common.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Contact(
        UUID id,
        UUID tenantId,
        ContactableType contactableType,
        UUID contactableId,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        boolean isEmailVerified,
        boolean isPhoneNumberVerified,
        boolean isFavorite) {

    public Contact {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(contactableType, "contactableType is required");
        Objects.requireNonNull(contactableId, "contactableId is required");
    }
}
