package yowyob.comops.api.actor.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateActorRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phoneNumber,
        @Email String email,
        String gender,
        String nationality,
        LocalDate birthDate,
        String profession,
        String biography) {
}
