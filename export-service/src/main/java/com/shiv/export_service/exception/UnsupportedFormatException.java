package com.shiv.export_service.exception;

import com.shiv.reportplatform.common.dto.ExportFormat;

/** Thrown when no exporter is registered for the requested format. */
public class UnsupportedFormatException extends RuntimeException {
    public UnsupportedFormatException(ExportFormat format) {
        super("Export format not supported yet: " + format);
    }
}