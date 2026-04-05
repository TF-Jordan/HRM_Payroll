package yowyob.comops.api.actor.adapter.in.web;

import yowyob.comops.api.actor.domain.model.Actor;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ActorResponse(
        UUID id,
        UUID tenantId,
        String firstName,
        String lastName,
        String displayName,
        String phoneNumber,
        String email,
        String gender,
        String nationality,
        LocalDate birthDate,
        String profession,
        String biography,
        Instant createdAt) {

    public static ActorResponse from(Actor actor) {
        return new ActorResponse(
                actor.id(),
                actor.tenantId(),
                actor.firstName(),
                actor.lastName(),
                actor.displayName(),
                actor.phoneNumber(),
                actor.email(),
                actor.gender(),
                actor.nationality(),
                actor.birthDate(),
                actor.profession(),
                actor.biography(),
                actor.createdAt());
    }
}
