package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateTrainingRequest(
        @NotBlank String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxParticipants) {
}
