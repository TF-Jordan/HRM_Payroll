package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.application.port.in.CreateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.CreateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.DeactivateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.ListAgenciesUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyUseCase;
import yowyob.comops.api.organization.application.port.out.AgencySelfServiceCreationPolicy;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.AgencySelfServiceCreationDisabledException;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.DuplicateAgencyCodeException;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.model.Agency;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class AgencyApplicationService
        implements CreateAgencyUseCase, ListAgenciesUseCase, UpdateAgencyUseCase, DeactivateAgencyUseCase {

    private final AgencyRepository agencyRepository;
    private final OrganizationRepository organizationRepository;
    private final Optional<AgencySelfServiceCreationPolicy> agencySelfServiceCreationPolicy;

    public AgencyApplicationService(AgencyRepository agencyRepository, OrganizationRepository organizationRepository,
            Optional<AgencySelfServiceCreationPolicy> agencySelfServiceCreationPolicy) {
        this.agencyRepository = agencyRepository;
        this.organizationRepository = organizationRepository;
        this.agencySelfServiceCreationPolicy = agencySelfServiceCreationPolicy;
    }

    @Override
    public Mono<Agency> createAgency(CreateAgencyCommand command) {
        Objects.requireNonNull(command, "command is required");
        return ensureSelfServiceCreationAllowed(command.tenantId())
                .then(organizationRepository.findById(command.tenantId(), command.organizationId()))
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(command.organizationId())))
                .flatMap(organization -> {
                    Agency agency = Agency.create(command.tenantId(), command.organizationId(), command.code(),
                            command.name(), command.agencyType());
                    return agencyRepository.existsByCode(command.tenantId(), command.organizationId(), agency.code())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateAgencyCodeException(agency.code()))
                                    : agencyRepository.save(agency));
                });
    }

    @Override
    public Flux<Agency> listAgencies(java.util.UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> agencyRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<Agency> updateAgency(UpdateAgencyCommand command) {
        Objects.requireNonNull(command, "command is required");
        return agencyRepository.findById(command.tenantId(), command.agencyId())
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(command.agencyId())))
                .flatMap(existing -> agencyRepository.existsByCode(command.tenantId(), existing.organizationId(), command.code())
                        .flatMap(exists -> exists && !existing.code().equalsIgnoreCase(command.code())
                                ? Mono.<Agency>error(new DuplicateAgencyCodeException(command.code()))
                                : agencyRepository.save(existing.update(command.code(), command.name(), command.agencyType()))));
    }

    @Override
    public Mono<Void> deactivateAgency(java.util.UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> agencyRepository.findById(context.tenantId(), agencyId)
                        .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                        .map(Agency::deactivate)
                        .flatMap(agencyRepository::save)
                        .then());
    }

    private Mono<Void> ensureSelfServiceCreationAllowed(java.util.UUID tenantId) {
        return agencySelfServiceCreationPolicy
                .map(policy -> policy.isSelfServiceCreationAllowed(tenantId)
                        .flatMap(isAllowed -> isAllowed
                                ? Mono.<Void>empty()
                                : Mono.error(new AgencySelfServiceCreationDisabledException())))
                .orElseGet(Mono::empty);
    }
}
