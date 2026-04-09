package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record RejectLeaveRequestRequest(@NotBlank String reason) {
}
