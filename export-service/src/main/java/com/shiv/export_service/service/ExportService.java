package com.shiv.export_service.service;

import com.shiv.export_service.exception.UnsupportedFormatException;
import com.shiv.export_service.exporter.ReportExporter;
import com.shiv.reportplatform.common.dto.ExportFormat;
import com.shiv.reportplatform.common.dto.ExportRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 * Picks the right exporter for the requested format.
 * Spring injects ALL beans implementing ReportExporter as a List,
 * so a new exporter class is picked up with no change here.
 */
@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);


    private final Map<ExportFormat, ReportExporter> exporters = new EnumMap<>(ExportFormat.class);

    public ExportService(List<ReportExporter> exporterBeans) {
        exporterBeans.forEach(e -> exporters.put(e.supportedFormat(), e));
        log.info("Registered exporters for formats: {}", exporters.keySet());
    }

    public byte[] export(ExportRequest request) {
        ReportExporter exporter = exporters.get(request.format());
        if (exporter == null) {
            throw new UnsupportedFormatException(request.format());
        }
        long start = System.currentTimeMillis();
        byte[] file = exporter.export(request.report());
        log.info("Exported report={} format={} bytes={} tookMs={}",
                request.report().id(), request.format(), file.length,
                System.currentTimeMillis() - start);
        return file;
    }
}
