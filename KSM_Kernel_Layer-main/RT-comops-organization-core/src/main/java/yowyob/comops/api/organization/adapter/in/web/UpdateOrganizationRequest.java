package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationRequest(
        @NotBlank String code,
        @NotBlank String legalName,
        @NotBlank String displayName,
        @NotBlank String organizationType) {
}
