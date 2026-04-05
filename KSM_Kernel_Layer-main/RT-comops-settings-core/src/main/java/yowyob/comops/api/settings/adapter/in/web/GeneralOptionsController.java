package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.settings.application.port.in.GetAppBusinessSettingsUseCase;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsCommand;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping({"/api/general-options", "/api/generalOptions"})
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:write')")
public class GeneralOptionsController {

    private final GetAppBusinessSettingsUseCase getAppBusinessSettingsUseCase;
    private final UpdateAppBusinessSettingsUseCase updateAppBusinessSettingsUseCase;

    public GeneralOptionsController(GetAppBusinessSettingsUseCase getAppBusinessSettingsUseCase,
            UpdateAppBusinessSettingsUseCase updateAppBusinessSettingsUseCase) {
        this.getAppBusinessSettingsUseCase = getAppBusinessSettingsUseCase;
        this.updateAppBusinessSettingsUseCase = updateAppBusinessSettingsUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<AppBusinessSettingsResponse>>> getOptions() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> getAppBusinessSettingsUseCase.getSettings(context.tenantId(), context.organizationId()))
                .map(AppBusinessSettingsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "General options retrieved.")));
    }

    @PutMapping
    public Mono<ResponseEntity<ApiResponse<AppBusinessSettingsResponse>>> updateOptions(
            @Valid @RequestBody Mono<UpdateAppBusinessSettingsRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateAppBusinessSettingsUseCase.updateSettings(new UpdateAppBusinessSettingsCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT2().organizationId(),
                        tuple.getT1().agencyId(),
                        tuple.getT1().negotiateSellingPrice(),
                        tuple.getT1().sellingPriceIncludeVat(),
                        tuple.getT1().authorizeExceptionalDiscount(),
                        tuple.getT1().grantableDiscountRate(),
                        tuple.getT1().printLogo(),
                        tuple.getT1().paperFormat(),
                        tuple.getT1().lengthOfVatInvoiceNumber(),
                        tuple.getT1().prefixOfVatInvoiceNumber(),
                        tuple.getT1().lowStockAlert(),
                        tuple.getT1().preventiveMaintenanceAlert())))
                .map(AppBusinessSettingsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "General options updated.")));
    }
}
