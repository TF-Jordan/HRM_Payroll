package yowyob.comops.api.actor.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateActorCommand(
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
}
