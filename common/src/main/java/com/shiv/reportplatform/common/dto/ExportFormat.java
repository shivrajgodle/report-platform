package com.shiv.reportplatform.common.dto;

/**
 * Formats the export-service can produce.
 * Each constant carries its file extension and HTTP content type,
 * so the controller never needs a switch statement to build a response.
 */
public enum ExportFormat {

    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    PNG("png", "image/png");

    private final String extension;
    private final String contentType;

    ExportFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }
}
