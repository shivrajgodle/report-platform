package com.shiv.report_platform.controller;

import com.shiv.report_platform.service.ReportService;
import com.shiv.reportplatform.common.dto.ReportDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.print.Pageable;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/reports")   // /v1 lets you evolve the API later without breaking clients
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    // @Valid triggers the annotations we put on the DTOs in the common module
    @PostMapping
    public ResponseEntity<ReportDto> create(@Valid @RequestBody ReportDto request) {
        ReportDto created = service.create(request);
        // 201 Created + Location header pointing to the new resource
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ReportDto get(@PathVariable String id) {
        return service.getById(id);
    }

    // Supports ?page=0&size=20&sort=createdAt,desc
    @GetMapping
    public Page<ReportDto> list(Pageable pageable) {
        return service.list(pageable);
    }

}
