package yowyob.comops.api.hrm.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TerminateEmployeeRequest(@NotNull LocalDate terminationDate) {
}
