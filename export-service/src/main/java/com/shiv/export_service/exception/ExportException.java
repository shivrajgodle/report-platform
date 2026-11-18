package com.shiv.export_service.exception;

/** Thrown when generating a file fails. Wraps the underlying Aspose exception. */
public class ExportException extends RuntimeException {
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
