package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "organization", name = "organization")
public record OrganizationEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID businessActorId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String code,
        String legalName,
        String displayName,
        String organizationType) implements PersistableEntity {
}
