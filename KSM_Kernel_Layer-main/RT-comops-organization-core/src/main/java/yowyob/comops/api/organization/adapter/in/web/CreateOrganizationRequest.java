package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrganizationRequest(
        @NotNull UUID businessActorId,
        @NotBlank String code,
        @NotBlank String legalName,
        @NotBlank String displayName,
        @NotBlank String organizationType) {
}
