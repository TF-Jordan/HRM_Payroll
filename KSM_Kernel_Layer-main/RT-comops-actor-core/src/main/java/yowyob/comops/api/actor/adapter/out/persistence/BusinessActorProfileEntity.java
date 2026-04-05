package yowyob.comops.api.actor.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "actor", name = "business_actor_profile")
public record BusinessActorProfileEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID actorId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String name,
        String businessId,
        String niu,
        String tradeRegistryNumber,
        String website,
        String contactPhone,
        String privateAddress,
        String businessAddress,
        String businessProfile) implements PersistableEntity {
}
