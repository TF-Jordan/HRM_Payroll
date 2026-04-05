package yowyob.comops.api.common.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Address(
        UUID id,
        UUID tenantId,
        AddressableType addressableType,
        UUID addressableId,
        AddressType addressType,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String locality,
        String postalCode,
        Double latitude,
        Double longitude,
        boolean isDefault) {

    public Address {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(addressableType, "addressableType is required");
        Objects.requireNonNull(addressableId, "addressableId is required");
        Objects.requireNonNull(addressType, "addressType is required");
        if (addressLine1 == null || addressLine1.isBlank()) {
            throw new IllegalArgumentException("addressLine1 is required");
        }
        validateLatitude(latitude);
        validateLongitude(longitude);
    }

    private static void validateLatitude(Double latitude) {
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
    }

    private static void validateLongitude(Double longitude) {
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}
