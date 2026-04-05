package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.settings.application.port.in.UpsertDocumentSequenceCommand;
import yowyob.comops.api.settings.application.port.in.UpsertDocumentSequenceUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/settings/document-sequences")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:write')")
public class SettingsController {

    private final UpsertDocumentSequenceUseCase upsertDocumentSequenceUseCase;

    public SettingsController(UpsertDocumentSequenceUseCase upsertDocumentSequenceUseCase) {
        this.upsertDocumentSequenceUseCase = upsertDocumentSequenceUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<DocumentSequenceResponse>>> upsertSequence(
            @Valid @RequestBody Mono<UpsertDocumentSequenceRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> upsertDocumentSequenceUseCase.upsert(new UpsertDocumentSequenceCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().agencyId(),
                        tuple.getT1().documentType(),
                        tuple.getT1().prefix(),
                        tuple.getT1().suffix(),
                        tuple.getT1().paddingWidth(),
                        tuple.getT1().nextNumber())))
                .map(DocumentSequenceResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Document sequence saved.")));
    }
}
