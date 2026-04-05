package yowyob.comops.api.settings.application.service;

import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberCommand;
import yowyob.comops.api.settings.application.port.in.GenerateDocumentNumberUseCase;
import yowyob.comops.api.settings.application.port.in.GetAppBusinessSettingsUseCase;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsCommand;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsUseCase;
import yowyob.comops.api.settings.application.port.in.UpsertDocumentSequenceCommand;
import yowyob.comops.api.settings.application.port.in.UpsertDocumentSequenceUseCase;
import yowyob.comops.api.settings.application.port.out.AppBusinessSettingsRepository;
import yowyob.comops.api.settings.application.port.out.DocumentSequenceRepository;
import yowyob.comops.api.settings.domain.DocumentSequenceNotFoundException;
import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import yowyob.comops.api.settings.domain.model.DocumentSequence;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SettingsApplicationService implements UpsertDocumentSequenceUseCase, GenerateDocumentNumberUseCase,
        GetAppBusinessSettingsUseCase, UpdateAppBusinessSettingsUseCase {

    private final DocumentSequenceRepository documentSequenceRepository;
    private final AppBusinessSettingsRepository appBusinessSettingsRepository;

    public SettingsApplicationService(DocumentSequenceRepository documentSequenceRepository,
            AppBusinessSettingsRepository appBusinessSettingsRepository) {
        this.documentSequenceRepository = documentSequenceRepository;
        this.appBusinessSettingsRepository = appBusinessSettingsRepository;
    }

    @Override
    public Mono<DocumentSequence> upsert(UpsertDocumentSequenceCommand command) {
        Objects.requireNonNull(command, "command is required");
        return documentSequenceRepository.findByScopeAndType(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.documentType())
                .map(existing -> existing.reconfigure(command.prefix(), command.suffix(), command.paddingWidth(),
                        command.nextNumber()))
                .switchIfEmpty(Mono.fromSupplier(() -> DocumentSequence.create(command.tenantId(), command.organizationId(),
                        command.agencyId(), command.documentType(), command.prefix(), command.suffix(),
                        command.paddingWidth(), command.nextNumber())))
                .flatMap(documentSequenceRepository::save);
    }

    @Override
    public Mono<String> generate(GenerateDocumentNumberCommand command) {
        Objects.requireNonNull(command, "command is required");
        return findSequence(command)
                .switchIfEmpty(Mono.error(new DocumentSequenceNotFoundException(command.documentType())))
                .flatMap(sequence -> documentSequenceRepository.save(sequence.advance())
                        .thenReturn(sequence.currentFormattedValue()));
    }

    private Mono<DocumentSequence> findSequence(GenerateDocumentNumberCommand command) {
        Mono<DocumentSequence> scoped = documentSequenceRepository.findByScopeAndType(command.tenantId(),
                command.organizationId(), command.agencyId(), command.documentType());
        if (command.agencyId() == null) {
            return scoped;
        }
        return scoped.switchIfEmpty(documentSequenceRepository.findByScopeAndType(command.tenantId(),
                command.organizationId(), null, command.documentType()));
    }

    @Override
    public Mono<AppBusinessSettings> getSettings(java.util.UUID tenantId, java.util.UUID organizationId) {
        return appBusinessSettingsRepository.findByOrganizationId(tenantId, organizationId)
                .switchIfEmpty(Mono.defer(() -> appBusinessSettingsRepository
                        .save(AppBusinessSettings.defaults(tenantId, organizationId))));
    }

    @Override
    public Mono<AppBusinessSettings> updateSettings(UpdateAppBusinessSettingsCommand command) {
        Objects.requireNonNull(command, "command is required");
        return appBusinessSettingsRepository.findByOrganizationId(command.tenantId(), command.organizationId())
                .defaultIfEmpty(AppBusinessSettings.defaults(command.tenantId(), command.organizationId()))
                .map(existing -> existing.update(command.agencyId(), command.negotiateSellingPrice(),
                        command.sellingPriceIncludeVat(), command.authorizeExceptionalDiscount(),
                        command.grantableDiscountRate(), command.printLogo(), command.paperFormat(),
                        command.lengthOfVatInvoiceNumber(), command.prefixOfVatInvoiceNumber(),
                        command.lowStockAlert(), command.preventiveMaintenanceAlert()))
                .flatMap(appBusinessSettingsRepository::save);
    }
}
