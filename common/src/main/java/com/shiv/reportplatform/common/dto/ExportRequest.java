package com.shiv.reportplatform.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * What report-service sends to export-service:
 * the report data plus the format wanted.
 * Sending the full report (not just an id) keeps export-service
 * stateless and independent of report-service's database.
 */
public record ExportRequest(
        @NotNull(message = "report is required") @Valid ReportDto report,
        @NotNull(message = "format is required") ExportFormat format
) {
}
