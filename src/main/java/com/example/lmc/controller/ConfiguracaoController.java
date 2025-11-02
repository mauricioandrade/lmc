package com.example.lmc.controller;

import com.example.lmc.dto.BicoDTO;
import com.example.lmc.dto.ProdutoDTO;
import com.example.lmc.dto.TanqueDTO;
import com.example.lmc.entity.Bico;
import com.example.lmc.entity.Produto;
import com.example.lmc.entity.Tanque;
import com.example.lmc.repository.BicoRepository;
import com.example.lmc.repository.ProdutoRepository;
import com.example.lmc.repository.TanqueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Configurações", description = "Endpoints para consulta de produtos, tanques e bicos")
public class ConfiguracaoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private TanqueRepository tanqueRepository;

    @Autowired
    private BicoRepository bicoRepository;

    @GetMapping("/produtos")
    @Operation(summary = "Listar produtos", description = "Retorna todos os produtos cadastrados para configuração da LMC")
    @ApiResponse(responseCode = "200", description = "Lista de produtos recuperada com sucesso")
    public ResponseEntity<List<ProdutoDTO>> getProdutos() {
        List<Produto> produtos = produtoRepository.findAll();

        List<ProdutoDTO> produtosDTO = produtos.stream()
                .map(p -> new ProdutoDTO(p.getId(), p.getNome()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(produtosDTO);
    }

    @GetMapping("/tanques")
    @Operation(summary = "Listar tanques por produto", description = "Retorna os tanques associados ao produto informado")
    @ApiResponse(responseCode = "200", description = "Lista de tanques recuperada com sucesso")
    public ResponseEntity<List<TanqueDTO>> getTanquesPorProduto(@RequestParam Long produtoId) {
        List<Tanque> tanques = tanqueRepository.findByProdutoId(produtoId);

        List<TanqueDTO> tanquesDTO = tanques.stream()
                .map(t -> new TanqueDTO(t.getId(), t.getNumero(), t.getCapacidadeNominal()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tanquesDTO);
    }

    @GetMapping("/bicos")
    @Operation(summary = "Listar bicos por tanque", description = "Retorna os bicos associados ao tanque informado")
    @ApiResponse(responseCode = "200", description = "Lista de bicos recuperada com sucesso")
    public ResponseEntity<List<BicoDTO>> getBicosPorTanque(@RequestParam Long tanqueId) {
        List<Bico> bicos = bicoRepository.findByTanqueId(tanqueId);

        List<BicoDTO> bicosDTO = bicos.stream()
                .map(b -> new BicoDTO(b.getId(), b.getNumero()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(bicosDTO);
    }
}
