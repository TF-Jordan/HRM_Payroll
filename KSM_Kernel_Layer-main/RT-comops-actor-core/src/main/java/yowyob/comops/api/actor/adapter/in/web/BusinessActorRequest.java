package yowyob.comops.api.actor.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record BusinessActorRequest(
        @NotBlank String name,
        String businessId,
        String niu,
        String tradeRegistryNumber,
        String website,
        String contactPhone,
        String privateAddress,
        String businessAddress,
        String businessProfile) {
}
