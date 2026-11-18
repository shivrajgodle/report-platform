package com.shiv.report_platform.repository;

import com.shiv.report_platform.model.ReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data generates the implementation at runtime.
 * save, findById and findAll(Pageable) come for free.
 */
public interface ReportRepository extends MongoRepository<ReportDocument, String> {
}
