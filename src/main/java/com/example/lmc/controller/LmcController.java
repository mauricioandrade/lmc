package com.example.lmc.controller;

import com.example.lmc.dto.LmcFolhaRequestDTO;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.service.LmcService;
import com.example.lmc.service.RelatorioPdfService;
import com.example.lmc.service.RelatorioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/lmc")
@CrossOrigin(origins = "http://localhost:5173")
public class LmcController {

    @Autowired
    private LmcService lmcService;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private RelatorioPdfService relatorioPdfService;

    @PostMapping
    public ResponseEntity<LmcFolha> salvarFolha(
            @Valid @RequestBody LmcFolhaRequestDTO requestDTO
    ) {
        LmcFolha folhaSalva = lmcService.salvarFolhaDiaria(requestDTO);
        return new ResponseEntity<>(folhaSalva, HttpStatus.CREATED);
    }

    @GetMapping("/relatorio")
    public ResponseEntity<Set<LmcFolha>> gerarRelatorio(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Set<LmcFolha> relatorio = relatorioService.gerarRelatorio(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<byte[]> gerarRelatorioPdf(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        try {
            byte[] pdfBytes = relatorioPdfService.gerarRelatorioPdf(inicio, fim);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "LMC_Relatorio.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

