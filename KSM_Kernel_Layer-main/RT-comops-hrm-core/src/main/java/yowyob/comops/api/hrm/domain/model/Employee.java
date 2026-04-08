package yowyob.comops.api.hrm.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.hrm.domain.exception.InvalidEmployeeStateException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Employee extends BaseEntity {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "ON_LEAVE", "SUSPENDED", "TERMINATED");

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID actorId;
    private final String registrationNumber;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final String gender;
    private final LocalDate birthDate;
    private final LocalDate hireDate;
    private final LocalDate terminationDate;
    private final String department;
    private final String jobTitle;
    private final String status;
    private final String cnpsNumber;

    private Employee(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                     UUID organizationId, UUID agencyId, UUID actorId, String registrationNumber,
                     String firstName, String lastName, String email, String phoneNumber,
                     String gender, LocalDate birthDate, LocalDate hireDate, LocalDate terminationDate,
                     String department, String jobTitle, String status, String cnpsNumber) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.agencyId = agencyId;
        this.actorId = Objects.requireNonNull(actorId, "actorId is required");
        this.registrationNumber = requireText(registrationNumber, "registrationNumber");
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthDate = birthDate;
        this.hireDate = Objects.requireNonNull(hireDate, "hireDate is required");
        this.terminationDate = terminationDate;
        this.department = department;
        this.jobTitle = jobTitle;
        this.status = normalizeStatus(status);
        this.cnpsNumber = cnpsNumber;
    }

    public static Employee create(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId,
                                  String registrationNumber, String firstName, String lastName,
                                  String email, String phoneNumber, String gender, LocalDate birthDate,
                                  LocalDate hireDate, String department, String jobTitle, String cnpsNumber) {
        Instant now = Instant.now();
        return new Employee(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, actorId,
                registrationNumber, firstName, lastName, email, phoneNumber, gender, birthDate, hireDate,
                null, department, jobTitle, "ACTIVE", cnpsNumber);
    }

    public static Employee rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                     UUID organizationId, UUID agencyId, UUID actorId,
                                     String registrationNumber, String firstName, String lastName,
                                     String email, String phoneNumber, String gender, LocalDate birthDate,
                                     LocalDate hireDate, LocalDate terminationDate, String department,
                                     String jobTitle, String status, String cnpsNumber) {
        return new Employee(id, tenantId, createdAt, updatedAt, organizationId, agencyId, actorId,
                registrationNumber, firstName, lastName, email, phoneNumber, gender, birthDate, hireDate,
                terminationDate, department, jobTitle, status, cnpsNumber);
    }

    public Employee update(String firstName, String lastName, String email, String phoneNumber,
                           String gender, LocalDate birthDate, String department, String jobTitle,
                           UUID agencyId, String cnpsNumber) {
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, registrationNumber, firstName, lastName, email, phoneNumber, gender, birthDate,
                hireDate, terminationDate, department, jobTitle, status, cnpsNumber);
    }

    public Employee terminate(LocalDate terminationDate) {
        if (!"ACTIVE".equals(status) && !"ON_LEAVE".equals(status) && !"SUSPENDED".equals(status)) {
            throw new InvalidEmployeeStateException(id(), status, "ACTIVE, ON_LEAVE or SUSPENDED");
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, registrationNumber, firstName, lastName, email, phoneNumber, gender, birthDate,
                hireDate, terminationDate, department, jobTitle, "TERMINATED", cnpsNumber);
    }

    public Employee suspend() {
        if (!"ACTIVE".equals(status)) {
            throw new InvalidEmployeeStateException(id(), status, "ACTIVE");
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, registrationNumber, firstName, lastName, email, phoneNumber, gender, birthDate,
                hireDate, terminationDate, department, jobTitle, "SUSPENDED", cnpsNumber);
    }

    public Employee reactivate() {
        if (!"SUSPENDED".equals(status) && !"ON_LEAVE".equals(status)) {
            throw new InvalidEmployeeStateException(id(), status, "SUSPENDED or ON_LEAVE");
        }
        return new Employee(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                actorId, registrationNumber, firstName, lastName, email, phoneNumber, gender, birthDate,
                hireDate, terminationDate, department, jobTitle, "ACTIVE", cnpsNumber);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID actorId() { return actorId; }
    public String registrationNumber() { return registrationNumber; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String email() { return email; }
    public String phoneNumber() { return phoneNumber; }
    public String gender() { return gender; }
    public LocalDate birthDate() { return birthDate; }
    public LocalDate hireDate() { return hireDate; }
    public LocalDate terminationDate() { return terminationDate; }
    public String department() { return department; }
    public String jobTitle() { return jobTitle; }
    public String status() { return status; }
    public String cnpsNumber() { return cnpsNumber; }

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
