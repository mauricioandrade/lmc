package com.example.lmc.service;

import com.example.lmc.entity.LmcFolha;
import com.example.lmc.repository.LmcFolhaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class RelatorioService {

    private final LmcFolhaRepository lmcFolhaRepository;

    public RelatorioService(LmcFolhaRepository lmcFolhaRepository) {
        this.lmcFolhaRepository = lmcFolhaRepository;
    }

    public Set<LmcFolha> gerarRelatorio(LocalDate inicio, LocalDate fim) {
        return lmcFolhaRepository.findByDataBetweenEager(inicio, fim);
    }

    public List<LmcFolha> gerarRelatorioPorProduto(Long produtoId, LocalDate inicio, LocalDate fim) {
        return lmcFolhaRepository.findByProdutoIdAndDataBetween(produtoId, inicio, fim);
    }
}
