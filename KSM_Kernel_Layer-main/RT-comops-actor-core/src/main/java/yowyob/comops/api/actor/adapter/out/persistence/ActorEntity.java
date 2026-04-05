package yowyob.comops.api.actor.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "actor", name = "actor")
public record ActorEntity(
        @Id UUID id,
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
        Instant deletedAt) implements PersistableEntity {
}
