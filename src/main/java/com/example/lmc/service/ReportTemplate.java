package com.example.lmc.service;

public enum ReportTemplate {
    PRINCIPAL("reports/lmc_anexo_oficial.jrxml"),
    SUB_COMPRAS("reports/sub_compras.jrxml"),
    SUB_VENDAS("reports/sub_vendas_bico.jrxml"),
    SUB_MEDICOES("reports/sub_medicoes_tanque.jrxml"),
    TERMO_ABERTURA("reports/termo_abertura.jrxml"),
    TERMO_ENCERRAMENTO("reports/termo_encerramento.jrxml");

    private final String path;

    ReportTemplate(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
