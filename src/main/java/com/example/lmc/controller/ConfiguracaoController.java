package com.example.lmc.controller;

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
    public ResponseEntity<List<Produto>> getProdutos() {
        return ResponseEntity.ok(produtoRepository.findAll());
    }

    @GetMapping("/tanques")
    public ResponseEntity<List<Tanque>> getTanquesPorProduto(@RequestParam Long produtoId) {
        return ResponseEntity.ok(tanqueRepository.findByProdutoId(produtoId));
    }

    @GetMapping("/bicos")
    public ResponseEntity<List<Bico>> getBicosPorTanque(@RequestParam Long tanqueId) {
        return ResponseEntity.ok(bicoRepository.findByTanqueId(tanqueId));
    }
}

