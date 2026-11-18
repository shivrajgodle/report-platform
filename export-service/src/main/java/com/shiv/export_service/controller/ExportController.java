package com.shiv.export_service.controller;

import com.shiv.export_service.service.ExportService;
import com.shiv.reportplatform.common.dto.ExportRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService service;

    public ExportController(ExportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<byte[]> export(@Valid @RequestBody ExportRequest request) {
        byte[] file = service.export(request);

        // Strip anything unsafe from the title before using it in a filename (header injection)
        String safeName = request.report().title().replaceAll("[^a-zA-Z0-9-_]", "_");
        String filename = safeName + "." + request.format().getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(request.format().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(file);
    }
}
