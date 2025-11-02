package com.example.lmc.service;

import com.example.lmc.entity.LmcFolha;
import com.example.lmc.repository.LmcFolhaRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioPdfService {

    @Autowired
    private LmcFolhaRepository lmcFolhaRepository;

    public byte[] gerarRelatorioPdf(LocalDate dataInicio, LocalDate dataFim) throws JRException, FileNotFoundException {


        List<LmcFolha> folhas = lmcFolhaRepository.findByDataBetweenEager(dataInicio, dataFim);


        File file = ResourceUtils.getFile("classpath:reports/lmc_anexo.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());


        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(folhas);


        Map<String, Object> parameters = new HashMap<>();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periodo = dataInicio.format(formatter) + " a " + dataFim.format(formatter);

        parameters.put("PERIODO", periodo);
        parameters.put("FOLHA_NUMERO", "1");


        parameters.put("EMPRESA_NOME", "MEU POSTO LTDA");
        parameters.put("EMPRESA_CNPJ", "12.345.678/0001-99");
        parameters.put("EMPRESA_ENDERECO", "Rua Principal, 123 - Centro, Sua Cidade - UF");


        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);


        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        return pdfBytes;
    }
}
