package com.shiv.reportplatform.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

/**
 * A full report as sent over the wire.
 * id and createdAt are filled by report-service, not by the client,
 * so they have no validation annotations.
 */
public record ReportDto(
        String id,
        @NotBlank(message = "title is required") String title,
        String description,
        Instant createdAt,
        // @Valid makes validation cascade into each row
        @NotEmpty(message = "at lease one row is required") List<@Valid ReportRowDto> rows
) {
}
