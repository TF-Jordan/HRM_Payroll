package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.organization.application.port.in.CreateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.CreateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.ListAgenciesUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
@RequestMapping("/api/organizations/{organizationId}/agencies")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class AgencyController {

    private final CreateAgencyUseCase createAgencyUseCase;
    private final ListAgenciesUseCase listAgenciesUseCase;

    public AgencyController(CreateAgencyUseCase createAgencyUseCase, ListAgenciesUseCase listAgenciesUseCase) {
        this.createAgencyUseCase = createAgencyUseCase;
        this.listAgenciesUseCase = listAgenciesUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> createAgency(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<CreateAgencyRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createAgencyUseCase.createAgency(new CreateAgencyCommand(tuple.getT2().tenantId(),
                        organizationId, tuple.getT1().code(), tuple.getT1().name(), tuple.getT1().agencyType())))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Agency created.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<AgencyResponse>>>> listAgencies(
            @PathVariable("organizationId") UUID organizationId) {
        return listAgenciesUseCase.listAgencies(organizationId)
                .map(AgencyResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agencies retrieved.")));
    }
}
