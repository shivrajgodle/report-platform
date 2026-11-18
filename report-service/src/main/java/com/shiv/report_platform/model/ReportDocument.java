package com.shiv.report_platform.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Maps to the "reports" collection in MongoDB.
 * Rows are embedded because they are always read and written together
 * with their report (a natural fit for a document database).
 */
@Document(collection = "reports")
public record ReportDocument(
        @Id String id,                 // null on insert, Mongo generates an ObjectId
        String title,
        String description,
        Instant createdAt,
        List<ReportRowDocument> rows
) {
}