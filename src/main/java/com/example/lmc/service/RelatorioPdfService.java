package com.example.lmc.service;

import com.example.lmc.entity.Empresa;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.repository.LmcFolhaRepository;
import jakarta.persistence.EntityNotFoundException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RelatorioPdfService {

    private final LmcFolhaRepository lmcFolhaRepository;
    private final EmpresaService empresaService;
    private final JasperReportManager jasperReportManager;

    public RelatorioPdfService(LmcFolhaRepository lmcFolhaRepository, EmpresaService empresaService, JasperReportManager jasperReportManager) {
        this.lmcFolhaRepository = lmcFolhaRepository;
        this.empresaService = empresaService;
        this.jasperReportManager = jasperReportManager;
    }

    public byte[] gerarRelatorioPdf(LocalDate dataInicio, LocalDate dataFim) throws JRException {
        List<LmcFolha> folhas = buscarFolhasOrdenadas(dataInicio, dataFim);
        if (folhas.isEmpty()) {
            throw new JRException("Nenhum dado encontrado para o período selecionado.");
        }

        Empresa empresaAtiva = buscarEmpresaAtiva();
        JasperReport principal = jasperReportManager.getReport(ReportTemplate.PRINCIPAL);
        JasperReport subCompras = jasperReportManager.getReport(ReportTemplate.SUB_COMPRAS);
        JasperReport subVendas = jasperReportManager.getReport(ReportTemplate.SUB_VENDAS);
        JasperReport subMedicoes = jasperReportManager.getReport(ReportTemplate.SUB_MEDICOES);

        Map<String, Object> paramsTermos = criarParametrosTermo(empresaAtiva, dataInicio, dataFim, folhas.size());
        List<JasperPrint> prints = new ArrayList<>(folhas.size() + 2);
        prints.add(preencherRelatorio(ReportTemplate.TERMO_ABERTURA, paramsTermos));

        for (LmcFolha folha : folhas) {
            Map<String, Object> parametrosFolha = criarParametrosFolha(folha, empresaAtiva, dataInicio, dataFim, subCompras, subVendas, subMedicoes);
            prints.add(preencherRelatorio(principal, parametrosFolha));
        }

        prints.add(preencherRelatorio(ReportTemplate.TERMO_ENCERRAMENTO, paramsTermos));
        return exportarPdf(prints);
    }

    private List<LmcFolha> buscarFolhasOrdenadas(LocalDate dataInicio, LocalDate dataFim) {
        Set<LmcFolha> folhasSet = lmcFolhaRepository.findByDataBetweenEager(dataInicio, dataFim);
        return folhasSet.stream()
                .sorted(Comparator
                        .comparing(LmcFolha::getData)
                        .thenComparing(folha -> folha.getProduto() == null ? "" : folha.getProduto().getNome(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private Empresa buscarEmpresaAtiva() {
        try {
            return empresaService.getEmpresaAtiva();
        } catch (EntityNotFoundException e) {
            Empresa empresa = new Empresa();
            empresa.setRazaoSocial("Empresa Não Configurada");
            empresa.setCnpj("00.000.000/0000-00");
            empresa.setEnderecoCompleto("Configure uma empresa ativa no sistema.");
            return empresa;
        }
    }

    private Map<String, Object> criarParametrosTermo(Empresa empresa, LocalDate dataInicio, LocalDate dataFim, int quantidadeFolhas) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("EMPRESA_NOME", empresa.getRazaoSocial());
        parametros.put("EMPRESA_CNPJ", empresa.getCnpj());
        parametros.put("EMPRESA_ENDERECO", empresa.getEnderecoCompleto());
        parametros.put("PERIODO_INICIO", Date.valueOf(dataInicio));
        parametros.put("PERIODO_FIM", Date.valueOf(dataFim));
        parametros.put("LIVRO_NUMERO", String.format("001/%d", dataInicio.getYear()));
        parametros.put("TOTAL_FOLHAS", quantidadeFolhas);
        return parametros;
    }

    private Map<String, Object> criarParametrosFolha(LmcFolha folha, Empresa empresa, LocalDate dataInicio, LocalDate dataFim,
                                                     JasperReport subCompras, JasperReport subVendas, JasperReport subMedicoes) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("EMPRESA_NOME", empresa.getRazaoSocial());
        parametros.put("EMPRESA_CNPJ", empresa.getCnpj());
        parametros.put("EMPRESA_ENDERECO", empresa.getEnderecoCompleto());
        parametros.put("DATA", Date.valueOf(folha.getData()));
        parametros.put("PERIODO_INICIO", Date.valueOf(dataInicio));
        parametros.put("PERIODO_FIM", Date.valueOf(dataFim));
        parametros.put("PRODUTO", folha.getProduto() != null ? folha.getProduto().getNome() : "");

        BigDecimal totalRecebido = valorOuZero(folha.getTotalRecebido());
        BigDecimal volumeDisponivel = valorOuZero(folha.getVolumeDisponivel());
        BigDecimal vendasDia = valorOuZero(folha.getTotalVendasDia());
        BigDecimal estoqueEscritural = valorOuZero(folha.getEstoqueEscritural());
        BigDecimal fechamentoFisico = valorOuZero(folha.getEstoqueFechamento());
        BigDecimal perdasGanhos = valorOuZero(folha.getPerdasGanhos());
        BigDecimal valorVendasDia = valorOuZero(folha.getValorVendasDia());
        BigDecimal valorVendasMes = valorOuZero(folha.getValorAcumuladoMes());
        BigDecimal estoqueAbertura = volumeDisponivel.subtract(totalRecebido);

        parametros.put("ESTOQUE_ABERTURA_TOTAL", estoqueAbertura);
        parametros.put("VOLUME_RECEBIDO_TOTAL", totalRecebido);
        parametros.put("VOLUME_DISPONIVEL", volumeDisponivel);
        parametros.put("VENDAS_DIA_TOTAL", vendasDia);
        parametros.put("ESTOQUE_ESCRITURAL", estoqueEscritural);
        parametros.put("FECHAMENTO_FISICO_TOTAL", fechamentoFisico);
        parametros.put("PERDAS_GANHOS", perdasGanhos);
        parametros.put("VALOR_VENDAS_DIA", valorVendasDia);
        parametros.put("VALOR_VENDAS_MES", valorVendasMes);
        parametros.put("LIST_COMPRAS", Optional.ofNullable(folha.getCompras()).orElse(Collections.emptySet()));
        parametros.put("LIST_VENDAS_BICO", Optional.ofNullable(folha.getVendasBico()).orElse(Collections.emptySet()));
        parametros.put("LIST_MEDICOES_TANQUE", Optional.ofNullable(folha.getMedicoesTanque()).orElse(Collections.emptySet()));
        parametros.put("SUBREPORT_COMPRAS", subCompras);
        parametros.put("SUBREPORT_VENDAS", subVendas);
        parametros.put("SUBREPORT_MEDICOES", subMedicoes);
        return parametros;
    }

    private JasperPrint preencherRelatorio(ReportTemplate template, Map<String, Object> parametros) throws JRException {
        JasperReport relatorio = jasperReportManager.getReport(template);
        return preencherRelatorio(relatorio, parametros);
    }

    private JasperPrint preencherRelatorio(JasperReport relatorio, Map<String, Object> parametros) throws JRException {
        return JasperFillManager.fillReport(relatorio, parametros, new JREmptyDataSource(1));
    }

    private byte[] exportarPdf(List<JasperPrint> prints) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(SimpleExporterInput.getInstance(prints));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }

    private static BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
