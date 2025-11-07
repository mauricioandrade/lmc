package com.example.lmc.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

@Component
public class JasperReportManager {

    private final Map<ReportTemplate, JasperReport> cache = new EnumMap<>(ReportTemplate.class);

    public JasperReport getReport(ReportTemplate template) {
        return cache.computeIfAbsent(template, this::compileReport);
    }

    private JasperReport compileReport(ReportTemplate template) {
        Resource resource = new ClassPathResource(template.getPath());
        if (!resource.exists()) {
            throw new IllegalStateException("Recurso não encontrado no classpath: " + template.getPath());
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return JasperCompileManager.compileReport(inputStream);
        } catch (IOException | JRException exception) {
            throw new IllegalStateException("Falha ao compilar " + template.getPath(), exception);
        }
    }
}
