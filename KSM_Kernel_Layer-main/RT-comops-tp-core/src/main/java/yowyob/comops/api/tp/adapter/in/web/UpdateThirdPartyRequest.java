package yowyob.comops.api.tp.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateThirdPartyRequest(
        @NotBlank String referenceCode,
        @NotBlank String displayName,
        @NotEmpty Set<String> roles,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        Boolean active,
        boolean prospect) {
}
