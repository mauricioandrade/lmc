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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.Map; // <-- 1. VERIFIQUE SE ESTE IMPORT ESTÁ AQUI
import java.util.Set;

@RestController
@RequestMapping("/api/lmc")
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
    // ... (ApiResponses)
    public ResponseEntity<LmcFolha> salvarFolha(
            @Valid @RequestBody LmcFolhaRequestDTO requestDTO
    ) {
        LmcFolha folhaSalva = lmcService.salvarFolhaDiaria(requestDTO);
        return new ResponseEntity<>(folhaSalva, HttpStatus.CREATED);
    }

    @GetMapping("/folha")
    @Operation(summary = "Buscar folha por data e produto", description = "Busca uma única folha LMC (e seus dados) pela data e identificador do produto informado")
    // ... (ApiResponses)
    public ResponseEntity<LmcFolha> buscarFolhaParaEdicao(
            @Parameter(description = "Data da folha a ser consultada", example = "2024-01-15")
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Identificador do produto da folha", example = "1")
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
    // ... (ApiResponses)
    public ResponseEntity<Set<LmcFolha>> gerarRelatorio(
            @Parameter(description = "Data inicial do período", example = "2024-01-01")
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período", example = "2024-01-31")
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Set<LmcFolha> relatorio = relatorioService.gerarRelatorio(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorio/pdf")
    @Operation(summary = "Gerar relatório em PDF", description = "Exporta o relatório LMC consolidado em formato PDF para o período informado")
    // ... (ApiResponses)
    public ResponseEntity<byte[]> gerarRelatorioPdf(
            @Parameter(description = "Data inicial do período", example = "2024-01-01")
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período", example = "2024-01-31")
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

    // --- ENDPOINTS DE MEDIÇÃO ---

    @PostMapping("/folhas/{folhaId}/medicoes")
    @Operation(summary = "Adicionar nova medição", description = "Adiciona uma nova medição de tanque a uma folha existente")
    // ... (ApiResponses)
    public ResponseEntity<LmcMedicaoTanque> adicionarMedicao(
            @Parameter(description = "Identificador da folha")
            @PathVariable Long folhaId,
            @Valid @RequestBody LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {

        LmcMedicaoTanque medicaoSalva = lmcService.adicionarMedicaoTanque(folhaId, medicaoDTO);
        return new ResponseEntity<>(medicaoSalva, HttpStatus.CREATED);
    }

    @PutMapping("/medicoes/{id}")
    @Operation(summary = "Atualizar medição de tanque", description = "Atualiza uma medição de tanque e recalcula a folha")
    // ... (ApiResponses)
    public ResponseEntity<LmcMedicaoTanque> atualizarMedicao(
            @Parameter(description = "Identificador da medição")
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {

        LmcMedicaoTanque medicao = lmcService.atualizarMedicaoTanque(id, medicaoDTO);
        return ResponseEntity.ok(medicao);
    }

    @DeleteMapping("/medicoes/{id}")
    @Operation(summary = "Deletar medição de tanque", description = "Deleta uma medição de tanque e recalcula a folha")
    // ... (ApiResponses)
    public ResponseEntity<Void> deletarMedicao(
            @Parameter(description = "Identificador da medição")
            @PathVariable Long id) {
        lmcService.deletarMedicaoTanque(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/folhas/{folhaId}/compras")
    @Operation(summary = "Adicionar nova compra", description = "Adiciona um novo registro de compra a uma folha existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compra registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = LmcCompra.class))),
            @ApiResponse(responseCode = "400", description = "Dados de compra inválidos"),
            @ApiResponse(responseCode = "404", description = "Folha não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao adicionar compra")
    })
    public ResponseEntity<LmcCompra> adicionarCompra(
            @Parameter(description = "Identificador da folha")
            @PathVariable Long folhaId,
            @Valid @RequestBody LmcFolhaRequestDTO.CompraDTO compraDTO) {

        LmcCompra compraSalva = lmcService.adicionarCompra(folhaId, compraDTO);
        return new ResponseEntity<>(compraSalva, HttpStatus.CREATED);
    }
    // --- FIM DA MUDANÇA ---

    @PutMapping("/compras/{id}")
    @Operation(summary = "Atualizar compra", description = "Atualiza uma compra e recalcula a folha")
    // ... (ApiResponses)
    public ResponseEntity<LmcCompra> atualizarCompra(
            @Parameter(description = "Identificador da compra")
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.CompraDTO compraDTO) {

        LmcCompra compra = lmcService.atualizarCompra(id, compraDTO);
        return ResponseEntity.ok(compra);
    }

    @DeleteMapping("/compras/{id}")
    @Operation(summary = "Deletar compra", description = "Deleta uma compra e recalcula a folha")
    // ... (ApiResponses)
    public ResponseEntity<Void> deletarCompra(
            @Parameter(description = "Identificador da compra")
            @PathVariable Long id) {
        lmcService.deletarCompra(id);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS DE VENDA ---

    // --- 3. NOVO ENDPOINT (ADICIONAR VENDA) ---
    @PostMapping("/folhas/{folhaId}/vendas")
    @Operation(summary = "Adicionar nova venda de bico", description = "Adiciona um novo registro de venda a uma folha existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venda registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = LmcVendaBico.class))),
            @ApiResponse(responseCode = "400", description = "Dados de venda inválidos"),
            @ApiResponse(responseCode = "404", description = "Folha não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao adicionar venda")
    })
    public ResponseEntity<LmcVendaBico> adicionarVenda(
            @Parameter(description = "Identificador da folha")
            @PathVariable Long folhaId,
            @Valid @RequestBody LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {

        LmcVendaBico vendaSalva = lmcService.adicionarVenda(folhaId, vendaDTO);
        return new ResponseEntity<>(vendaSalva, HttpStatus.CREATED);
    }
    // --- FIM DA MUDANÇA ---

    @PutMapping("/vendas/{id}")
    @Operation(summary = "Atualizar venda de bico", description = "Atualiza uma venda de bico e recalcula a folha")
    // ... (ApiResponses)
    public ResponseEntity<LmcVendaBico> atualizarVenda(
            @Parameter(description = "Identificador da venda")
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {

        LmcVendaBico venda = lmcService.atualizarVenda(id, vendaDTO);
        return ResponseEntity.ok(venda);
    }

    @DeleteMapping("/vendas/{id}")
    @Operation(summary = "Deletar venda de bico", description = "Deleta uma venda de bico e recalcula a folha")
    // ... (ApiResponses)
    public ResponseEntity<Void> deletarVenda(
            @Parameter(description = "Identificador da venda")
            @PathVariable Long id) {
        lmcService.deletarVenda(id);
        return ResponseEntity.noContent().build();
    }

    // --- 4. NOVO ENDPOINT (ATUALIZAR OBSERVAÇÕES) ---
    @PutMapping("/folha/{folhaId}/observacoes")
    @Operation(summary = "Atualizar observações", description = "Atualiza o campo de observações de uma folha LMC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Observações atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = LmcFolha.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Folha não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<LmcFolha> atualizarObservacoes(
            @Parameter(description = "Identificador da folha")
            @PathVariable Long folhaId,
            @RequestBody Map<String, String> requestBody) { // Recebe um JSON simples: {"observacoes": "..."}

        String observacoes = requestBody.get("observacoes");
        LmcFolha folhaAtualizada = lmcService.atualizarObservacoes(folhaId, observacoes);
        return ResponseEntity.ok(folhaAtualizada);
    }
    // --- FIM DA MUDANÇA ---
}
