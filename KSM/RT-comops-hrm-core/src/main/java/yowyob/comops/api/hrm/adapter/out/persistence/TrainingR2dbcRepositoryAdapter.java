package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.TrainingRepository;
import yowyob.comops.api.hrm.domain.model.Training;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class TrainingR2dbcRepositoryAdapter implements TrainingRepository {

    private final TrainingSpringDataRepository repository;

    public TrainingR2dbcRepositoryAdapter(TrainingSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Training> findById(UUID trainingId) {
        return repository.findById(trainingId).map(this::toDomain);
    }

    @Override
    public Flux<Training> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<Training> save(Training t) {
        TrainingEntity entity = new TrainingEntity(t.id(), t.tenantId(), t.createdAt(), t.updatedAt(),
                t.organizationId(), t.title(), t.description(), t.startDate(), t.endDate(),
                t.maxParticipants(), t.status());
        return repository.save(entity).map(this::toDomain);
    }

    private Training toDomain(TrainingEntity e) {
        return Training.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.title(), e.description(), e.startDate(), e.endDate(),
                e.maxParticipants(), e.status());
    }
}
