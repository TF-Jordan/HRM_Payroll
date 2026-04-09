package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Dependent extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final String firstName;
    private final String lastName;
    private final String relationship;
    private final LocalDate birthDate;
    private final String gender;

    private Dependent(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                      UUID organizationId, UUID employeeId, String firstName, String lastName,
                      String relationship, LocalDate birthDate, String gender) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.relationship = requireText(relationship, "relationship");
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static Dependent create(UUID tenantId, UUID organizationId, UUID employeeId,
                                   String firstName, String lastName, String relationship,
                                   LocalDate birthDate, String gender) {
        Instant now = Instant.now();
        return new Dependent(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId,
                firstName, lastName, relationship, birthDate, gender);
    }

    public static Dependent rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                      UUID organizationId, UUID employeeId, String firstName, String lastName,
                                      String relationship, LocalDate birthDate, String gender) {
        return new Dependent(id, tenantId, createdAt, updatedAt, organizationId, employeeId,
                firstName, lastName, relationship, birthDate, gender);
    }

    public Dependent update(String firstName, String lastName, String relationship,
                            LocalDate birthDate, String gender) {
        return new Dependent(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                firstName, lastName, relationship, birthDate, gender);
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String relationship() { return relationship; }
    public LocalDate birthDate() { return birthDate; }
    public String gender() { return gender; }

    public boolean isChild() {
        return "CHILD".equalsIgnoreCase(relationship);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
