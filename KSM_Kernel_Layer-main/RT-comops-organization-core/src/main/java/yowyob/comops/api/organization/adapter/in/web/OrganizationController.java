package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.organization.application.port.in.GetOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.ListMyOrganizationsUseCase;
import yowyob.comops.api.organization.application.port.in.ListOrganizationsUseCase;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.CreateOrganizationCommand;
import yowyob.comops.api.organization.application.port.in.CreateOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.SearchOrganizationsUseCase;
import yowyob.comops.api.organization.application.port.in.TransferOrganizationOwnershipUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateOrganizationCommand;
import yowyob.comops.api.organization.application.port.in.UpdateOrganizationUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/organizations")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class OrganizationController {

    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final GetOrganizationUseCase getOrganizationUseCase;
    private final ListOrganizationsUseCase listOrganizationsUseCase;
    private final ListMyOrganizationsUseCase listMyOrganizationsUseCase;
    private final UpdateOrganizationUseCase updateOrganizationUseCase;
    private final TransferOrganizationOwnershipUseCase transferOrganizationOwnershipUseCase;
    private final SearchOrganizationsUseCase searchOrganizationsUseCase;

    public OrganizationController(CreateOrganizationUseCase createOrganizationUseCase,
            GetOrganizationUseCase getOrganizationUseCase,
            ListOrganizationsUseCase listOrganizationsUseCase,
            ListMyOrganizationsUseCase listMyOrganizationsUseCase,
            UpdateOrganizationUseCase updateOrganizationUseCase,
            TransferOrganizationOwnershipUseCase transferOrganizationOwnershipUseCase,
            SearchOrganizationsUseCase searchOrganizationsUseCase) {
        this.createOrganizationUseCase = createOrganizationUseCase;
        this.getOrganizationUseCase = getOrganizationUseCase;
        this.listOrganizationsUseCase = listOrganizationsUseCase;
        this.listMyOrganizationsUseCase = listMyOrganizationsUseCase;
        this.updateOrganizationUseCase = updateOrganizationUseCase;
        this.transferOrganizationOwnershipUseCase = transferOrganizationOwnershipUseCase;
        this.searchOrganizationsUseCase = searchOrganizationsUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<OrganizationResponse>>> createOrganization(
            @Valid @RequestBody Mono<CreateOrganizationRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createOrganizationUseCase.createOrganization(new CreateOrganizationCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().businessActorId(),
                        tuple.getT1().code(),
                        tuple.getT1().service(),
                        tuple.getT1().resolvedIndividualBusiness(),
                        tuple.getT1().email(),
                        tuple.getT1().shortName(),
                        tuple.getT1().longName(),
                        tuple.getT1().description(),
                        tuple.getT1().logoUri(),
                        tuple.getT1().logoId(),
                        tuple.getT1().websiteUrl(),
                        tuple.getT1().socialNetwork(),
                        tuple.getT1().businessRegistrationNumber(),
                        tuple.getT1().taxNumber(),
                        tuple.getT1().capitalShare(),
                        tuple.getT1().ceoName(),
                        tuple.getT1().yearFounded(),
                        tuple.getT1().keywords(),
                        tuple.getT1().numberOfEmployees(),
                        tuple.getT1().legalForm(),
                        tuple.getT1().resolvedActive(),
                        tuple.getT1().status())))
                .map(OrganizationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Organization created.")));
    }

    @GetMapping("/{organizationId}")
    public Mono<ResponseEntity<ApiResponse<OrganizationResponse>>> getOrganization(
            @PathVariable("organizationId") UUID organizationId) {
        return getOrganizationUseCase.getOrganization(organizationId)
                .map(OrganizationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization retrieved.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<OrganizationResponse>>>> listOrganizations() {
        return listOrganizationsUseCase.listOrganizations()
                .map(OrganizationResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organizations retrieved.")));
    }

    @GetMapping("/my")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationResponse>>>> listMyOrganizations() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listMyOrganizationsUseCase.listMine(context.tenantId(), context.userId())
                        .map(OrganizationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Owned organizations retrieved.")));
    }

    @PatchMapping("/{organizationId}")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<OrganizationResponse>>> updateOrganization(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<UpdateOrganizationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> listMyOrganizationsUseCase.listMine(tuple.getT2().tenantId(), tuple.getT2().userId())
                        .filter(organization -> organization.id().equals(organizationId))
                        .singleOrEmpty()
                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                "organization is not owned by the current business actor")))
                        .flatMap(owned -> updateOrganizationUseCase.update(new UpdateOrganizationCommand(
                                tuple.getT2().tenantId(),
                                organizationId,
                                owned.businessActorId(),
                                tuple.getT1().code(),
                                tuple.getT1().service(),
                                tuple.getT1().resolvedIndividualBusiness(),
                                tuple.getT1().email(),
                                tuple.getT1().shortName(),
                                tuple.getT1().longName(),
                                tuple.getT1().description(),
                                tuple.getT1().logoUri(),
                                tuple.getT1().logoId(),
                                tuple.getT1().websiteUrl(),
                                tuple.getT1().socialNetwork(),
                                tuple.getT1().businessRegistrationNumber(),
                                tuple.getT1().taxNumber(),
                                tuple.getT1().capitalShare(),
                                tuple.getT1().ceoName(),
                                tuple.getT1().yearFounded(),
                                tuple.getT1().keywords(),
                                tuple.getT1().numberOfEmployees(),
                                tuple.getT1().legalForm(),
                                tuple.getT1().resolvedActive(),
                                tuple.getT1().status()))))
                .map(OrganizationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization updated.")));
    }

    @PostMapping("/{organizationId}/transfer/{newOwnerId}")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<OrganizationResponse>>> transferOwnership(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("newOwnerId") UUID newOwnerId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listMyOrganizationsUseCase.listMine(context.tenantId(), context.userId())
                        .filter(organization -> organization.id().equals(organizationId))
                        .singleOrEmpty()
                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                "organization is not owned by the current business actor")))
                        .flatMap(owned -> transferOrganizationOwnershipUseCase.transfer(context.tenantId(),
                                organizationId, owned.businessActorId(), newOwnerId)))
                .map(OrganizationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization ownership transferred.")));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationSearchResponse>>>> searchOrganizations(
            @RequestParam("q") String query,
            @RequestParam(value = "organizationType", required = false) String organizationType) {
        return searchOrganizationsUseCase.searchOrganizations(query, organizationType)
                .map(OrganizationSearchResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization search results retrieved.")));
    }
}
