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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "http://localhost:5173")
public class ConfiguracaoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private TanqueRepository tanqueRepository;

    @Autowired
    private BicoRepository bicoRepository;

    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoDTO>> getProdutos() {
        List<Produto> produtos = produtoRepository.findAll();

        List<ProdutoDTO> produtosDTO = produtos.stream()
                .map(p -> new ProdutoDTO(p.getId(), p.getNome()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(produtosDTO);
    }

    @GetMapping("/tanques")
    public ResponseEntity<List<TanqueDTO>> getTanquesPorProduto(@RequestParam Long produtoId) {
        List<Tanque> tanques = tanqueRepository.findByProdutoId(produtoId);

        List<TanqueDTO> tanquesDTO = tanques.stream()
                .map(t -> new TanqueDTO(t.getId(), t.getNumero(), t.getCapacidadeNominal()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tanquesDTO);
    }

    @GetMapping("/bicos")
    public ResponseEntity<List<BicoDTO>> getBicosPorTanque(@RequestParam Long tanqueId) {
        List<Bico> bicos = bicoRepository.findByTanqueId(tanqueId);

        List<BicoDTO> bicosDTO = bicos.stream()
                .map(b -> new BicoDTO(b.getId(), b.getNumero()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(bicosDTO);
    }
}
