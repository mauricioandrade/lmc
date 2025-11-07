package com.example.lmc.controller;

import com.example.lmc.dto.BicoDTO;
import com.example.lmc.dto.EmpresaDTO;
import com.example.lmc.dto.ProdutoDTO;
import com.example.lmc.dto.TanqueDTO;
import com.example.lmc.entity.Produto;
import com.example.lmc.service.BicoService;
import com.example.lmc.service.EmpresaService;
import com.example.lmc.service.ProdutoService;
import com.example.lmc.service.TanqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@Tag(name = "Configurações", description = "Endpoints para consulta e gerenciamento de produtos, tanques e bicos")
public class ConfiguracaoController {


    private final ProdutoService produtoService;
    private final TanqueService tanqueService;
    private final BicoService bicoService;
    private final EmpresaService empresaService;

    @Autowired
    public ConfiguracaoController(ProdutoService produtoService,
                                  TanqueService tanqueService,
                                  EmpresaService empresaService,
                                  BicoService bicoService) {
        this.produtoService = produtoService;
        this.tanqueService = tanqueService;
        this.empresaService = empresaService;
        this.bicoService = bicoService;

    }

    @GetMapping("/produtos")
    @Operation(summary = "Listar produtos")
    public ResponseEntity<List<ProdutoDTO>> getProdutos() {
        List<ProdutoDTO> produtos = produtoService.listarTodos().stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/produtos/{id}")
    @Operation(summary = "Buscar um produto por ID")
    public ResponseEntity<ProdutoDTO> getProdutoById(@PathVariable Long id) {
        ProdutoDTO produto = ProdutoDTO.fromEntity(produtoService.buscarPorId(id));
        return ResponseEntity.ok(produto);
    }

    @PostMapping("/produtos")
    @Operation(summary = "Criar um novo produto")
    public ResponseEntity<ProdutoDTO> criarProduto(@RequestBody ProdutoDTO produtoDTO) {
        Produto produtoParaSalvar = new Produto();
        produtoParaSalvar.setNome(produtoDTO.getNome());
        Produto produtoSalvo = produtoService.salvarProduto(produtoParaSalvar);
        ProdutoDTO dtoRetorno = new ProdutoDTO(produtoSalvo.getId(), produtoSalvo.getNome());
        return new ResponseEntity<>(dtoRetorno, HttpStatus.CREATED);
    }

    @PutMapping("/produtos/{id}")
    @Operation(summary = "Atualizar um produto existente")
    public ResponseEntity<ProdutoDTO> atualizarProduto(@PathVariable Long id, @RequestBody ProdutoDTO produtoDTO) {
        Produto produtoParaAtualizar = new Produto();
        produtoParaAtualizar.setNome(produtoDTO.getNome());
        Produto produtoAtualizado = produtoService.atualizarProduto(id, produtoParaAtualizar);
        ProdutoDTO dtoRetorno = new ProdutoDTO(produtoAtualizado.getId(), produtoAtualizado.getNome());
        return ResponseEntity.ok(dtoRetorno);
    }

    @DeleteMapping("/produtos/{id}")
    @Operation(summary = "Deletar um produto")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/tanques")
    @Operation(summary = "Listar tanques por produto (para formulário LMC)")
    public ResponseEntity<List<TanqueDTO>> getTanquesPorProduto(
            @Parameter(description = "Identificador do produto") @RequestParam Long produtoId) {
        return ResponseEntity.ok(tanqueService.listarPorProduto(produtoId));
    }


    @GetMapping("/tanques/all")
    @Operation(summary = "Listar TODOS os tanques (para Admin)")
    public ResponseEntity<List<TanqueDTO>> getTodosTanques() {
        return ResponseEntity.ok(tanqueService.listarTodos());
    }

    @PostMapping("/tanques")
    @Operation(summary = "Criar um novo tanque")
    public ResponseEntity<TanqueDTO> criarTanque(@RequestBody TanqueDTO tanqueDTO) {
        TanqueDTO tanqueSalvo = tanqueService.salvarTanque(tanqueDTO);
        return new ResponseEntity<>(tanqueSalvo, HttpStatus.CREATED);
    }

    @PutMapping("/tanques/{id}")
    @Operation(summary = "Atualizar um tanque existente")
    public ResponseEntity<TanqueDTO> atualizarTanque(@PathVariable Long id, @RequestBody TanqueDTO tanqueDTO) {
        TanqueDTO tanqueAtualizado = tanqueService.atualizarTanque(id, tanqueDTO);
        return ResponseEntity.ok(tanqueAtualizado);
    }

    @DeleteMapping("/tanques/{id}")
    @Operation(summary = "Deletar um tanque")
    public ResponseEntity<Void> deletarTanque(@PathVariable Long id) {
        tanqueService.deletarTanque(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empresas")
    @Operation(summary = "Listar TODAS as empresas (para Admin)")
    public ResponseEntity<List<EmpresaDTO>> getTodasEmpresas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @PostMapping("/empresas")
    @Operation(summary = "Criar uma nova empresa")
    public ResponseEntity<EmpresaDTO> criarEmpresa(@RequestBody EmpresaDTO empresaDTO) {
        EmpresaDTO empresaSalva = empresaService.salvarEmpresa(empresaDTO);
        return new ResponseEntity<>(empresaSalva, HttpStatus.CREATED);
    }

    @PutMapping("/empresas/{id}")
    @Operation(summary = "Atualizar uma empresa existente")
    public ResponseEntity<EmpresaDTO> atualizarEmpresa(@PathVariable Long id, @RequestBody EmpresaDTO empresaDTO) {
        EmpresaDTO empresaAtualizada = empresaService.atualizarEmpresa(id, empresaDTO);
        return ResponseEntity.ok(empresaAtualizada);
    }

    @DeleteMapping("/empresas/{id}")
    @Operation(summary = "Deletar uma empresa")
    public ResponseEntity<Void> deletarEmpresa(@PathVariable Long id) {
        empresaService.deletarEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bicos")
    @Operation(summary = "Listar bicos por tanque (para formulário LMC)")
    public ResponseEntity<List<BicoDTO>> getBicosPorTanque(
            @Parameter(description = "Identificador do tanque") @RequestParam Long tanqueId) {
        return ResponseEntity.ok(bicoService.listarPorTanque(tanqueId));
    }

    @GetMapping("/bicos/all")
    @Operation(summary = "Listar TODOS os bicos (para Admin)")
    public ResponseEntity<List<BicoDTO>> getTodosBicos() {
        return ResponseEntity.ok(bicoService.listarTodos());
    }

    @PostMapping("/bicos")
    @Operation(summary = "Criar um novo bico")
    public ResponseEntity<BicoDTO> criarBico(@RequestBody BicoDTO bicoDTO) {
        BicoDTO bicoSalvo = bicoService.salvarBico(bicoDTO);
        return new ResponseEntity<>(bicoSalvo, HttpStatus.CREATED);
    }

    @PutMapping("/bicos/{id}")
    @Operation(summary = "Atualizar um bico existente")
    public ResponseEntity<BicoDTO> atualizarBico(@PathVariable Long id, @RequestBody BicoDTO bicoDTO) {
        BicoDTO bicoAtualizado = bicoService.atualizarBico(id, bicoDTO);
        return ResponseEntity.ok(bicoAtualizado);
    }

    @DeleteMapping("/bicos/{id}")
    @Operation(summary = "Deletar um bico")
    public ResponseEntity<Void> deletarBico(@PathVariable Long id) {
        bicoService.deletarBico(id);
        return ResponseEntity.noContent().build();
    }
}
