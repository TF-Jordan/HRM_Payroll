package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "tp", name = "third_party")
public record ThirdPartyEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String partyType,
        UUID partyId,
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
        Instant convertedAt) implements PersistableEntity {
}
