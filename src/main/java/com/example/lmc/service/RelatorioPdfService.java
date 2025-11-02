package com.example.lmc.service;

import com.example.lmc.entity.LmcFolha;
import com.example.lmc.repository.LmcFolhaRepository;
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

@Service
public class RelatorioPdfService {

    @Autowired
    private LmcFolhaRepository lmcFolhaRepository;

    private JasperReport jasperReportPrincipal;
    private JasperReport jasperSubReportCompras;
    private JasperReport jasperSubReportVendas;
    private JasperReport jasperSubReportMedicoes;

    // Construtor: Compila todos os relatórios na inicialização
    public RelatorioPdfService() {
        try {
            this.jasperReportPrincipal = compileReport("reports/lmc_anexo_oficial.jrxml");
            this.jasperSubReportCompras = compileReport("reports/sub_compras.jrxml");
            this.jasperSubReportVendas = compileReport("reports/sub_vendas_bico.jrxml");
            this.jasperSubReportMedicoes = compileReport("reports/sub_medicoes_tanque.jrxml");

        } catch (IOException | JRException e) {
            e.printStackTrace();
            throw new RuntimeException("Não foi possível compilar os relatórios Jasper.", e);
        }
    }

    private JasperReport compileReport(String path) throws IOException, JRException {
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("Recurso não encontrado no classpath: " + path);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return JasperCompileManager.compileReport(inputStream);
        }
    }

    /**
     * Gera um relatório PDF de LMC para o período, onde cada LmcFolha
     * (dia/produto) é renderizada como uma página separada no mesmo PDF.
     */
    public byte[] gerarRelatorioPdf(LocalDate dataInicio, LocalDate dataFim) throws JRException {

        // 1. Busca todos os dados do banco
        Set<LmcFolha> folhas = lmcFolhaRepository.findByDataBetweenEager(dataInicio, dataFim);

        // 2. Cria uma lista para armazenar cada página preenchida
        List<JasperPrint> jasperPrintList = new ArrayList<>();

        // 3. Itera sobre cada folha (cada dia/produto)
        for (LmcFolha folha : folhas) {
            // Cria um mapa de parâmetros para ESTA folha
            Map<String, Object> parameters = new HashMap<>();

            // === Parâmetros da Empresa e Período ===
            parameters.put("EMPRESA_NOME", "Teste");
            parameters.put("EMPRESA_CNPJ", "32.458.917/0001-54");
            parameters.put("EMPRESA_ENDERECO", "Teste, 000 - Teste, Itobi - SP");

            // Converte LocalDate para java.util.Date (exigido pelo novo JRXML)
            parameters.put("DATA", Date.valueOf(folha.getData()));
            parameters.put("PERIODO_INICIO", Date.valueOf(dataInicio));
            parameters.put("PERIODO_FIM", Date.valueOf(dataFim));
            parameters.put("PRODUTO", folha.getProduto().getNome());

            // === Parâmetros de Totais ===
            // O novo relatório calcula o Estoque de Abertura (3.1) no Java
            BigDecimal estoqueAberturaTotal = folha.getVolumeDisponivel().subtract(folha.getTotalRecebido());
            parameters.put("ESTOQUE_ABERTURA_TOTAL", estoqueAberturaTotal);

            parameters.put("VOLUME_RECEBIDO_TOTAL", folha.getTotalRecebido());
            parameters.put("VOLUME_DISPONIVEL", folha.getVolumeDisponivel());
            parameters.put("VENDAS_DIA_TOTAL", folha.getTotalVendasDia());
            parameters.put("ESTOQUE_ESCRITURAL", folha.getEstoqueEscritural());
            parameters.put("FECHAMENTO_FISICO_TOTAL", folha.getEstoqueFechamento()); // Total (7) e (9.1)
            parameters.put("PERDAS_GANHOS", folha.getPerdasGanhos());
            parameters.put("VALOR_VENDAS_DIA", folha.getValorVendasDia());
            parameters.put("VALOR_VENDAS_MES", folha.getValorAcumuladoMes());

            // === Parâmetros das Coleções (para os sub-relatórios) ===
            parameters.put("LIST_COMPRAS", folha.getCompras());
            parameters.put("LIST_VENDAS_BICO", folha.getVendasBico());
            parameters.put("LIST_MEDICOES_TANQUE", folha.getMedicoesTanque());

            // === Parâmetros dos Sub-relatórios Compilados ===
            parameters.put("SUBREPORT_COMPRAS", this.jasperSubReportCompras);
            parameters.put("SUBREPORT_VENDAS", this.jasperSubReportVendas);
            parameters.put("SUBREPORT_MEDICOES", this.jasperSubReportMedicoes);

            // 4. Preenche o relatório principal com os parâmetros
            // Usamos JREmptyDataSource(1) porque o relatório principal é um template estático
            // e não itera sobre uma coleção principal. O '1' garante que a banda <detail> renderize.
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    this.jasperReportPrincipal,
                    parameters,
                    new JREmptyDataSource(1)
            );

            // 5. Adiciona a página preenchida à lista
            jasperPrintList.add(jasperPrint);
        }

        // 6. Se a lista estiver vazia, retorna um PDF vazio ou lança exceção
        if (jasperPrintList.isEmpty()) {
            // Você pode retornar um PDF de "Nenhum dado encontrado" ou lançar uma exceção
            throw new JRException("Nenhum dado encontrado para o período selecionado.");
        }

        // 7. Exporta a LISTA de JasperPrints para um ÚNICO PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        // Define a lista de páginas como entrada
        exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));

        // Define o stream de saída
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

        // Executa a exportação (juntando todos os JPs em um PDF)
        exporter.exportReport();

        return baos.toByteArray();
    }
}
