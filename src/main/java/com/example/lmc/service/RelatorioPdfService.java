package com.example.lmc.service;

import com.example.lmc.entity.Empresa;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.repository.LmcFolhaRepository;
import jakarta.persistence.EntityNotFoundException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioPdfService {

    private final LmcFolhaRepository lmcFolhaRepository;
    private final EmpresaService empresaService;

    private volatile JasperReport jasperReportPrincipal;
    private volatile JasperReport jasperSubReportCompras;
    private volatile JasperReport jasperSubReportVendas;
    private volatile JasperReport jasperSubReportMedicoes;
    private volatile JasperReport jasperTermoAbertura;
    private volatile JasperReport jasperTermoEncerramento;

    @Autowired
    public RelatorioPdfService(LmcFolhaRepository lmcFolhaRepository, EmpresaService empresaService) {
        this.lmcFolhaRepository = lmcFolhaRepository;
        this.empresaService = empresaService;
    }

    private JasperReport compileOnce(JasperReport cached, String path) {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = compileReport(path);
                    switch (path) {
                        case "reports/lmc_anexo_oficial.jrxml" -> jasperReportPrincipal = cached;
                        case "reports/sub_compras.jrxml" -> jasperSubReportCompras = cached;
                        case "reports/sub_vendas_bico.jrxml" -> jasperSubReportVendas = cached;
                        case "reports/sub_medicoes_tanque.jrxml" -> jasperSubReportMedicoes = cached;
                        case "reports/termo_abertura.jrxml" -> jasperTermoAbertura = cached;
                        case "reports/termo_encerramento.jrxml" -> jasperTermoEncerramento = cached;
                    }
                }
            }
        }
        return cached;
    }

    private JasperReport getPrincipal() {
        return compileOnce(jasperReportPrincipal, "reports/lmc_anexo_oficial.jrxml");
    }

    private JasperReport getSubCompras() {
        return compileOnce(jasperSubReportCompras, "reports/sub_compras.jrxml");
    }

    private JasperReport getSubVendas() {
        return compileOnce(jasperSubReportVendas, "reports/sub_vendas_bico.jrxml");
    }

    private JasperReport getSubMedicoes() {
        return compileOnce(jasperSubReportMedicoes, "reports/sub_medicoes_tanque.jrxml");
    }

    private JasperReport getTermoAbertura() {
        return compileOnce(jasperTermoAbertura, "reports/termo_abertura.jrxml");
    }

    private JasperReport getTermoEncerramento() {
        return compileOnce(jasperTermoEncerramento, "reports/termo_encerramento.jrxml");
    }

    private JasperReport compileReport(String path) {
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalStateException("Recurso não encontrado no classpath: " + path);
        }
        try (InputStream in = resource.getInputStream()) {
            return JasperCompileManager.compileReport(in);
        } catch (IOException | JRException e) {
            throw new IllegalStateException("Falha ao compilar " + path, e);
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public byte[] gerarRelatorioPdf(LocalDate dataInicio, LocalDate dataFim) throws JRException {

        Set<LmcFolha> folhasSet = lmcFolhaRepository.findByDataBetweenEager(dataInicio, dataFim);
        List<LmcFolha> folhas = folhasSet.stream()
                .sorted(Comparator
                        .comparing(LmcFolha::getData)
                        .thenComparing(f -> f.getProduto() == null ? "" : f.getProduto().getNome(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        if (folhas.isEmpty()) {
            throw new JRException("Nenhum dado encontrado para o período selecionado.");
        }

        // Compila todos os relatórios necessários
        JasperReport principal = getPrincipal();
        JasperReport subCompras = getSubCompras();
        JasperReport subVendas = getSubVendas();
        JasperReport subMedicoes = getSubMedicoes();
        JasperReport termoAberturaReport = getTermoAbertura();
        JasperReport termoEncerramentoReport = getTermoEncerramento();

        // Busca a empresa ativa
        Empresa empresaAtiva;
        try {
            empresaAtiva = empresaService.getEmpresaAtiva();
        } catch (EntityNotFoundException e) {
            empresaAtiva = new Empresa();
            empresaAtiva.setRazaoSocial("Empresa Não Configurada");
            empresaAtiva.setCnpj("00.000.000/0000-00");
            empresaAtiva.setEnderecoCompleto("Configure uma empresa ativa no sistema.");
        }

        // Prepara a lista de páginas (termo abertura + folhas + termo encerramento)
        List<JasperPrint> prints = new ArrayList<>(folhas.size() + 2);

        // Parâmetros dos termos
        Map<String, Object> paramsTermos = new HashMap<>();
        paramsTermos.put("EMPRESA_NOME", empresaAtiva.getRazaoSocial());
        paramsTermos.put("EMPRESA_CNPJ", empresaAtiva.getCnpj());
        paramsTermos.put("EMPRESA_ENDERECO", empresaAtiva.getEnderecoCompleto());
        paramsTermos.put("PERIODO_INICIO", Date.valueOf(dataInicio));
        paramsTermos.put("PERIODO_FIM", Date.valueOf(dataFim));

        String livroNumero = String.format("001/%d", dataInicio.getYear());
        paramsTermos.put("LIVRO_NUMERO", livroNumero);
        paramsTermos.put("TOTAL_FOLHAS", folhas.size());

        // Adiciona termo de abertura
        JasperPrint termoAberturaPrint = JasperFillManager.fillReport(
                termoAberturaReport,
                paramsTermos,
                new JREmptyDataSource(1)
        );
        prints.add(termoAberturaPrint);

        // Loop das folhas de movimentação
        for (LmcFolha folha : folhas) {
            Map<String, Object> p = new HashMap<>();

            // Dados dinâmicos da empresa
            p.put("EMPRESA_NOME", empresaAtiva.getRazaoSocial());
            p.put("EMPRESA_CNPJ", empresaAtiva.getCnpj());
            p.put("EMPRESA_ENDERECO", empresaAtiva.getEnderecoCompleto());

            p.put("DATA", Date.valueOf(folha.getData()));
            p.put("PERIODO_INICIO", Date.valueOf(dataInicio));
            p.put("PERIODO_FIM", Date.valueOf(dataFim));
            p.put("PRODUTO", folha.getProduto() != null ? folha.getProduto().getNome() : "");

            BigDecimal totalRecebido = nz(folha.getTotalRecebido());
            BigDecimal volumeDisponivel = nz(folha.getVolumeDisponivel());
            BigDecimal vendasDia = nz(folha.getTotalVendasDia());
            BigDecimal estoqueEscritural = nz(folha.getEstoqueEscritural());
            BigDecimal fechamentoFisico = nz(folha.getEstoqueFechamento());
            BigDecimal perdasGanhos = nz(folha.getPerdasGanhos());
            BigDecimal valorVendasDia = nz(folha.getValorVendasDia());
            BigDecimal valorVendasMes = nz(folha.getValorAcumuladoMes());

            BigDecimal estoqueAbertura = volumeDisponivel.subtract(totalRecebido);

            p.put("ESTOQUE_ABERTURA_TOTAL", estoqueAbertura);
            p.put("VOLUME_RECEBIDO_TOTAL", totalRecebido);
            p.put("VOLUME_DISPONIVEL", volumeDisponivel);
            p.put("VENDAS_DIA_TOTAL", vendasDia);
            p.put("ESTOQUE_ESCRITURAL", estoqueEscritural);
            p.put("FECHAMENTO_FISICO_TOTAL", fechamentoFisico);
            p.put("PERDAS_GANHOS", perdasGanhos);
            p.put("VALOR_VENDAS_DIA", valorVendasDia);
            p.put("VALOR_VENDAS_MES", valorVendasMes);

            // Coleções (usando LIST_ ao invés de SET_)
            p.put("LIST_COMPRAS", Optional.ofNullable(folha.getCompras()).orElse(Collections.emptySet()));
            p.put("LIST_VENDAS_BICO", Optional.ofNullable(folha.getVendasBico()).orElse(Collections.emptySet()));
            p.put("LIST_MEDICOES_TANQUE", Optional.ofNullable(folha.getMedicoesTanque()).orElse(Collections.emptySet()));

            // Subreports compilados
            p.put("SUBREPORT_COMPRAS", subCompras);
            p.put("SUBREPORT_VENDAS", subVendas);
            p.put("SUBREPORT_MEDICOES", subMedicoes);

            JasperPrint jp = JasperFillManager.fillReport(principal, p, new JREmptyDataSource(1));
            prints.add(jp);
        }

        // Adiciona termo de encerramento
        JasperPrint termoEncerramentoPrint = JasperFillManager.fillReport(
                termoEncerramentoReport,
                paramsTermos,
                new JREmptyDataSource(1)
        );
        prints.add(termoEncerramentoPrint);

        // Exporta tudo em um único PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(SimpleExporterInput.getInstance(prints));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        exporter.exportReport();
        return baos.toByteArray();
    }
}
