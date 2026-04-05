package yowyob.comops.api.actor.application.service;

import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.actor.domain.DuplicateActorEmailException;
import yowyob.comops.api.actor.domain.model.Actor;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ActorApplicationService implements CreateActorUseCase {

    private final ActorRepository actorRepository;

    public ActorApplicationService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    @Override
    public Mono<Actor> createActor(CreateActorCommand command) {
        Objects.requireNonNull(command, "command is required");

        Actor actor = Actor.create(
                command.tenantId(),
                command.firstName(),
                command.lastName(),
                command.phoneNumber(),
                command.email(),
                command.gender(),
                command.nationality(),
                command.birthDate(),
                command.profession(),
                command.biography());

        if (actor.email() == null) {
            return actorRepository.save(actor);
        }

        return actorRepository.existsActiveByEmail(actor.tenantId(), actor.email())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateActorEmailException(actor.email()))
                        : actorRepository.save(actor));
    }
}
