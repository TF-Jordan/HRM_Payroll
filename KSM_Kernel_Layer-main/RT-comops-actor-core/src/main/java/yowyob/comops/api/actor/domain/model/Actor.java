package yowyob.comops.api.actor.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public final class Actor extends BaseEntity {

    private final String firstName;
    private final String lastName;
    private final String displayName;
    private final String phoneNumber;
    private final String email;
    private final String gender;
    private final String nationality;
    private final LocalDate birthDate;
    private final String profession;
    private final String biography;
    private final Instant deletedAt;

    private Actor(
            UUID id,
            UUID tenantId,
            Instant createdAt,
            Instant updatedAt,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String gender,
            String nationality,
            LocalDate birthDate,
            String profession,
            String biography,
            Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.displayName = this.firstName + " " + this.lastName;
        this.phoneNumber = normalize(phoneNumber);
        this.email = normalizeEmail(email);
        this.gender = normalize(gender);
        this.nationality = normalize(nationality);
        this.birthDate = birthDate;
        this.profession = normalize(profession);
        this.biography = normalize(biography);
        this.deletedAt = deletedAt;
    }

    public static Actor create(
            UUID tenantId,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String gender,
            String nationality,
            LocalDate birthDate,
            String profession,
            String biography) {
        Instant now = Instant.now();
        return new Actor(
                UUID.randomUUID(),
                tenantId,
                now,
                now,
                firstName,
                lastName,
                phoneNumber,
                email,
                gender,
                nationality,
                birthDate,
                profession,
                biography,
                null);
    }

    public static Actor rehydrate(
            UUID id,
            UUID tenantId,
            Instant createdAt,
            Instant updatedAt,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String gender,
            String nationality,
            LocalDate birthDate,
            String profession,
            String biography,
            Instant deletedAt) {
        return new Actor(id, tenantId, createdAt, updatedAt, firstName, lastName, phoneNumber, email, gender,
                nationality, birthDate, profession, biography, deletedAt);
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String displayName() {
        return displayName;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public String email() {
        return email;
    }

    public String gender() {
        return gender;
    }

    public String nationality() {
        return nationality;
    }

    public LocalDate birthDate() {
        return birthDate;
    }

    public String profession() {
        return profession;
    }

    public String biography() {
        return biography;
    }

    public Instant deletedAt() {
        return deletedAt;
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

    private static String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
