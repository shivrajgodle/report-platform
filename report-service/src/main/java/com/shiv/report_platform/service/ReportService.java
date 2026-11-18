package com.shiv.report_platform.service;

import com.shiv.report_platform.exception.ReportNotFoundException;
import com.shiv.report_platform.model.ReportDocument;
import com.shiv.report_platform.model.ReportRowDocument;
import com.shiv.report_platform.repository.ReportRepository;
import com.shiv.reportplatform.common.dto.ReportDto;
import com.shiv.reportplatform.common.dto.ReportRowDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.Instant;

/**
 * Business logic for reports. Controllers talk to this class,
 * never to the repository directly.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository repository;

    // Constructor injection: the field is final and easy to mock in tests
    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public ReportDto create(ReportDto request) {
        // Server decides id and createdAt, never trust the client for these
        ReportDocument toSave = new ReportDocument(
                null,
                request.title(),
                request.description(),
                Instant.now(),
                request.rows().stream()
                        .map(r -> new ReportRowDocument(r.label(), r.value(), r.category()))
                        .toList());

        ReportDocument saved = repository.save(toSave);
        log.info("Report created id={} rows={}", saved.id(), saved.rows().size());
        return toDto(saved);
    }


    public ReportDto getById(String id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ReportNotFoundException(id));
    }

    public Page<ReportDto> list(Pageable pageable) {
        // Always paginate: an unbounded findAll() is a production incident waiting to happen
        return repository.findAll((org.springframework.data.domain.Pageable) pageable).map(this::toDto);
    }


    // Document -> DTO mapping kept in one place
    private ReportDto toDto(ReportDocument doc) {
        return new ReportDto(
                doc.id(),
                doc.title(),
                doc.description(),
                doc.createdAt(),
                doc.rows().stream()
                        .map(r -> new ReportRowDto(r.label(), r.value(), r.category()))
                        .toList());
    }

}
