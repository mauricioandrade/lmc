package com.example.lmc.controller;

import com.example.lmc.dto.LmcFolhaRequestDTO;
import com.example.lmc.entity.LmcCompra;
import com.example.lmc.entity.LmcFolha;
import com.example.lmc.entity.LmcMedicaoTanque;
import com.example.lmc.entity.LmcVendaBico;
import com.example.lmc.service.LmcService;
import com.example.lmc.service.RelatorioPdfService;
import com.example.lmc.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/lmc")
// --- MUDANÇA: Adicionado PUT, DELETE, OPTIONS ---
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@Tag(name = "LMC", description = "Operações principais da Folha LMC")
public class LmcController {

    private final LmcService lmcService;
    private final RelatorioService relatorioService;
    private final RelatorioPdfService relatorioPdfService;

    @Autowired
    public LmcController(LmcService lmcService, RelatorioService relatorioService, RelatorioPdfService relatorioPdfService) {
        this.lmcService = lmcService;
        this.relatorioService = relatorioService;
        this.relatorioPdfService = relatorioPdfService;
    }

    @PostMapping
    @Operation(summary = "Criar folha diária", description = "Registra uma nova folha LMC diária com medições, vendas e compras")
    public ResponseEntity<LmcFolha> salvarFolha(
            @Valid @RequestBody LmcFolhaRequestDTO requestDTO
    ) {
        LmcFolha folhaSalva = lmcService.salvarFolhaDiaria(requestDTO);
        return new ResponseEntity<>(folhaSalva, HttpStatus.CREATED);
    }

    @GetMapping("/folha")
    @Operation(summary = "Buscar folha por data e produto", description = "Busca uma única folha LMC (e seus dados) pela data e ID do produto")
    public ResponseEntity<LmcFolha> buscarFolhaParaEdicao(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam("produtoId") Long produtoId
    ) {
        try {
            LmcFolha folha = lmcService.buscarFolhaPorDataEProduto(data, produtoId);
            return ResponseEntity.ok(folha);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/relatorio")
    @Operation(summary = "Gerar relatório em JSON", description = "Gera o relatório consolidado de folhas no período informado")
    public ResponseEntity<Set<LmcFolha>> gerarRelatorio(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Set<LmcFolha> relatorio = relatorioService.gerarRelatorio(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorio/pdf")
    @Operation(summary = "Gerar relatório em PDF", description = "Exporta o relatório LMC consolidado em formato PDF para o período informado")
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

        } catch (JRException e) {
            if (e.getMessage().contains("Nenhum dado encontrado")) {
                return ResponseEntity.noContent().build();
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- MUDANÇA: NOVO ENDPOINT (ADICIONAR MEDIÇÃO) ---
    @PostMapping("/folhas/{folhaId}/medicoes")
    @Operation(summary = "Adicionar nova medição", description = "Adiciona uma nova medição de tanque a uma folha existente")
    public ResponseEntity<LmcMedicaoTanque> adicionarMedicao(
            @PathVariable Long folhaId,
            @Valid @RequestBody LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {

        LmcMedicaoTanque medicaoSalva = lmcService.adicionarMedicaoTanque(folhaId, medicaoDTO);
        return new ResponseEntity<>(medicaoSalva, HttpStatus.CREATED);
    }
    // --- FIM DA MUDANÇA ---

    @PutMapping("/medicoes/{id}")
    @Operation(summary = "Atualizar medição de tanque", description = "Atualiza uma medição de tanque e recalcula a folha")
    public ResponseEntity<LmcMedicaoTanque> atualizarMedicao(
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) { // Usa DTO

        LmcMedicaoTanque medicao = lmcService.atualizarMedicaoTanque(id, medicaoDTO);
        return ResponseEntity.ok(medicao);
    }

    @DeleteMapping("/medicoes/{id}")
    @Operation(summary = "Deletar medição de tanque", description = "Deleta uma medição de tanque e recalcula a folha")
    public ResponseEntity<Void> deletarMedicao(@PathVariable Long id) {
        lmcService.deletarMedicaoTanque(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/compras/{id}")
    @Operation(summary = "Atualizar compra", description = "Atualiza uma compra e recalcula a folha")
    public ResponseEntity<LmcCompra> atualizarCompra(
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.CompraDTO compraDTO) { // Usa DTO

        LmcCompra compra = lmcService.atualizarCompra(id, compraDTO);
        return ResponseEntity.ok(compra);
    }

    @DeleteMapping("/compras/{id}")
    @Operation(summary = "Deletar compra", description = "Deleta uma compra e recalcula a folha")
    public ResponseEntity<Void> deletarCompra(@PathVariable Long id) {
        lmcService.deletarCompra(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/vendas/{id}")
    @Operation(summary = "Atualizar venda de bico", description = "Atualiza uma venda de bico e recalcula a folha")
    public ResponseEntity<LmcVendaBico> atualizarVenda(
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) { // Usa DTO

        LmcVendaBico venda = lmcService.atualizarVenda(id, vendaDTO);
        return ResponseEntity.ok(venda);
    }

    @DeleteMapping("/vendas/{id}")
    @Operation(summary = "Deletar venda de bico", description = "Deleta uma venda de bico e recalcula a folha")
    public ResponseEntity<Void> deletarVenda(@PathVariable Long id) {
        lmcService.deletarVenda(id);
        return ResponseEntity.noContent().build();
    }
}
