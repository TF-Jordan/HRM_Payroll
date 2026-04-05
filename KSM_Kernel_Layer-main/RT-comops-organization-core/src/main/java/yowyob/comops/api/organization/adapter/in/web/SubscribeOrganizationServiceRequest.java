package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record SubscribeOrganizationServiceRequest(@NotBlank String serviceCode) {
}
