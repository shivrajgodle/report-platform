package com.shiv.export_service.exporter;

import com.shiv.reportplatform.common.dto.ExportFormat;
import com.shiv.reportplatform.common.dto.ReportDto;

/**
 * Strategy interface: one implementation per output format.
 * Adding a new format later means adding one new class,
 * with no changes to existing code (open/closed principle).
 */
public interface ReportExporter {

    /** Which format this exporter produces. Used to register it automatically. */
    ExportFormat supportedFormat();

    /** Builds the file in memory and returns its bytes. */
    byte[] export(ReportDto report);
}
