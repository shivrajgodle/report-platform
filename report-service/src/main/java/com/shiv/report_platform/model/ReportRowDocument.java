package com.shiv.report_platform.model;

import java.math.BigDecimal;

/**
 * Row embedded inside a report document (no separate collection).
 * We keep a persistence model separate from the API DTO, so changing
 * the database shape never silently changes the public API.
 */
public record ReportRowDocument(String label, BigDecimal value, String category) {
}