package yowyob.comops.api.hrm.adapter.in.web;

import yowyob.comops.api.hrm.domain.model.Training;

import java.time.LocalDate;
import java.util.UUID;

public record TrainingResponse(
        UUID id,
        UUID organizationId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxParticipants,
        String status) {

    public static TrainingResponse from(Training training) {
        return new TrainingResponse(training.id(), training.organizationId(), training.title(),
                training.description(), training.startDate(), training.endDate(),
                training.maxParticipants(), training.status());
    }
}
