package com.shiv.report_platform.exception;

/** Thrown when a report id does not exist. Mapped to HTTP 404 below. */
public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(String id) {
        super("Report not found: " + id);
    }
}
