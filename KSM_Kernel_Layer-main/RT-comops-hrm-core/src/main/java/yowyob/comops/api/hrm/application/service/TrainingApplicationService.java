package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.out.TrainingRepository;
import yowyob.comops.api.hrm.domain.model.Training;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TrainingApplicationService {

    private final TrainingRepository trainingRepository;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public TrainingApplicationService(TrainingRepository trainingRepository,
                                      ReactiveTransactionalExecutor transactionalExecutor) {
        this.trainingRepository = trainingRepository;
        this.transactionalExecutor = transactionalExecutor;
    }

    public Mono<Training> createTraining(UUID tenantId, UUID organizationId, String title,
                                         String description, LocalDate startDate, LocalDate endDate,
                                         Integer maxParticipants) {
        Training training = Training.create(tenantId, organizationId, title, description,
                startDate, endDate, maxParticipants);
        return transactionalExecutor.transactional(trainingRepository.save(training));
    }

    public Mono<Training> getTraining(UUID trainingId) {
        return trainingRepository.findById(trainingId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found: " + trainingId)));
    }

    public Flux<Training> listTrainings(UUID tenantId, UUID organizationId) {
        return trainingRepository.findByOrganizationId(tenantId, organizationId);
    }

    public Mono<Training> startTraining(UUID trainingId) {
        return transactionalExecutor.transactional(
                trainingRepository.findById(trainingId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found: " + trainingId)))
                        .map(Training::start)
                        .flatMap(trainingRepository::save));
    }

    public Mono<Training> completeTraining(UUID trainingId) {
        return transactionalExecutor.transactional(
                trainingRepository.findById(trainingId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Training not found: " + trainingId)))
                        .map(Training::complete)
                        .flatMap(trainingRepository::save));
    }
}
