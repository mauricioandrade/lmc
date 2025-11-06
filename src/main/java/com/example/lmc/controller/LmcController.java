package com.example.lmc.controller;

import com.example.lmc.dto.AtualizarObservacaoRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/lmc")
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@Tag(name = "LMC", description = "Operações principais da Folha LMC")
public class LmcController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LmcController.class);

    private final LmcService lmcService;
    private final RelatorioService relatorioService;
    private final RelatorioPdfService relatorioPdfService;

    public LmcController(LmcService lmcService, RelatorioService relatorioService, RelatorioPdfService relatorioPdfService) {
        this.lmcService = lmcService;
        this.relatorioService = relatorioService;
        this.relatorioPdfService = relatorioPdfService;
    }

    @PostMapping
    @Operation(summary = "Criar folha diária", description = "Registra uma nova folha LMC diária com medições, vendas e compras")
    public ResponseEntity<LmcFolha> salvarFolha(@Valid @RequestBody LmcFolhaRequestDTO requestDTO) {
        LmcFolha folhaSalva = lmcService.salvarFolhaDiaria(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(folhaSalva);
    }

    @GetMapping("/folha")
    @Operation(summary = "Buscar folha por data e produto", description = "Busca uma única folha LMC pela data e produto informado")
    public ResponseEntity<LmcFolha> buscarFolhaParaEdicao(
            @Parameter(description = "Data da folha a ser consultada", example = "2024-01-15")
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Identificador do produto da folha", example = "1")
            @RequestParam("produtoId") Long produtoId
    ) {
        try {
            return ResponseEntity.ok(lmcService.buscarFolhaPorDataEProduto(data, produtoId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/relatorio")
    @Operation(summary = "Gerar relatório em JSON", description = "Gera o relatório consolidado de folhas no período informado")
    public ResponseEntity<Set<LmcFolha>> gerarRelatorio(
            @Parameter(description = "Data inicial do período", example = "2024-01-01")
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do período", example = "2024-01-31")
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return ResponseEntity.ok(relatorioService.gerarRelatorio(inicio, fim));
    }

    @GetMapping("/relatorio/pdf")
    @Operation(summary = "Gerar relatório em PDF", description = "Exporta o relatório LMC consolidado em formato PDF")
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
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (JRException e) {
            if (e.getMessage() != null && e.getMessage().contains("Nenhum dado encontrado")) {
                return ResponseEntity.noContent().build();
            }
            LOGGER.error("Erro ao gerar relatório PDF", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            LOGGER.error("Erro inesperado ao gerar relatório PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/folhas/{folhaId}/medicoes")
    @Operation(summary = "Adicionar nova medição", description = "Adiciona uma nova medição de tanque a uma folha existente")
    public ResponseEntity<LmcMedicaoTanque> adicionarMedicao(
            @Parameter(description = "Identificador da folha")
            @PathVariable Long folhaId,
            @Valid @RequestBody LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        LmcMedicaoTanque medicaoSalva = lmcService.adicionarMedicaoTanque(folhaId, medicaoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(medicaoSalva);
    }

    @PutMapping("/medicoes/{id}")
    @Operation(summary = "Atualizar medição de tanque", description = "Atualiza uma medição de tanque e recalcula a folha")
    public ResponseEntity<LmcMedicaoTanque> atualizarMedicao(
            @Parameter(description = "Identificador da medição")
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.MedicaoTanqueDTO medicaoDTO) {
        return ResponseEntity.ok(lmcService.atualizarMedicaoTanque(id, medicaoDTO));
    }

    @DeleteMapping("/medicoes/{id}")
    @Operation(summary = "Deletar medição de tanque", description = "Deleta uma medição de tanque e recalcula a folha")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(compraSalva);
    }

    @PutMapping("/compras/{id}")
    @Operation(summary = "Atualizar compra", description = "Atualiza uma compra e recalcula a folha")
    public ResponseEntity<LmcCompra> atualizarCompra(
            @Parameter(description = "Identificador da compra")
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.CompraDTO compraDTO) {
        return ResponseEntity.ok(lmcService.atualizarCompra(id, compraDTO));
    }

    @DeleteMapping("/compras/{id}")
    @Operation(summary = "Deletar compra", description = "Deleta uma compra e recalcula a folha")
    public ResponseEntity<Void> deletarCompra(
            @Parameter(description = "Identificador da compra")
            @PathVariable Long id) {
        lmcService.deletarCompra(id);
        return ResponseEntity.noContent().build();
    }

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
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaSalva);
    }

    @PutMapping("/vendas/{id}")
    @Operation(summary = "Atualizar venda de bico", description = "Atualiza uma venda de bico e recalcula a folha")
    public ResponseEntity<LmcVendaBico> atualizarVenda(
            @Parameter(description = "Identificador da venda")
            @PathVariable Long id,
            @Valid @RequestBody LmcFolhaRequestDTO.VendaBicoDTO vendaDTO) {
        return ResponseEntity.ok(lmcService.atualizarVenda(id, vendaDTO));
    }

    @DeleteMapping("/vendas/{id}")
    @Operation(summary = "Deletar venda de bico", description = "Deleta uma venda de bico e recalcula a folha")
    public ResponseEntity<Void> deletarVenda(
            @Parameter(description = "Identificador da venda")
            @PathVariable Long id) {
        lmcService.deletarVenda(id);
        return ResponseEntity.noContent().build();
    }

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
            @RequestBody AtualizarObservacaoRequest request) {
        LmcFolha folhaAtualizada = lmcService.atualizarObservacoes(folhaId, request.getObservacoes());
        return ResponseEntity.ok(folhaAtualizada);
    }
}
