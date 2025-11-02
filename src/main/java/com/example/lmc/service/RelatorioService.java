package com.example.lmc.service;

import com.example.lmc.entity.LmcFolha;
import com.example.lmc.repository.LmcFolhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {

    @Autowired
    private LmcFolhaRepository lmcFolhaRepository;

    public List<LmcFolha> gerarRelatorio(LocalDate inicio, LocalDate fim) {
        return lmcFolhaRepository.findByDataBetweenEager(inicio, fim);
    }

    public List<LmcFolha> gerarRelatorioPorProduto(Long produtoId, LocalDate inicio, LocalDate fim) {
        return lmcFolhaRepository.findByProdutoIdAndDataBetween(produtoId, inicio, fim);
    }
}
